package com.tech.eventix.viewmodel

import app.cash.turbine.test
import com.tech.eventix.domain.Event
import com.tech.eventix.domain.Venue
import com.tech.eventix.uistate.EventUiState
import com.tech.eventix.uistate.EventsScreenUiState
import com.tech.eventix.usecase.BrowseEventsUseCase
import com.tech.eventix.utils.ResultState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
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
    fun `initial state should be Loading`() = runTest {
        // ASSERT
        assertEquals(EventsScreenUiState.Loading, viewModel.eventsScreenUiState.value)
    }

    @Test
    fun `uiState should load events successfully on first page`() = runTest {
        // ARRANGE
        val events = listOf(
            createValidEvent("Concert Event"),
            createValidEvent("Sports Event")
        )
        val expectedUiStates = events.map { it.toExpectedUiState() }

        val eventsFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns eventsFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        eventsFlow.emit(ResultState.Success(events))

        // ASSERT
        val currentState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(expectedUiStates, currentState.events)
        assertEquals(0, currentState.page)
        assertFalse(currentState.isLoadingMore)
        assertNull(currentState.paginationError)
        
        verify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
    }

    @Test
    fun `uiState should handle error on first page`() = runTest {
        // ARRANGE
        val errorMessage = "Network error"

        val eventsFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns eventsFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        eventsFlow.emit(ResultState.Error(RuntimeException(errorMessage)))

        // ASSERT
        val currentState: EventsScreenUiState.Error = viewModel.eventsScreenUiState.value as EventsScreenUiState.Error
        assertEquals(errorMessage, currentState.message)
        
        verify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
    }

    @Test
    fun `uiState should load next page when onLoadNextPage is called`() = runTest {
        // ARRANGE
        val firstPageEvents = listOf(createValidEvent("Event 1"))
        val secondPageEvents = listOf(createValidEvent("Event 2"))
        val allExpectedEvents = (firstPageEvents + secondPageEvents).map { it.toExpectedUiState() }

        val firstPageFlow = MutableSharedFlow<ResultState<List<Event>>>()
        val secondPageFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns firstPageFlow
        every { mockBrowseEventsUseCase(1, 20, null) } returns secondPageFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        // Emit first page
        firstPageFlow.emit(ResultState.Success(firstPageEvents))
        
        // Get first page state and trigger next page load
        val firstPageState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        firstPageState.onLoadNextPage()
        
        // Emit second page
        secondPageFlow.emit(ResultState.Success(secondPageEvents))

        // ASSERT
        val finalState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(allExpectedEvents, finalState.events)
        assertEquals(1, finalState.page)
        assertFalse(finalState.isLoadingMore)
        
        verify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        verify(exactly = 1) { mockBrowseEventsUseCase(1, 20, null) }
    }

    @Test
    fun `uiState should show loading state while loading next page`() = runTest {
        // ARRANGE
        val firstPageEvents = listOf(createValidEvent("Event 1"))

        val firstPageFlow = MutableSharedFlow<ResultState<List<Event>>>()
        val secondPageFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns firstPageFlow
        every { mockBrowseEventsUseCase(1, 20, null) } returns secondPageFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        // Emit first page
        firstPageFlow.emit(ResultState.Success(firstPageEvents))
        
        // Get first page state and trigger next page load
        val firstPageState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        firstPageState.onLoadNextPage()
        
        // ASSERT - Check loading more state before emitting second page
        val loadingMoreState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertTrue(loadingMoreState.isLoadingMore)
        assertNull(loadingMoreState.paginationError)
        
        // Emit second page to complete the loading
        secondPageFlow.emit(ResultState.Success(emptyList()))
        
        // ASSERT - Check final state
        val finalState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertFalse(finalState.isLoadingMore)
    }

    @Test
    fun `uiState should handle pagination error`() = runTest {
        // ARRANGE
        val firstPageEvents = listOf(createValidEvent("Event 1"))
        val errorMessage = "Pagination error"

        val firstPageFlow = MutableSharedFlow<ResultState<List<Event>>>()
        val secondPageFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns firstPageFlow
        every { mockBrowseEventsUseCase(1, 20, null) } returns secondPageFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        // Emit first page
        firstPageFlow.emit(ResultState.Success(firstPageEvents))
        
        // Get first page state and trigger next page load
        val firstPageState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        firstPageState.onLoadNextPage()
        
        // Emit error for second page
        secondPageFlow.emit(ResultState.Error(RuntimeException(errorMessage)))

        // ASSERT - Pagination error handled
        val errorState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(errorMessage, errorState.paginationError)
        assertEquals(0, errorState.page) // Page should remain at previous page
        assertFalse(errorState.isLoadingMore)
        assertEquals(firstPageEvents.map { it.toExpectedUiState() }, errorState.events)
        
        verify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        verify(exactly = 1) { mockBrowseEventsUseCase(1, 20, null) }
    }

    @Test
    fun `uiState should perform search with keyword`() = runTest {
        // ARRANGE
        val keyword = "concert"
        val searchResults = listOf(createValidEvent("Concert Event"))
        val expectedUiStates = searchResults.map { it.toExpectedUiState() }

        val initialFlow = MutableSharedFlow<ResultState<List<Event>>>()
        val searchFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns initialFlow
        every { mockBrowseEventsUseCase(0, 20, keyword) } returns searchFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        // Emit initial empty results
        initialFlow.emit(ResultState.Success(emptyList()))
        
        // Get initial state and trigger search
        val initialState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        initialState.onSearch(keyword)
        
        // Emit search results
        searchFlow.emit(ResultState.Success(searchResults))

        // ASSERT - Search results loaded
        val searchState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(expectedUiStates, searchState.events)
        assertEquals(0, searchState.page)
        
        verify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        verify(exactly = 1) { mockBrowseEventsUseCase(0, 20, keyword) }
    }

    @Test
    fun `uiState should search with empty keyword when searching with whitespace`() = runTest {
        // ARRANGE
        val whitespaceKeyword = "   "
        val initialEvents = listOf(createValidEvent("Event 1"))

        val eventsFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns eventsFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        // Emit initial events
        eventsFlow.emit(ResultState.Success(initialEvents))
        
        // Get initial state and trigger search with whitespace
        val initialState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        initialState.onSearch(whitespaceKeyword)
        
        // Emit the same events again (since whitespace gets trimmed to null)
        eventsFlow.emit(ResultState.Success(initialEvents))

        // ASSERT - Should call with null keyword (empty after trim)
        verify(exactly = 2) { mockBrowseEventsUseCase(0, 20, null) }
    }

    @Test
    fun `uiState should handle search error`() = runTest {
        // ARRANGE
        val keyword = "error"
        val errorMessage = "Search error"

        val initialFlow = MutableSharedFlow<ResultState<List<Event>>>()
        val searchFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns initialFlow
        every { mockBrowseEventsUseCase(0, 20, keyword) } returns searchFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        // Emit initial empty results
        initialFlow.emit(ResultState.Success(emptyList()))
        
        // Get initial state and trigger search
        val initialState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        initialState.onSearch(keyword)
        
        // Emit search error
        searchFlow.emit(ResultState.Error(RuntimeException(errorMessage)))

        // ASSERT - Search error handled
        val errorState: EventsScreenUiState.Error = viewModel.eventsScreenUiState.value as EventsScreenUiState.Error
        assertEquals(errorMessage, errorState.message)
        
        verify(exactly = 1) { mockBrowseEventsUseCase(0, 20, null) }
        verify(exactly = 1) { mockBrowseEventsUseCase(0, 20, keyword) }
    }

    @Test
    fun `uiState should replace events when searching (page 0)`() = runTest {
        // ARRANGE
        val initialEvents = listOf(createValidEvent("Initial Event"))
        val searchResults = listOf(createValidEvent("Search Result"))
        val keyword = "search"

        val initialFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        coEvery { mockBrowseEventsUseCase(0, 20, null) } coAnswers   {
            val keyword = args[2] as String? // null for initial load

            if(keyword == null) {
                initialFlow.emit(ResultState.Success(initialEvents))
            } else {
                initialFlow.emit(ResultState.Success(searchResults))
            }
            initialFlow
        }



        var searchPerformed = false

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                when (state) {
                    is EventsScreenUiState.Success -> {
                        if (!searchPerformed && state.events.size == 1 && state.events[0].name == "Initial Event") {
                            searchPerformed = true
                            // Get initial state and trigger search
                            state.onSearch(keyword)
                            
                            // Emit search results
                        } else if (searchPerformed && state.events.size == 1 && state.events[0].name == "Search Result") {
                            // ASSERT - Events replaced with search results

                        }
                    }
                    else -> {}
                }
            }
        }

        // Emit initial events

        val state: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success

        assertEquals(searchResults.map { it.toExpectedUiState() }, state.events)
        assertEquals(0, state.page)

        advanceTimeBy(500)
    }

    @Test
    fun `toUiState should map Event correctly`() = runTest {
        // ARRANGE
        val event = Event(
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

        val expectedUiState = EventUiState(
            name = "Test Concert",
            image = "https://example.com/image.jpg",
            dateTime = "Fri, 25 December, 7:00 pm",
            location = "Madison Square Garden, New York"
        )

        val eventsFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns eventsFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        eventsFlow.emit(ResultState.Success(listOf(event)))

        // ASSERT
        val currentState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals(1, currentState.events.size)
        val actualUiState = currentState.events[0]
        assertEquals(expectedUiState.name, actualUiState.name)
        assertEquals(expectedUiState.image, actualUiState.image)
        assertEquals(expectedUiState.dateTime, actualUiState.dateTime)
        assertEquals(expectedUiState.location, actualUiState.location)
    }

    @Test
    fun `toUiState should handle null imageUrl`() = runTest {
        // ARRANGE
        val event = createValidEvent("Test Event").copy(imageUrl = null)

        val eventsFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns eventsFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        eventsFlow.emit(ResultState.Success(listOf(event)))

        // ASSERT
        val currentState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals("", currentState.events[0].image)
    }

    @Test
    fun `toUiState should handle null venue`() = runTest {
        // ARRANGE
        val event = createValidEvent("Test Event").copy(venue = null)

        val eventsFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns eventsFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        eventsFlow.emit(ResultState.Success(listOf(event)))

        // ASSERT
        val currentState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals("", currentState.events[0].location)
    }

    @Test
    fun `toUiState should handle empty date and time`() = runTest {
        // ARRANGE
        val event = createValidEvent("Test Event").copy(date = "", time = "")

        val eventsFlow = MutableSharedFlow<ResultState<List<Event>>>()

        // STUB
        every { mockBrowseEventsUseCase(0, 20, null) } returns eventsFlow

        // ACT
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.eventsScreenUiState.collectLatest { state ->
                // Just collect to trigger the flow
            }
        }

        eventsFlow.emit(ResultState.Success(listOf(event)))

        // ASSERT
        val currentState: EventsScreenUiState.Success = viewModel.eventsScreenUiState.value as EventsScreenUiState.Success
        assertEquals("", currentState.events[0].dateTime)
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