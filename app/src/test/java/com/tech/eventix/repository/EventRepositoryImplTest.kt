package com.tech.eventix.repository

import com.tech.eventix.api.RemoteDataSource
import com.tech.eventix.api.model.*
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
            
            val events = (result as ResultState.Success).data
            assertEquals("Expected 2 events", 2, events.size)
            assertEquals("Event 1", events[0].name)
            assertEquals("Event 2", events[1].name)
            
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
            
            val events = (result as ResultState.Success).data
            assertEquals("Expected 1 event", 1, events.size)
            assertEquals("Concert Event", events[0].name)
            
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
        val expectedTomorrowDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

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
        val expectedTomorrowDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

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
            
            val events = (result as ResultState.Success).data
            assertTrue("Expected empty list", events.isEmpty())
            
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
        val expectedTomorrowDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

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
        val networkEvent = NetworkEvent(
            name = "Test Concert",
            type = "event",
            id = "123",
            test = false,
            url = "https://example.com",
            locale = "en-us",
            images = listOf(
                Image(
                    ratio = "16_9",
                    url = "https://example.com/image.jpg",
                    width = 1024,
                    height = 576,
                    fallback = false
                )
            ),
            sales = mockk(),
            dates = Dates(
                start = StartDate(
                    localDate = "2024-12-25",
                    localTime = "20:00:00",
                    dateTime = "2024-12-25T20:00:00Z",
                    dateTBD = false,
                    dateTBA = false,
                    timeTBA = false,
                    noSpecificTime = false
                ),
                timezone = "America/New_York",
                status = Status(code = "onsale"),
                spanMultipleDays = false
            ),
            classifications = emptyList(),
            promoter = mockk(),
            promoters = emptyList(),
            info = null,
            pleaseNote = null,
            seatmap = null,
            accessibility = null,
            ticketLimit = null,
            ageRestrictions = null,
            ticketing = null,
            _links = mockk(),
            _embedded = EventEmbedded(
                venues = listOf(
                    NetworkVenue(
                        name = "Madison Square Garden",
                        type = "venue",
                        id = "venue-123",
                        test = false,
                        url = "https://venue.com",
                        locale = "en-us",
                        images = null,
                        postalCode = "10001",
                        timezone = "America/New_York",
                        city = City(name = "New York"),
                        state = State(name = "New York", stateCode = "NY"),
                        country = null,
                        address = Address(line1 = "4 Pennsylvania Plaza"),
                        location = null,
                        markets = null,
                        dmas = null,
                        boxOfficeInfo = null,
                        parkingDetail = null,
                        upcomingEvents = null,
                        ada = null,
                        classifications = null,
                        _links = null
                    )
                ),
                attractions = emptyList()
            )
        )

        val mockRoot = createMockRoot(listOf(networkEvent))
        val expectedTomorrowDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

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
            
            val events = (result as ResultState.Success).data
            assertEquals("Expected 1 event", 1, events.size)
            
            val event = events[0]
            assertEquals("Test Concert", event.name)
            assertEquals("https://example.com/image.jpg", event.imageUrl)
            assertEquals("2024-12-25", event.date)
            assertEquals("20:00:00", event.time)
            assertEquals(false, event.test)
            
            assertNotNull("Expected venue", event.venue)
            val venue = event.venue!!
            assertEquals("Madison Square Garden", venue.name)
            assertEquals("New York", venue.city)
            assertEquals("NY", venue.state)
            assertEquals("4 Pennsylvania Plaza", venue.address)
            
            awaitComplete()
        }
    }

    @Test
    fun getEvents_WithNetworkEventWithoutVenue_ShouldMapToEventWithNullVenue() = runTest {
        // Arrange
        val networkEventWithoutVenue = createMockNetworkEventWithoutVenue("Event Without Venue")
        val mockRoot = createMockRoot(listOf(networkEventWithoutVenue))
        val expectedTomorrowDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"

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
            
            val events = (result as ResultState.Success).data
            assertEquals("Expected 1 event", 1, events.size)
            
            val event = events[0]
            assertEquals("Event Without Venue", event.name)
            assertNull("Expected null venue", event.venue)
            
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
            sales = mockk(),
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
            promoter = mockk(),
            promoters = emptyList(),
            info = null,
            pleaseNote = null,
            seatmap = null,
            accessibility = null,
            ticketLimit = null,
            ageRestrictions = null,
            ticketing = null,
            _links = mockk(),
            _embedded = EventEmbedded(
                venues = listOf(
                    NetworkVenue(
                        name = "Test Arena",
                        type = "venue",
                        id = "venue-456",
                        test = false,
                        url = "https://venue.com",
                        locale = "en-us",
                        images = null,
                        postalCode = "90210",
                        timezone = "America/Los_Angeles",
                        city = City(name = "Los Angeles"),
                        state = State(name = "California", stateCode = "CA"),
                        country = null,
                        address = Address(line1 = "1000 Venue Boulevard"),
                        location = null,
                        markets = null,
                        dmas = null,
                        boxOfficeInfo = null,
                        parkingDetail = null,
                        upcomingEvents = null,
                        ada = null,
                        classifications = null,
                        _links = null
                    )
                ),
                attractions = emptyList()
            )
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
            _embedded = EventEmbedded(
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
        val venue1 = NetworkVenue(
            name = "First Venue",
            type = "venue",
            id = "venue-1",
            test = false,
            url = "https://venue1.com",
            locale = "en-us",
            images = null,
            postalCode = "12345",
            timezone = "America/New_York",
            city = City(name = "First City"),
            state = State(name = "First State", stateCode = "FS"),
            country = null,
            address = Address(line1 = "123 First Street"),
            location = null,
            markets = null,
            dmas = null,
            boxOfficeInfo = null,
            parkingDetail = null,
            upcomingEvents = null,
            ada = null,
            classifications = null,
            _links = null
        )

        val venue2 = NetworkVenue(
            name = "Second Venue",
            type = "venue",
            id = "venue-2",
            test = false,
            url = "https://venue2.com",
            locale = "en-us",
            images = null,
            postalCode = "67890",
            timezone = "America/Los_Angeles",
            city = City(name = "Second City"),
            state = State(name = "Second State", stateCode = "SS"),
            country = null,
            address = Address(line1 = "456 Second Avenue"),
            location = null,
            markets = null,
            dmas = null,
            boxOfficeInfo = null,
            parkingDetail = null,
            upcomingEvents = null,
            ada = null,
            classifications = null,
            _links = null
        )

        val networkEvent = createBasicNetworkEvent("Event Multiple Venues").copy(
            _embedded = EventEmbedded(
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

    // ================================================
    // HELPER FUNCTIONS
    // ================================================

    private fun createMockNetworkEvent(name: String): NetworkEvent {
        return NetworkEvent(
            name = name,
            type = "event",
            id = "event-id",
            test = false,
            url = "https://example.com",
            locale = "en-us",
            images = listOf(
                Image(
                    ratio = "16_9",
                    url = "https://example.com/image.jpg",
                    width = 1024,
                    height = 576,
                    fallback = false
                )
            ),
            sales = mockk(),
            dates = Dates(
                start = StartDate(
                    localDate = "2024-12-25",
                    localTime = "19:00:00",
                    dateTime = "2024-12-25T19:00:00Z",
                    dateTBD = false,
                    dateTBA = false,
                    timeTBA = false,
                    noSpecificTime = false
                ),
                timezone = "America/New_York",
                status = Status(code = "onsale"),
                spanMultipleDays = false
            ),
            classifications = emptyList(),
            promoter = mockk(),
            promoters = emptyList(),
            info = null,
            pleaseNote = null,
            seatmap = null,
            accessibility = null,
            ticketLimit = null,
            ageRestrictions = null,
            ticketing = null,
            _links = mockk(),
            _embedded = EventEmbedded(
                venues = listOf(
                    NetworkVenue(
                        name = "Test Venue",
                        type = "venue",
                        id = "venue-id",
                        test = false,
                        url = "https://venue.com",
                        locale = "en-us",
                        images = null,
                        postalCode = null,
                        timezone = null,
                        city = City(name = "Test City"),
                        state = State(name = "Test State", stateCode = "TS"),
                        country = null,
                        address = Address(line1 = "123 Test Street"),
                        location = null,
                        markets = null,
                        dmas = null,
                        boxOfficeInfo = null,
                        parkingDetail = null,
                        upcomingEvents = null,
                        ada = null,
                        classifications = null,
                        _links = null
                    )
                ),
                attractions = emptyList()
            )
        )
    }

    private fun createMockNetworkEventWithoutVenue(name: String): NetworkEvent {
        return NetworkEvent(
            name = name,
            type = "event",
            id = "event-id",
            test = false,
            url = "https://example.com",
            locale = "en-us",
            images = listOf(
                Image(
                    ratio = "16_9",
                    url = "https://example.com/image.jpg",
                    width = 1024,
                    height = 576,
                    fallback = false
                )
            ),
            sales = mockk(),
            dates = Dates(
                start = StartDate(
                    localDate = "2024-12-25",
                    localTime = "19:00:00",
                    dateTime = "2024-12-25T19:00:00Z",
                    dateTBD = false,
                    dateTBA = false,
                    timeTBA = false,
                    noSpecificTime = false
                ),
                timezone = "America/New_York",
                status = Status(code = "onsale"),
                spanMultipleDays = false
            ),
            classifications = emptyList(),
            promoter = mockk(),
            promoters = emptyList(),
            info = null,
            pleaseNote = null,
            seatmap = null,
            accessibility = null,
            ticketLimit = null,
            ageRestrictions = null,
            ticketing = null,
            _links = mockk(),
            _embedded = EventEmbedded(
                venues = emptyList(), // No venues
                attractions = emptyList()
            )
        )
    }

    private fun createBasicNetworkEvent(name: String): NetworkEvent {
        return NetworkEvent(
            name = name,
            type = "event",
            id = "event-id",
            test = false,
            url = "https://example.com",
            locale = "en-us",
            images = listOf(
                Image(
                    ratio = "16_9",
                    url = "https://example.com/image.jpg",
                    width = 1024,
                    height = 576,
                    fallback = false
                )
            ),
            sales = mockk(),
            dates = Dates(
                start = StartDate(
                    localDate = "2024-12-25",
                    localTime = "19:00:00",
                    dateTime = "2024-12-25T19:00:00Z",
                    dateTBD = false,
                    dateTBA = false,
                    timeTBA = false,
                    noSpecificTime = false
                ),
                timezone = "America/New_York",
                status = Status(code = "onsale"),
                spanMultipleDays = false
            ),
            classifications = emptyList(),
            promoter = mockk(),
            promoters = emptyList(),
            info = null,
            pleaseNote = null,
            seatmap = null,
            accessibility = null,
            ticketLimit = null,
            ageRestrictions = null,
            ticketing = null,
            _links = mockk(),
            _embedded = EventEmbedded(
                venues = listOf(
                    NetworkVenue(
                        name = "Default Venue",
                        type = "venue",
                        id = "venue-id",
                        test = false,
                        url = "https://venue.com",
                        locale = "en-us",
                        images = null,
                        postalCode = null,
                        timezone = null,
                        city = City(name = "Default City"),
                        state = State(name = "Default State", stateCode = "DS"),
                        country = null,
                        address = Address(line1 = "123 Default Street"),
                        location = null,
                        markets = null,
                        dmas = null,
                        boxOfficeInfo = null,
                        parkingDetail = null,
                        upcomingEvents = null,
                        ada = null,
                        classifications = null,
                        _links = null
                    )
                ),
                attractions = emptyList()
            )
        )
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
} 