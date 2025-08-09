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
    fun viewModel_shouldEmitSuccessState_whenFirstPageRequested() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val events = listOf(
            createValidEvent("Concert Event"),
            createValidEvent("Sports Event")
        )
        val expectedUiStates = events.map { it.toExpectedUiState() }

        // STUB
        coEvery { mockBrowseEventsUseCase(0, 20, null) } coAnswers {
            flowOf(ResultState.Success(events))
        }

        // ACT - Start collecting state changes
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // ASSERT - Verify success state with correct data
        val currentState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(expectedUiStates, currentState.events)
        assertEquals(0, currentState.page)
        assertFalse(currentState.isLoadingMore)
        assertNull(currentState.paginationError)
        
        // VERIFY - Use case called with correct parameters
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

        // STUB - Return different results based on page parameter
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            val page = args[0] as Int
            if (page == 0) {
                flowOf(ResultState.Success(firstPageEvents))
            } else {
                flowOf(ResultState.Success(secondPageEvents))
            }
        }

        var nextPageTriggered = false

        // ACT - Collect state and trigger next page when first page is loaded
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                if (state is EventsScreenUiState.Success && !nextPageTriggered) {
                    val hasFirstPageEvent = state.events.any { it.name == "Event 1" }
                    if (hasFirstPageEvent && state.page == 0) {
                        nextPageTriggered = true
                        state.onLoadNextPage()
                    }
                }
            }
        }

        // ASSERT - Verify events appended and page incremented
        val finalState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(allExpectedEvents, finalState.events)
        assertEquals(1, finalState.page)
        assertFalse(finalState.isLoadingMore)

        // VERIFY - Both pages requested
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
        val firstPageEvents = listOf(createValidEvent("Event 1"))
        val errorMessage = "Pagination error"

        // STUB - Return different results based on page parameter
        coEvery { mockBrowseEventsUseCase(any(), any(), any()) } coAnswers {
            val page = args[0] as Int
            if (page == 0) {
                flowOf(ResultState.Success(firstPageEvents))
            } else {
                flowOf(ResultState.Error(RuntimeException(errorMessage)))
            }
        }

        var nextPageTriggered = false

        // ACT - Collect state and trigger pagination error
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                if (state is EventsScreenUiState.Success && !nextPageTriggered) {
                    val hasFirstPageEvent = state.events.any { it.name == "Event 1" }
                    if (hasFirstPageEvent && state.page == 0) {
                        nextPageTriggered = true
                        state.onLoadNextPage()
                    }
                }
            }
        }

        // ASSERT - Verify pagination error handled correctly
        val errorState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(errorMessage, errorState.paginationError)
        assertEquals(0, errorState.page) // Page remains at previous page
        assertFalse(errorState.isLoadingMore)
        assertEquals(firstPageEvents.map { it.toExpectedUiState() }, errorState.events)
        
        // VERIFY - Both pages requested
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        coVerify(exactly = 1) { mockBrowseEventsUseCase(1, 20, null) }
    }

    @Test
    fun viewModel_shouldEmitSearchResults_whenValidKeywordProvided() = runTest(UnconfinedTestDispatcher()) {
        // ARRANGE
        val keyword = "concert"
        val initialEvents = listOf(createValidEvent("Initial Event"))
        val searchResults = listOf(createValidEvent("Concert Event"))
        val expectedUiStates = searchResults.map { it.toExpectedUiState() }

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

        // ASSERT - Verify search results loaded correctly
        val searchState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(expectedUiStates, searchState.events)
        assertEquals(0, searchState.page)
        
        // VERIFY - Both initial load and search called
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        coVerify(exactly = 1) { mockBrowseEventsUseCase(0, 20, keyword) }
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
    fun viewModel_shouldEmitValidEventData_whenEventHasAllFields() = runTest(UnconfinedTestDispatcher()) {
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

        // STUB
        coEvery { mockBrowseEventsUseCase(0, 20, null) } coAnswers {
            flowOf(ResultState.Success(listOf(validEvent)))
        }

        // ACT - Load valid event
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { }
        }

        // ASSERT - Verify UI state contains event with populated fields
        val currentState = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(1, currentState.events.size)
        val eventUiState = currentState.events[0]
        assertEquals("Test Concert", eventUiState.name)
        assertTrue("Image should be populated", eventUiState.image.isNotEmpty())
        assertTrue("DateTime should be populated", eventUiState.dateTime.isNotEmpty())
        assertTrue("Location should be populated", eventUiState.location.isNotEmpty())
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