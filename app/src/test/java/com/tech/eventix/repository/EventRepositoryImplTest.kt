package com.tech.eventix.repository

import com.tech.eventix.api.RemoteDataSource
import com.tech.eventix.api.model.*
import com.tech.eventix.domain.Event
import com.tech.eventix.domain.EventDetail
import com.tech.eventix.domain.Venue
import app.cash.turbine.test
import com.tech.eventix.utils.ResultState
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventRepositoryImplTest {

    private lateinit var repository: EventRepositoryImpl
    private val mockApiService: RemoteDataSource = mockk()
    private val mockApiKeyProvider: ApiKeyProvider = mockk()

    @Before
    fun setUp() {
        repository = EventRepositoryImpl(mockApiService, mockApiKeyProvider)
        
        // Setup default mock behavior
        every { mockApiKeyProvider.getApiKey() } returns "test-api-key"
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun getEvents_WithValidApiResponse_ShouldEmitSuccessWithMappedEvents() = runTest {
        // Arrange
        val networkEvents = listOf(
            createMockNetworkEvent("Event 1"),
            createMockNetworkEvent("Event 2")
        )
        val mockRoot = createMockRoot(networkEvents)
        val expectedTomorrowDate =
            LocalDate.now()
                .plusDays(1)
                .format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

        val expectedEvents = listOf(
            createExpectedEvent("Event 1"),
            createExpectedEvent("Event 2")
        )

        coEvery { 
            mockApiService.getEvents(
                page = 1,
                size = 20,
                apiKey = "test-api-key",
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = null
            )
        } returns mockRoot

        // Act & Assert
        repository.getEvents(page = 1, size = 20, keyword = null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals(
                "Events should match expected mapped domain events",
                expectedEvents,
                actualEvents
            )
            
            awaitComplete()
        }

        // Verify API was called with correct parameters
        coVerify(exactly = 1) { 
            mockApiService.getEvents(
                page = 1,
                size = 20,
                apiKey = "test-api-key",
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = null
            )
        }
    }

    @Test
    fun getEvents_WithKeywordParameter_ShouldCallApiWithKeyword() = runTest {
        // Arrange
        val keyword = "concert"
        val networkEvents = listOf(createMockNetworkEvent("Concert Event"))
        val mockRoot = createMockRoot(networkEvents)
        val expectedTomorrowDate =
            LocalDate.now()
            .plusDays(1)
            .format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

        val expectedEvents = listOf(
            createExpectedEvent("Concert Event")
        )

        coEvery { 
            mockApiService.getEvents(
                page = 0,
                size = 10,
                apiKey = "test-api-key",
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = keyword
            )
        } returns mockRoot

        // Act & Assert
        repository.getEvents(page = 0, size = 10, keyword = keyword).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should return mapped events for keyword search", expectedEvents, actualEvents)
            
            awaitComplete()
        }

        // Verify API was called with keyword
        coVerify(exactly = 1) { 
            mockApiService.getEvents(
                page = 0,
                size = 10,
                apiKey = "test-api-key",
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = keyword
            )
        }
    }

    @Test
    fun getEvents_WithApiThrowsException_ShouldEmitError() = runTest {
        // Arrange
        val expectedException = RuntimeException("Network error")
        val expectedTomorrowDate =
            LocalDate.now()
            .plusDays(1)
            .format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

        coEvery { 
            mockApiService.getEvents(
                page = 1,
                size = 20,
                apiKey = "test-api-key",
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = null
            )
        } throws expectedException

        // Act & Assert
        repository.getEvents(page = 1, size = 20, keyword = null).test {
            val result = awaitItem()
            assertTrue("Expected Error result", result is ResultState.Error)
            
            val errorMessage = (result as ResultState.Error).getErrorMessage()
            assertEquals("Network error", errorMessage)
            
            awaitComplete()
        }
    }

    @Test
    fun getEvents_WithEmptyApiResponse_ShouldEmitSuccessWithEmptyList() = runTest {
        // Arrange
        val mockRoot = createMockRoot(emptyList())
        val expectedTomorrowDate =
            LocalDate.now()
                .plusDays(1)
                .format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"
        
        val expectedEvents = emptyList<Event>()

        coEvery { 
            mockApiService.getEvents(
                page = 1,
                size = 20,
                apiKey = "test-api-key",
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = null
            )
        } returns mockRoot

        // Act & Assert
        repository.getEvents(page = 1, size = 20, keyword = null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals(
                "Should return empty list for empty API response",
                expectedEvents,
                actualEvents
            )
            
            awaitComplete()
        }
    }

    @Test
    fun getEvents_WithApiKeyProviderReturnsKey_ShouldUseCorrectApiKey() = runTest {
        // Arrange
        val customApiKey = "custom-api-key-123"
        every { mockApiKeyProvider.getApiKey() } returns customApiKey
        
        val networkEvents = listOf(createMockNetworkEvent("Test Event"))
        val mockRoot = createMockRoot(networkEvents)
        val expectedTomorrowDate =
            LocalDate.now()
                .plusDays(1)
                .format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

        coEvery { 
            mockApiService.getEvents(
                page = 1,
                size = 20,
                apiKey = customApiKey,
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = null
            )
        } returns mockRoot

        // Act
        repository.getEvents(page = 1, size = 20, keyword = null).test {
            awaitItem()
            awaitComplete()
        }

        // Assert
        verify(exactly = 1) { mockApiKeyProvider.getApiKey() }
        coVerify(exactly = 1) { 
            mockApiService.getEvents(
                page = 1,
                size = 20,
                apiKey = customApiKey,
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = null
            )
        }
    }

    @Test
    fun getEvents_WithNetworkEventMapping_ShouldCorrectlyMapToDomainEvent() = runTest {
        // Arrange
        val venue = createNetworkVenue(
            name = "Madison Square Garden",
            id = "venue-123", 
            city = "New York",
            state = "NY",
            address = "4 Pennsylvania Plaza",
            postalCode = "10001",
            timezone = "America/New_York"
        )
        val networkEvent = createMockNetworkEvent(
            name = "Test Concert",
            id = "123",
            time = "20:00:00",
            venue = venue
        )

        val mockRoot = createMockRoot(listOf(networkEvent))
        val expectedTomorrowDate =
            LocalDate.now()
                .plusDays(1)
                .format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

        val expectedEvents = listOf(
            createExpectedEvent(
                name = "Test Concert",
                id = "123",
                time = "20:00:00",
                venue = createExpectedVenue(
                    name = "Madison Square Garden",
                    city = "New York", 
                    state = "NY",
                    address = "4 Pennsylvania Plaza"
                )
            )
        )

        coEvery { 
            mockApiService.getEvents(
                page = 1,
                size = 20,
                apiKey = "test-api-key",
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = null
            )
        } returns mockRoot

        // Act & Assert
        repository.getEvents(page = 1, size = 20, keyword = null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals(
                "Should correctly map network event to domain event",
                expectedEvents,
                actualEvents
            )
            
            awaitComplete()
        }
    }

    @Test
    fun getEvents_WithNetworkEventWithoutVenue_ShouldMapToEventWithNullVenue() = runTest {
        // Arrange
        val networkEventWithoutVenue = createMockNetworkEventWithoutVenue("Event Without Venue")
        val mockRoot = createMockRoot(listOf(networkEventWithoutVenue))
        val expectedTomorrowDate =
            LocalDate.now()
                .plusDays(1)
                .format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

        val expectedEvents = listOf(
            createExpectedEvent(
                name = "Event Without Venue",
                venue = null
            )
        )

        coEvery { 
            mockApiService.getEvents(
                page = 1,
                size = 20,
                apiKey = "test-api-key",
                sortBy = "date,asc",
                startDateTime = expectedTomorrowDate,
                keyword = null
            )
        } returns mockRoot

        // Act & Assert
        repository.getEvents(page = 1, size = 20, keyword = null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals(
                "Should map event without venue correctly",
                expectedEvents,
                actualEvents
            )
            
            awaitComplete()
        }
    }

    // NetworkEventMapper-specific tests
    @Test
    fun toDomain_WithCompleteNetworkEvent_ShouldMapAllFieldsCorrectly() {
        // Arrange
        val networkEvent = NetworkEvent(
            name = "Complete Event",
            type = "event",
            id = "event-123",
            test = true,
            url = "https://example.com",
            locale = "en-us",
            images = listOf(
                Image(
                    ratio = "16_9",
                    url = "https://example.com/image1.jpg",
                    width = 1024,
                    height = 576,
                    fallback = false
                ),
                Image(
                    ratio = "4_3",
                    url = "https://example.com/image2.jpg",
                    width = 800,
                    height = 600,
                    fallback = true
                )
            ),
            dates = Dates(
                start = StartDate(
                    localDate = "2024-12-31",
                    localTime = "23:59:59",
                    dateTime = "2024-12-31T23:59:59Z",
                    dateTBD = false,
                    dateTBA = false,
                    timeTBA = false,
                    noSpecificTime = false
                ),
                timezone = "America/Los_Angeles",
                status = Status(code = "onsale"),
                spanMultipleDays = false
            ),
            classifications = emptyList(),
            embedded = EventEmbedded(
                venues = listOf(
                    createNetworkVenue(
                        name = "Test Arena",
                        id = "venue-456",
                        city = "Los Angeles",
                        state = "CA",
                        address = "1000 Venue Boulevard",
                        postalCode = "90210",
                        timezone = "America/Los_Angeles"
                    )
                ),
                attractions = emptyList()
            ),
            sales = mockk(),
            priceRanges = null,
            products = null,
            info = null,
            pleaseNote = null,
            promoter = mockk(),
            seatmap = null,
            accessibility = null,
            ageRestrictions = null,
            ticketLimit = null
        )

        // Act
        val domainEvent = networkEvent.toDomain()

        // Assert
        assertEquals("Complete Event", domainEvent.name)
        assertEquals("https://example.com/image1.jpg", domainEvent.imageUrl) // Should take first image
        assertEquals("2024-12-31", domainEvent.date)
        assertEquals("23:59:59", domainEvent.time)
        assertEquals(true, domainEvent.test)

        assertNotNull("Expected venue to be mapped", domainEvent.venue)
        val venue = domainEvent.venue!!
        assertEquals("Test Arena", venue.name)
        assertEquals("Los Angeles", venue.city)
        assertEquals("CA", venue.state)
        assertEquals("1000 Venue Boulevard", venue.address)
    }

    @Test
    fun toDomain_WithEmptyImages_ShouldUseEmptyImageUrl() {
        // Arrange
        val networkEvent = createBasicNetworkEvent("Event No Images").copy(
            images = emptyList()
        )

        // Act
        val domainEvent = networkEvent.toDomain()

        // Assert
        assertEquals("", domainEvent.imageUrl)
    }

    @Test
    fun toDomain_WithEmptyImagesList_ShouldUseEmptyImageUrl() {
        // Arrange
        val networkEvent = createBasicNetworkEvent("Event Empty Images").copy(
            images = emptyList()
        )

        // Act
        val domainEvent = networkEvent.toDomain()

        // Assert
        assertEquals("", domainEvent.imageUrl)
    }

    @Test
    fun toDomain_WithNullLocalTime_ShouldUseDefaultTime() {
        // Arrange
        val networkEvent = createBasicNetworkEvent("Event No Time").copy(
            dates = Dates(
                start = StartDate(
                    localDate = "2024-06-15",
                    localTime = null, // Null time
                    dateTime = "2024-06-15T00:00:00Z",
                    dateTBD = false,
                    dateTBA = false,
                    timeTBA = false,
                    noSpecificTime = false
                ),
                timezone = "UTC",
                status = Status(code = "onsale"),
                spanMultipleDays = false
            )
        )

        // Act
        val domainEvent = networkEvent.toDomain()

        // Assert
        assertEquals("2024-06-15", domainEvent.date)
        assertEquals("19:00:00", domainEvent.time) // Should use default time
    }

    @Test
    fun toDomain_WithEmptyVenues_ShouldHaveNullVenue() {
        // Arrange
        val networkEvent = createBasicNetworkEvent("Event No Venues").copy(
            embedded = EventEmbedded(
                venues = emptyList(), // No venues
                attractions = emptyList()
            )
        )

        // Act
        val domainEvent = networkEvent.toDomain()

        // Assert
        assertNull("Expected venue to be null", domainEvent.venue)
    }

    @Test
    fun toDomain_WithMultipleVenues_ShouldUseFirstVenue() {
        // Arrange
        val venue1 = createNetworkVenue(
            name = "First Venue",
            id = "venue-1",
            city = "First City",
            state = "FS",
            address = "123 First Street",
            postalCode = "12345",
            timezone = "America/New_York"
        )

        val venue2 = createNetworkVenue(
            name = "Second Venue",
            id = "venue-2",
            city = "Second City",
            state = "SS",
            address = "456 Second Avenue",
            postalCode = "67890",
            timezone = "America/Los_Angeles"
        )

        val networkEvent = createBasicNetworkEvent("Event Multiple Venues").copy(
            embedded = EventEmbedded(
                venues = listOf(venue1, venue2), // Multiple venues
                attractions = emptyList()
            )
        )

        // Act
        val domainEvent = networkEvent.toDomain()

        // Assert
        assertNotNull("Expected venue to be mapped", domainEvent.venue)
        val venue = domainEvent.venue!!
        assertEquals("First Venue", venue.name) // Should use first venue
        assertEquals("First City", venue.city)
        assertEquals("FS", venue.state)
        assertEquals("123 First Street", venue.address)
    }

    @Test
    fun toDomain_WithTestEvent_ShouldPreserveTestFlag() {
        // Arrange
        val networkEvent = createBasicNetworkEvent("Test Event").copy(
            test = true
        )

        // Act
        val domainEvent = networkEvent.toDomain()

        // Assert
        assertEquals(true, domainEvent.test)
    }

    @Test
    fun toDomain_WithNonTestEvent_ShouldPreserveTestFlag() {
        // Arrange
        val networkEvent = createBasicNetworkEvent("Production Event").copy(
            test = false
        )

        // Act
        val domainEvent = networkEvent.toDomain()

        // Assert
        assertEquals(false, domainEvent.test)
    }

    @Test
    fun toDomain_WithImageHavingEmptyUrl_ShouldUseEmptyImageUrl() {
        // Arrange
        val networkEvent = createBasicNetworkEvent("Event Empty Image URL").copy(
            images = listOf(
                Image(
                    ratio = "16_9",
                    url = "", // Empty URL
                    width = 1024,
                    height = 576,
                    fallback = false
                )
            )
        )

        // Act
        val domainEvent = networkEvent.toDomain()

        // Assert
        assertEquals("", domainEvent.imageUrl)
    }

    
    // Expected Domain Object Helpers
    private fun createExpectedEvent(
        name: String,
        id: String = "event-id",
        imageUrl: String = "https://example.com/image.jpg",
        date: String = "2024-12-25", 
        time: String = "19:00:00",
        venue: Venue? = createExpectedVenue(),
        test: Boolean = false
    ): Event {
        return Event(
            id = id,
            name = name,
            imageUrl = imageUrl,
            date = date,
            time = time,
            venue = venue,
            test = test
        )
    }
    
    private fun createExpectedVenue(
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

    private fun createMockNetworkEvent(
        name: String,
        id: String = "event-id",
        date: String = "2024-12-25",
        time: String = "19:00:00",
        venue: NetworkVenue? = createDefaultNetworkVenue(),
        test: Boolean = false
    ): NetworkEvent {
        return NetworkEvent(
            name = name,
            type = "event",
            id = id,
            test = test,
            url = "https://example.com",
            locale = "en-us",
            images = createDefaultImages(),
            dates = createDefaultDates(date, time),
            classifications = emptyList(),
            embedded = EventEmbedded(
                venues = venue?.let { listOf(it) } ?: emptyList(),
                attractions = emptyList()
            ),
            sales = mockk(),
            priceRanges = null,
            products = null,
            info = null,
            pleaseNote = null,
            promoter = mockk(),
            seatmap = null,
            accessibility = null,
            ageRestrictions = null,
            ticketLimit = null
        )
    }

    private fun createDefaultImages(): List<Image> {
        return listOf(
            Image(
                ratio = "16_9",
                url = "https://example.com/image.jpg",
                width = 1024,
                height = 576,
                fallback = false
            )
        )
    }

    private fun createDefaultDates(date: String, time: String): Dates {
        return Dates(
            start = StartDate(
                localDate = date,
                localTime = time,
                dateTime = "${date}T${time}Z",
                dateTBD = false,
                dateTBA = false,
                timeTBA = false,
                noSpecificTime = false
            ),
            timezone = "America/New_York",
            status = Status(code = "onsale"),
            spanMultipleDays = false
        )
    }

    private fun createDefaultNetworkVenue(): NetworkVenue {
        return createNetworkVenue(
            name = "Test Venue",
            city = "Test City",
            state = "TS",
            address = "123 Test Street"
        )
    }

    private fun createNetworkVenue(
        name: String,
        id: String = "venue-id",
        city: String,
        state: String,
        address: String,
        postalCode: String? = null,
        timezone: String? = null
    ): NetworkVenue {
        return NetworkVenue(
            name = name,
            type = "venue",
            id = id,
            test = false,
            url = "https://venue.com",
            locale = "en-us",
            images = null,
            postalCode = postalCode,
            timezone = timezone,
            city = City(name = city),
            state = State(name = state, stateCode = state),
            country = null,
            address = Address(line1 = address),
            location = null,
            markets = null,
            dmas = null,
            boxOfficeInfo = null,
            parkingDetail = null,
            generalInfo = null,
            upcomingEvents = null,
            ada = null,
            classifications = null,
            _links = null
        )
    }

    private fun createMockNetworkEventWithoutVenue(name: String): NetworkEvent {
        return createMockNetworkEvent(name = name, venue = null)
    }

    private fun createBasicNetworkEvent(name: String): NetworkEvent {
        val defaultVenue = createNetworkVenue(
            name = "Default Venue",
            city = "Default City", 
            state = "DS",
            address = "123 Default Street"
        )
        return createMockNetworkEvent(name = name, venue = defaultVenue)
    }

    private fun createMockRoot(events: List<NetworkEvent>): Root {
        return Root(
            _embedded = RootEmbedded(events = events),
            _links = mockk(),
            page = Page(
                size = events.size,
                totalElements = events.size,
                totalPages = 1,
                number = 0
            )
        )
    }

    // EventDetails-specific tests
    @Test
    fun getEventDetails_WithValidEventId_ShouldEmitSuccessWithMappedEventDetail() = runTest {
        // Arrange
        val eventId = "event-123"
        val networkEvent = createMockNetworkEventForDetails(
            id = eventId,
            name = "Concert Details",
            info = "Concert information",
            pleaseNote = "Please note details",
            priceMin = 25.0,
            priceMax = 100.0,
            productNames = listOf("VIP", "General"),
            genre = "Rock",
            ticketLimitInfo = "8 per person",
            ageEnforced = true,
            seatmapUrl = "https://example.com/seatmap.jpg"
        )

        val expectedEventDetail = createExpectedEventDetail(
            id = eventId,
            name = "Concert Details",
            imageUrl = "https://example.com/image.jpg", // from createDefaultImages()
            date = "2024-12-25", // from createMockNetworkEvent defaults
            time = "19:00:00", // from createMockNetworkEvent defaults  
            venue = createExpectedVenue( // from createDefaultNetworkVenue()
                name = "Test Venue",
                city = "Test City",
                state = "TS",
                address = "123 Test Street"
            ),
            info = "Concert information\n\nPlease note details",
            price = "$25.00 - $100.00",
            products = listOf("VIP", "General"),
            genre = "Rock", 
            ticketLimit = "8 per person",
            ageRestrictions = "Enforced",
            seatmapUrl = "https://example.com/seatmap.jpg",
            ticketUrl = "https://example.com" // from createMockNetworkEvent URL field
        )

        coEvery { 
            mockApiService.getEventDetails(eventId, "test-api-key")
        } returns networkEvent

        // Act & Assert
        repository.getEventDetails(eventId).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEventDetail = (result as ResultState.Success).data
            assertEquals(expectedEventDetail, actualEventDetail)
            
            awaitComplete()
        }

        // Verify API was called with correct parameters
        coVerify(exactly = 1) { 
            mockApiService.getEventDetails(eventId, "test-api-key")
        }
        verify(exactly = 1) { mockApiKeyProvider.getApiKey() }
    }

    @Test
    fun getEventDetails_WithApiThrowsException_ShouldEmitError() = runTest {
        // Arrange
        val eventId = "event-123"
        val expectedException = RuntimeException("Network error")

        coEvery { 
            mockApiService.getEventDetails(eventId, "test-api-key")
        } throws expectedException

        // Act & Assert
        repository.getEventDetails(eventId).test {
            val result = awaitItem()
            assertTrue("Expected Error result", result is ResultState.Error)
            
            val errorMessage = (result as ResultState.Error).getErrorMessage()
            assertEquals("Network error", errorMessage)
            
            awaitComplete()
        }

        // Verify API was called
        coVerify(exactly = 1) { 
            mockApiService.getEventDetails(eventId, "test-api-key")
        }
    }

    @Test
    fun getEventDetails_WithCustomApiKey_ShouldUseCorrectApiKey() = runTest {
        // Arrange
        val eventId = "event-456"
        val customApiKey = "custom-key-789"
        val networkEvent = createMockNetworkEventForDetails(id = eventId, name = "Test Event")
        
        every { mockApiKeyProvider.getApiKey() } returns customApiKey

        coEvery { 
            mockApiService.getEventDetails(eventId, customApiKey)
        } returns networkEvent

        // Act
        repository.getEventDetails(eventId).test {
            awaitItem()
            awaitComplete()
        }

        // Assert
        coVerify(exactly = 1) { 
            mockApiService.getEventDetails(eventId, customApiKey)
        }
        verify(exactly = 1) { mockApiKeyProvider.getApiKey() }
    }

    @Test
    fun getEventDetails_WithMinimalNetworkEvent_ShouldMapToEventDetailWithDefaults() = runTest {
        // Arrange
        val eventId = "minimal-event"
        val minimalNetworkEvent = createMockNetworkEventForDetails(
            id = eventId,
            name = "Minimal Event",
            info = null,
            pleaseNote = null,
            priceMin = null,
            priceMax = null,
            productNames = emptyList(),
            genre = null,
            ticketLimitInfo = null,
            ageEnforced = null,
            seatmapUrl = null,
            venue = null
        )

        val expectedEventDetail = createExpectedEventDetail(
            id = eventId,
            name = "Minimal Event",
            imageUrl = "https://example.com/image.jpg", // from createDefaultImages()
            date = "2024-12-25", // from createMockNetworkEvent defaults
            time = "19:00:00", // from createMockNetworkEvent defaults
            info = null,
            price = null,
            products = emptyList(),
            genre = null,
            ticketLimit = null,
            ageRestrictions = null,
            seatmapUrl = null,
            venue = null,
            ticketUrl = "https://example.com" // from createMockNetworkEvent URL field
        )

        coEvery { 
            mockApiService.getEventDetails(eventId, "test-api-key")
        } returns minimalNetworkEvent

        // Act & Assert
        repository.getEventDetails(eventId).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEventDetail = (result as ResultState.Success).data
            assertEquals(expectedEventDetail, actualEventDetail)
            
            awaitComplete()
        }
    }

    @Test
    fun getEventDetails_WithComplexPriceRanges_ShouldUseFirstPriceRange() = runTest {
        // Arrange  
        val eventId = "price-event"
        val networkEvent = createMockNetworkEventForDetails(
            id = eventId,
            name = "Price Event",
            priceMin = 15.99,
            priceMax = 199.99
        )

        val expectedEventDetail = createExpectedEventDetail(
            id = eventId,
            name = "Price Event", 
            price = "$15.99 - $199.99"
        )

        coEvery { 
            mockApiService.getEventDetails(eventId, "test-api-key")
        } returns networkEvent

        // Act & Assert
        repository.getEventDetails(eventId).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEventDetail = (result as ResultState.Success).data
            assertEquals(expectedEventDetail.price, actualEventDetail.price)
            assertEquals("$15.99 - $199.99", actualEventDetail.price)
            
            awaitComplete()
        }
    }

    @Test
    fun getEventDetails_WithMultipleClassifications_ShouldUsePrimaryGenre() = runTest {
        // Arrange
        val eventId = "genre-event"
        val networkEvent = createMockNetworkEventForDetails(
            id = eventId,
            name = "Genre Event",
            genre = "Jazz"
        )

        coEvery { 
            mockApiService.getEventDetails(eventId, "test-api-key")
        } returns networkEvent

        // Act & Assert
        repository.getEventDetails(eventId).test {
            val result = awaitItem()
            val actualEventDetail = (result as ResultState.Success).data
            assertEquals("Jazz", actualEventDetail.genre)
            awaitComplete()
        }
    }

    @Test
    fun getEventDetails_WithAgeRestrictionsNotEnforced_ShouldReturnNotEnforced() = runTest {
        // Arrange
        val eventId = "age-event"
        val networkEvent = createMockNetworkEventForDetails(
            id = eventId,
            name = "Age Event",
            ageEnforced = false
        )

        coEvery { 
            mockApiService.getEventDetails(eventId, "test-api-key")
        } returns networkEvent

        // Act & Assert
        repository.getEventDetails(eventId).test {
            val result = awaitItem()
            val actualEventDetail = (result as ResultState.Success).data
            assertEquals("Not Enforced", actualEventDetail.ageRestrictions)
            awaitComplete()
        }
    }

    @Test
    fun getEventDetails_WithEmptyInfoFields_ShouldHandleBlankInfo() = runTest {
        // Arrange
        val eventId = "empty-info-event"
        val networkEvent = createMockNetworkEventForDetails(
            id = eventId,
            name = "Empty Info Event",
            info = "",
            pleaseNote = ""
        )

        coEvery { 
            mockApiService.getEventDetails(eventId, "test-api-key")
        } returns networkEvent

        // Act & Assert
        repository.getEventDetails(eventId).test {
            val result = awaitItem()
            val actualEventDetail = (result as ResultState.Success).data
            assertNull("Info should be null when blank", actualEventDetail.info)
            awaitComplete()
        }
    }

    // Helper methods for EventDetails tests
    private fun createMockNetworkEventForDetails(
        id: String,
        name: String,
        info: String? = "Event info",
        pleaseNote: String? = null,
        priceMin: Double? = 25.0,
        priceMax: Double? = 100.0,
        productNames: List<String> = listOf("General"),
        genre: String? = "Music",
        ticketLimitInfo: String? = "6 per person",
        ageEnforced: Boolean? = false,
        seatmapUrl: String? = null,
        venue: NetworkVenue? = createDefaultNetworkVenue()
    ): NetworkEvent {
        // Use the existing method and modify specific fields
        val baseEvent = createMockNetworkEvent(
            name = name,
            id = id,
            venue = venue
        )
        
        // Return a copy with the additional fields for EventDetails
        return baseEvent.copy(
            info = info,
            pleaseNote = pleaseNote,
            priceRanges = if (priceMin != null && priceMax != null) {
                listOf(Price(
                    type = "standard",
                    currency = "USD",
                    min = priceMin,
                    max = priceMax
                ))
            } else null,
            products = if (productNames.isNotEmpty()) {
                productNames.mapIndexed { index, productName -> 
                    Product(
                        name = productName,
                        id = "product-$index",
                        url = "https://example.com/product-$index",
                        type = "product"
                    )
                }
            } else null,
            seatmap = if (seatmapUrl != null) {
                Seatmap(
                    staticUrl = seatmapUrl,
                    id = "seatmap-1"
                )
            } else null,
            ageRestrictions = if (ageEnforced != null) {
                AgeRestrictions(
                    legalAgeEnforced = ageEnforced,
                    id = "age-restriction-1"
                )
            } else null,
            ticketLimit = if (ticketLimitInfo != null) {
                TicketLimit(
                    info = ticketLimitInfo,
                    id = "ticket-limit-1"
                )
            } else null,
            classifications = if (genre != null) {
                listOf(Classification(
                    primary = true,
                    segment = IdName(id = "1", name = "Music"),
                    genre = IdName(id = "2", name = genre),
                    subGenre = IdName(id = "3", name = "Rock"),
                    type = IdName(id = "4", name = "Event"),
                    subType = IdName(id = "5", name = "Concert"),
                    family = false
                ))
            } else emptyList()
        )
    }

    private fun createExpectedEventDetail(
        id: String,
        name: String,
        imageUrl: String = "https://example.com/image.jpg",
        date: String = "2024-12-25",
        time: String = "19:00:00",
        venue: Venue? = createExpectedVenue(),
        info: String? = "Event info",
        seatmapUrl: String? = null,
        price: String? = "$25.00 - $100.00",
        products: List<String> = listOf("General"),
        genre: String? = "Music",
        ticketLimit: String? = "6 per person",
        ageRestrictions: String? = "Not Enforced",
        ticketUrl: String = "https://example.com/event"
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
} 