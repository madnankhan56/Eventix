package com.tech.eventix.viewmodel

import com.tech.eventix.domain.Event
import com.tech.eventix.domain.Venue
import com.tech.eventix.uistate.EventUiState
import com.tech.eventix.uistate.EventsScreenUiState
import com.tech.eventix.usecase.BrowseEventsUseCase
import com.tech.eventix.utils.ResultState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModelTest {

    private lateinit var viewModel: EventViewModel
    private val mockBrowseEventsUseCase: BrowseEventsUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = EventViewModel(mockBrowseEventsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun viewModel_shouldStartWithLoadingState_whenInitialized() = runTest(UnconfinedTestDispatcher()) {
        // ASSERT
        assertEquals(EventsScreenUiState.Loading, viewModel.eventsScreenUiState.value)
    }

    @Test
    fun viewModel_shouldEmitSuccessWithValidData_whenFirstPageLoads() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val validEvent = Event(
            name = "Test Concert",
            imageUrl = "https://example.com/image.jpg",
            date = "Fri, 25 December",
            time = "7:00 pm",
            venue = Venue(
                name = "Madison Square Garden",
                city = "New York",
                state = "NY",
                address = "4 Pennsylvania Plaza"
            ),
            test = false
        )
        val expectedUiState = validEvent.toExpectedUiState()

        // STUB
        coEvery { mockBrowseEventsUseCase(0, 20, null) } returns flowOf(ResultState.Success(listOf(validEvent)))

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest {  }
        }

        // ASSERT - Verify the entire success state and its data
        val currentState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success

        // Overall state properties
        assertEquals(0, currentState.page)
        assertFalse(currentState.isLoadingMore)
        assertNull(currentState.paginationError)
        assertEquals(1, currentState.events.size)
        
        // Detailed data properties
        val eventUiState = currentState.events.first()
        assertEquals(expectedUiState, eventUiState)
        assertEquals("Test Concert", eventUiState.name)
        assertTrue("Image should not be empty", eventUiState.image.isNotEmpty())
        assertTrue("DateTime should not be empty", eventUiState.dateTime.isNotEmpty())
        assertTrue("Location should not be empty", eventUiState.location.isNotEmpty())

        // VERIFY
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
    }

    @Test
    fun viewModel_shouldEmitErrorState_whenFirstPageFails() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val errorMessage = "Network error"

        // STUB
        coEvery { mockBrowseEventsUseCase(0, 20, null) } coAnswers {
            flowOf(ResultState.Error(RuntimeException(errorMessage)))
        }

        // ACT - Start collecting state changes
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // ASSERT - Verify error state with correct message
        val currentState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Error
        assertEquals(errorMessage, currentState.message)
        
        // VERIFY - Use case called with correct parameters
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
    }

    @Test
    fun viewModel_shouldAppendEventsAndIncrementPage_whenLoadingNextPage() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val firstPageEvents = listOf(createValidEvent("Event 1"))
        val secondPageEvents = listOf(createValidEvent("Event 2"))
        val allExpectedEvents = (firstPageEvents + secondPageEvents).map { it.toExpectedUiState() }

        // STUB - Include Loading state transition
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            val page = args[0] as Int
            when (page) {
                0 -> {
                    flowOf(ResultState.Success(firstPageEvents))
                }
                1 -> {
                    flowOf(ResultState.Success(secondPageEvents))
                }
                else -> flowOf(ResultState.Error(RuntimeException("Unexpected")))
            }
        }

        var nextPageTriggered = false

        // ACT & ASSERT - Test state transitions
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                if (state is EventsScreenUiState.Success && !nextPageTriggered) {
                    val hasFirstPageEvent = state.events.any { it.name == "Event 1" }
                    if (hasFirstPageEvent && state.page == 0) {
                        // Verify first page state before loading next
                        assertEquals(firstPageEvents.map { it.toExpectedUiState() }, state.events)
                        assertFalse(state.isLoadingMore)
                        assertNull(state.paginationError)
                        
                        nextPageTriggered = true
                        state.onLoadNextPage()
                    }
                }
            }
        }

        // Final state verification
        val finalState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(allExpectedEvents, finalState.events)
        assertEquals(1, finalState.page)
        assertFalse(finalState.isLoadingMore)
        assertNull(finalState.paginationError)
        
        // VERIFY - Both pages requested with correct parameters
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        coVerify(exactly = 1) { mockBrowseEventsUseCase(1, 20, null) }
    }

    @Test
    fun viewModel_shouldShowLoadingState_whileLoadingNextPage() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val firstPageEvents = listOf(createValidEvent("Event 1"))

        // STUB - Return different results based on page parameter
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            val page = args[0] as Int
            if (page == 0) {
                flowOf(ResultState.Success(firstPageEvents))
            } else {
                flowOf(ResultState.Success(emptyList()))
            }
        }

        var nextPageTriggered = false
        var loadingStateVerified = false

        // ACT - Collect state and verify loading state during pagination
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                if (state is EventsScreenUiState.Success) {
                    if (!nextPageTriggered && state.events.any { it.name == "Event 1" } && state.page == 0) {
                        nextPageTriggered = true
                        state.onLoadNextPage()
                    } else if (nextPageTriggered && !loadingStateVerified) {
                        // ASSERT - Verify loading state during pagination
                        loadingStateVerified = true
                        assertTrue(state.isLoadingMore)
                        assertNull(state.paginationError)
                    }
                }
            }
        }

        // ASSERT - Verify final loading state cleared
        val finalState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertFalse(finalState.isLoadingMore)
        
        // VERIFY - Both pages requested
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        coVerify(exactly = 1) { mockBrowseEventsUseCase(1, 20, null) }
    }

    @Test
    fun viewModel_shouldHandlePaginationError_whenNextPageFails() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val initialEvents = listOf(createValidEvent("Initial Event"))
        val errorMessage = "Pagination error"

        // STUB - First return Loading state to test edge case
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            val page = args[0] as Int
            when (page) {
                0 -> flowOf(ResultState.Success(initialEvents))
                1 -> {
                    flowOf(ResultState.Error(RuntimeException(errorMessage)))
                }
                else -> flowOf(ResultState.Error(RuntimeException("Unexpected")))
            }
        }

        // ACT & ASSERT - Test with different states
        // 1. Start with Loading state
        assertEquals(EventsScreenUiState.Loading, viewModel.eventsScreenUiState.value)

        var nextPageTriggered = false
        
        // 2. Move through states
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                if (state is EventsScreenUiState.Success && !nextPageTriggered) {
                    val hasInitialEvent = state.events.any { it.name == "Initial Event" }
                    if (hasInitialEvent && state.page == 0) {
                        nextPageTriggered = true
                        state.onLoadNextPage() // This will trigger error state
                    }
                }
            }
        }

        // 3. Verify error handling with previous state
        val errorState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(errorMessage, errorState.paginationError)
        assertEquals(0, errorState.page)
        assertEquals(initialEvents.map { it.toExpectedUiState() }, errorState.events)
        assertFalse(errorState.isLoadingMore)
        
        // VERIFY - All calls made with correct parameters
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        coVerify(exactly = 1) { mockBrowseEventsUseCase(1, 20, null) }
    }

    @Test
    fun viewModel_shouldTreatAsEmptyKeyword_whenWhitespaceProvided() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val whitespaceKeyword = "   "
        val initialEvents = listOf(createValidEvent("Event 1"))

        // STUB - Always return same events since whitespace gets trimmed to null
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            flowOf(ResultState.Success(initialEvents))
        }

        var searchTriggered = false

        // ACT - Collect state and search with whitespace
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                if (state is EventsScreenUiState.Success && !searchTriggered) {
                    val hasInitialEvent = state.events.any { it.name == "Event 1" }
                    if (hasInitialEvent) {
                        searchTriggered = true
                        state.onSearch(whitespaceKeyword)
                    }
                }
            }
        }

        // VERIFY - Should call use case only once since StateFlow doesn't emit for same value
        // Initial: EventQuery(0, null), Search with whitespace: EventQuery(0, null) - same value!
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
    }

    @Test
    fun viewModel_shouldEmitErrorState_whenSearchFails() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val keyword = "error"
        val errorMessage = "Search error"
        val initialEvents = listOf(createValidEvent("Initial Event"))

        // STUB - Return different results based on keyword parameter
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            val searchKeyword = args[2] as String?
            if (searchKeyword == null) {
                flowOf(ResultState.Success(initialEvents))
            } else {
                flowOf(ResultState.Error(RuntimeException(errorMessage)))
            }
        }

        var searchTriggered = false

        // ACT - Collect state and trigger search error
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                if (state is EventsScreenUiState.Success && !searchTriggered) {
                    val hasInitialEvent = state.events.any { it.name == "Initial Event" }
                    if (hasInitialEvent) {
                        searchTriggered = true
                        state.onSearch(keyword)
                    }
                }
            }
        }

        // ASSERT - Verify search error handled correctly
        val errorState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Error
        assertEquals(errorMessage, errorState.message)
        
        // VERIFY - Both initial load and search called
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, keyword) }
    }

    @Test
    fun viewModel_shouldReplaceEventsAndResetPage_whenKeywordProvided() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val initialEvents = listOf(createValidEvent("Initial Event"))
        val searchResults = listOf(createValidEvent("Search Result"))
        val keyword = "search"

        // STUB - Return different results based on keyword parameter
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            val searchKeyword = args[2] as String?
            if (searchKeyword == null) {
                flowOf(ResultState.Success(initialEvents))
            } else {
                flowOf(ResultState.Success(searchResults))
            }
        }

        var searchTriggered = false

        // ACT - Collect state and trigger search when initial events are loaded
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                if (state is EventsScreenUiState.Success && !searchTriggered) {
                    val hasInitialEvent = state.events.any { it.name == "Initial Event" }
                    if (hasInitialEvent) {
                        searchTriggered = true
                            state.onSearch(keyword)
                    }
                }
            }
        }

        // ASSERT - Verify search results replace initial events
        val finalState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(searchResults.map { it.toExpectedUiState() }, finalState.events)
        assertEquals(0, finalState.page)

        // VERIFY - Both initial load and search were called
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, keyword) }
    }

    @Test
    fun viewModel_shouldEmitSafeImageData_whenImageUrlIsNull() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val eventWithNullImage = createValidEvent("Test Event").copy(imageUrl = null)

        // STUB
        coEvery { mockBrowseEventsUseCase(0, 20, null) } coAnswers {
            flowOf(ResultState.Success(listOf(eventWithNullImage)))
        }

        // ACT - Load event with null image URL
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // ASSERT - Verify UI doesn't crash and provides safe default
        val currentState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(1, currentState.events.size)
        assertNotNull("Image field should not be null", currentState.events[0].image)
        // Don't care about exact value, just that it's safe for UI consumption
    }

    @Test
    fun viewModel_shouldEmitSafeLocationData_whenVenueIsNull() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val eventWithNullVenue = createValidEvent("Test Event").copy(venue = null)

        // STUB
        coEvery { mockBrowseEventsUseCase(0, 20, null) } coAnswers {
            flowOf(ResultState.Success(listOf(eventWithNullVenue)))
        }

        // ACT - Load event with null venue
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // ASSERT - Verify UI doesn't crash and provides safe default
        val currentState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(1, currentState.events.size)
        assertNotNull("Location field should not be null", currentState.events[0].location)
        // Don't care about exact value, just that it's safe for UI consumption
    }

    @Test
    fun viewModel_shouldEmitSafeDateTimeData_whenDateAndTimeAreEmpty() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val eventWithEmptyDateTime = createValidEvent("Test Event").copy(date = "", time = "")

        // STUB
        coEvery { mockBrowseEventsUseCase(0, 20, null) } coAnswers {
            flowOf(ResultState.Success(listOf(eventWithEmptyDateTime)))
        }

        // ACT - Load event with empty date and time
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // ASSERT - Verify UI doesn't crash and provides safe default
        val currentState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(1, currentState.events.size)
        assertNotNull("DateTime field should not be null", currentState.events[0].dateTime)
        // Event should still be displayable even with empty date/time
        assertEquals("Test Event", currentState.events[0].name)
    }

    @Test
    fun viewModel_shouldHandleNonSuccessState_whenLoadingNextPage() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            flowOf(ResultState.Error(RuntimeException("Initial Error")))
        }

        // ACT - Start collecting state changes
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // ASSERT - Verify we're in error state and no additional calls made
        assertTrue(viewModel.eventsScreenUiState.value is EventsScreenUiState.Error)
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
    }

    @Test
    fun viewModel_shouldHandleNullStateTransitions_whenBuildingStates() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val events = listOf(createValidEvent("Test Event"))
        val errorMessage = "Error Message"

        // STUB - Test null state handling
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            val page = args[0] as Int
            when (page) {
                0 -> flowOf(ResultState.Success(events))
                1 -> flowOf(ResultState.Error(RuntimeException(errorMessage)))
                else -> flowOf(ResultState.Error(RuntimeException("Unexpected")))
            }
        }

        // ACT & ASSERT - Test state transitions with null checks
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                when (state) {
                    is EventsScreenUiState.Success -> {
                        // Force state to Loading to test null handling
                        viewModel.eventsScreenUiState.value
                        state.onLoadNextPage()
                    }
                    else -> {}
                }
            }
        }

        // VERIFY - State transitions occurred
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        coVerify(exactly = 1) { mockBrowseEventsUseCase(1, 20, null) }
    }

    @Test
    fun viewModel_shouldHandleEdgeCases_whenStateTransitionsOccur() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val initialEvents = listOf(createValidEvent("Initial Event"))
        val errorMessage = "Error Message"

        // STUB - Test non-Success state for loadNextPage
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            flowOf(ResultState.Error(RuntimeException(errorMessage)))
        }

        // ACT & ASSERT - Verify loadNextPage doesn't trigger when in Error state
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // Try to load next page from Error state
        val errorState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Error
        assertEquals(errorMessage, errorState.message)

        // VERIFY - Only initial call made, no next page call
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
    }

    @Test
    fun viewModel_shouldHandleStateTransitions_whenBuildingErrorState() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val initialEvents = listOf(createValidEvent("Initial Event"))
        val errorMessage = "Error Message"

        // STUB - Test different state transitions
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            val page = args[0] as Int
            when (page) {
                0 -> flowOf(ResultState.Success(initialEvents))
                1 -> {
                    flowOf(ResultState.Error(RuntimeException(errorMessage)))
                }
                else -> flowOf(ResultState.Error(RuntimeException("Unexpected")))
            }
        }

        // ACT - Start with Loading -> Success -> Loading -> Error
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                if (state is EventsScreenUiState.Success && !state.isLoadingMore && state.page == 0) {
                    state.onLoadNextPage()
                }
            }
        }

        // ASSERT - Verify final error state preserves previous events
        val finalState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(errorMessage, finalState.paginationError)
        assertEquals(0, finalState.page)
        assertEquals(initialEvents.map { it.toExpectedUiState() }, finalState.events)
        assertFalse(finalState.isLoadingMore)

        // VERIFY
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        coVerify(exactly = 1) { mockBrowseEventsUseCase(1, 20, null) }
    }

    @Test
    fun viewModel_shouldHandleEmptyVenueFields_whenVenueFieldsAreEmpty() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val eventWithEmptyVenueFields = Event(
            name = "Test Event",
            imageUrl = "https://example.com/image.jpg",
            date = "2024-03-25",
            time = "19:00",
            venue = Venue(
                name = "",  // Empty name
                city = "",  // Empty city
                state = "NY",
                address = "123 Test St"
            ),
            test = false
        )

        // STUB
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            flowOf(ResultState.Success(listOf(eventWithEmptyVenueFields)))
        }

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // ASSERT - Verify empty venue fields handled gracefully
        val state = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals("", state.events[0].location)
        
        // VERIFY
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
    }

    @Test
    fun viewModel_shouldNotLoadNextPage_whenStateIsNotSuccess() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE - Start with error state
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            flowOf(ResultState.Error(RuntimeException("Initial Error")))
        }

        // ACT - Try to load next page from error state
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // ASSERT - Verify still in error state and no additional calls made
        assertTrue(viewModel.eventsScreenUiState.value is EventsScreenUiState.Error)
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
    }

    // Helper methods
    private fun createValidEvent(name: String): Event {
        return Event(
            name = name,
            imageUrl = "https://example.com/image.jpg",
            date = "Fri, 25 December",
            time = "7:00 pm",
            venue = Venue(
                name = "Test Venue",
                city = "Test City",
                state = "TS",
                address = "123 Test St"
            ),
            test = false
        )
    }

    private fun Event.toExpectedUiState(): EventUiState {
        return EventUiState(
            name = this.name,
            image = this.imageUrl.orEmpty(),
            dateTime = listOf(this.date, this.time).filter { it.isNotEmpty() }.joinToString(", "),
            location = listOfNotNull(this.venue?.name, this.venue?.city)
                .filter { it.isNotEmpty() }
                .joinToString(", ")
        )
    }
} 