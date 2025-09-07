package com.tech.eventix.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.tech.eventix.domain.EventDetail
import com.tech.eventix.domain.Venue
import com.tech.eventix.uistate.EventDetailUiState
import com.tech.eventix.uistate.EventDetailsScreenUiState
import com.tech.eventix.usecase.GetEventDetailsUseCase
import com.tech.eventix.utils.ResultState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailsViewModelTest {

    private lateinit var viewModel: EventDetailsViewModel
    private val mockGetEventDetailsUseCase: GetEventDetailsUseCase = mockk()
    private val mockSavedStateHandle: SavedStateHandle = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun viewModel_shouldStartWithLoadingState_whenInitialized() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns "test-event-id"
        coEvery { mockGetEventDetailsUseCase("test-event-id") } returns flowOf(ResultState.Success(createValidEventDetail()))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)

        // Assert
        assertEquals(EventDetailsScreenUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun viewModel_shouldEmitSuccessWithMappedData_whenEventDetailsLoaded() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(
            name = "Test Concert",
            imageUrl = "https://example.com/image.jpg",
            date = "Fri, 25 December",
            time = "7:30 pm",
            venue = createValidVenue(
                name = "Madison Square Garden",
                city = "New York",
                state = "NY",
                address = "4 Pennsylvania Plaza"
            ),
            price = "$50 - $150",
            info = "Concert information",
            seatmapUrl = "https://example.com/seatmap.jpg",
            products = listOf("VIP Package", "General Admission"),
            genre = "Rock",
            ticketLimit = "8 tickets per person",
            ageRestrictions = "18+",
            ticketUrl = "https://ticketmaster.com/buy"
        )
        val expectedUiState = eventDetail.toExpectedUiState()

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert - Verify the entire success state and its data
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success

        assertEquals(expectedUiState, currentState.event)
        assertEquals("Test Concert", currentState.event.name)
        assertEquals("https://example.com/image.jpg", currentState.event.image)
        assertEquals("Fri, 25 December, 7:30 pm", currentState.event.dateTime)
        assertEquals("Madison Square Garden, New York", currentState.event.location)
        assertEquals("$50 - $150", currentState.event.price)
        assertEquals("Concert information", currentState.event.info)
        assertEquals("https://example.com/seatmap.jpg", currentState.event.seatmapUrl)
        assertEquals(listOf("VIP Package", "General Admission"), currentState.event.products)
        assertEquals("Rock", currentState.event.genre)
        assertEquals("8 tickets per person", currentState.event.ticketLimit)
        assertEquals("18+", currentState.event.ageRestrictions)
        assertEquals("https://ticketmaster.com/buy", currentState.event.ticketUrl)

        // Verify use case was called with correct event ID
        coVerify(exactly = 1) { mockGetEventDetailsUseCase(eventId) }
    }

    @Test
    fun viewModel_shouldEmitErrorState_whenEventDetailsLoadFails() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val errorMessage = "Failed to load event details"
        
        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Error(RuntimeException(errorMessage)))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert - Verify error state with correct message
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Error
        assertEquals(errorMessage, currentState.message)

        // Verify use case was called
        coVerify(exactly = 1) { mockGetEventDetailsUseCase(eventId) }
    }

    @Test
    fun viewModel_shouldEmitErrorState_whenEventIdIsNull() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns null

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Error
        assertEquals("Event ID not provided", currentState.message)

        // Verify use case was never called since no event ID was provided
        coVerify(exactly = 0) { mockGetEventDetailsUseCase(any()) }
    }

    @Test
    fun viewModel_shouldHandleNullImageUrl_whenEventHasNoImage() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail().copy(imageUrl = null)

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals("", currentState.event.image)
    }

    @Test
    fun viewModel_shouldHandleNullVenue_whenEventHasNoVenue() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail().copy(venue = null)

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals("", currentState.event.location)
    }

    @Test
    fun viewModel_shouldFormatDateTime_whenDateAndTimeAreProvided() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(
            date = "Mon, 15 January",
            time = "8:00 pm"
        )

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals("Mon, 15 January, 8:00 pm", currentState.event.dateTime)
    }

    @Test
    fun viewModel_shouldHandleEmptyDateAndTime_whenDateTimeFieldsAreEmpty() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(
            date = "",
            time = ""
        )

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals("", currentState.event.dateTime)
    }

    @Test
    fun viewModel_shouldHandlePartialDateTime_whenOnlyDateIsProvided() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(
            date = "Sat, 20 February",
            time = ""
        )

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals("Sat, 20 February", currentState.event.dateTime)
    }

    @Test
    fun viewModel_shouldHandlePartialDateTime_whenOnlyTimeIsProvided() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(
            date = "",
            time = "9:15 pm"
        )

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals("9:15 pm", currentState.event.dateTime)
    }

    @Test
    fun viewModel_shouldFormatLocation_whenVenueNameAndCityAreProvided() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(
            venue = createValidVenue(
                name = "Apollo Theater",
                city = "New York",
                state = "NY",
                address = "253 W 125th St"
            )
        )

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals("Apollo Theater, New York", currentState.event.location)
    }

    @Test
    fun viewModel_shouldHandleEmptyVenueFields_whenVenueFieldsAreEmpty() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(
            venue = createValidVenue(
                name = "",
                city = "",
                state = "NY",
                address = "123 Test St"
            )
        )

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals("", currentState.event.location)
    }

    @Test
    fun viewModel_shouldHandlePartialVenueInfo_whenOnlyVenueNameIsProvided() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(
            venue = createValidVenue(
                name = "Concert Hall",
                city = "",
                state = "CA",
                address = "456 Main St"
            )
        )

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals("Concert Hall", currentState.event.location)
    }

    @Test
    fun viewModel_shouldHandleNullOptionalFields_whenOptionalFieldsAreNull() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(
            price = null,
            info = null,
            seatmapUrl = null,
            genre = null,
            ticketLimit = null,
            ageRestrictions = null,
            ticketUrl = null
        )

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertNull(currentState.event.price)
        assertNull(currentState.event.info)
        assertNull(currentState.event.seatmapUrl)
        assertNull(currentState.event.genre)
        assertNull(currentState.event.ticketLimit)
        assertNull(currentState.event.ageRestrictions)
        assertNull(currentState.event.ticketUrl)
    }

    @Test
    fun viewModel_shouldHandleEmptyProductsList_whenNoProductsAvailable() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val eventDetail = createValidEventDetail(products = emptyList())

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertTrue(currentState.event.products.isEmpty())
    }

    @Test
    fun viewModel_shouldPreserveProductsList_whenMultipleProductsAvailable() = runTest(UnconfinedTestDispatcher()) {
        // Arrange
        val eventId = "test-event-123"
        val products = listOf("Early Bird", "VIP", "General", "Student Discount")
        val eventDetail = createValidEventDetail(products = products)

        every { mockSavedStateHandle.get<String>(EventDetailsViewModel.EVENT_ID_KEY) } returns eventId
        coEvery { mockGetEventDetailsUseCase(eventId) } returns flowOf(ResultState.Success(eventDetail))

        // Act
        viewModel = EventDetailsViewModel(mockSavedStateHandle, mockGetEventDetailsUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collectLatest { }
        }

        // Assert
        val currentState = viewModel.uiState.value as EventDetailsScreenUiState.Success
        assertEquals(products, currentState.event.products)
        assertEquals(4, currentState.event.products.size)
    }

    // Helper methods
    private fun createValidEventDetail(
        id: String = "event-123",
        name: String = "Test Event",
        imageUrl: String? = "https://example.com/image.jpg",
        date: String = "Fri, 25 December",
        time: String = "7:00 pm",
        venue: Venue? = createValidVenue(),
        info: String? = "Event information",
        seatmapUrl: String? = "https://example.com/seatmap.jpg",
        price: String? = "$25 - $100",
        products: List<String> = listOf("General Admission", "VIP"),
        genre: String? = "Music",
        ticketLimit: String? = "6 per person",
        ageRestrictions: String? = "All ages",
        ticketUrl: String? = "https://example.com/tickets"
    ): EventDetail {
        return EventDetail(
            id = id,
            name = name,
            imageUrl = imageUrl,
            date = date,
            time = time,
            venue = venue,
            info = info,
            seatmapUrl = seatmapUrl,
            price = price,
            products = products,
            genre = genre,
            ticketLimit = ticketLimit,
            ageRestrictions = ageRestrictions,
            ticketUrl = ticketUrl
        )
    }

    private fun createValidVenue(
        name: String = "Test Venue",
        city: String = "Test City",
        state: String = "TS",
        address: String = "123 Test Street"
    ): Venue {
        return Venue(
            name = name,
            city = city,
            state = state,
            address = address
        )
    }

    private fun EventDetail.toExpectedUiState(): EventDetailUiState {
        val dateTimeCombined = listOf(date, time).filter { it.isNotEmpty() }.joinToString(", ")
        
        val location = listOfNotNull(venue?.name, venue?.city)
            .filter { it.isNotEmpty() }
            .joinToString(", ")

        return EventDetailUiState(
            name = name,
            image = imageUrl.orEmpty(),
            dateTime = dateTimeCombined,
            location = location,
            price = price,
            info = info,
            seatmapUrl = seatmapUrl,
            products = products,
            genre = genre,
            ticketLimit = ticketLimit,
            ageRestrictions = ageRestrictions,
            ticketUrl = ticketUrl
        )
    }
}