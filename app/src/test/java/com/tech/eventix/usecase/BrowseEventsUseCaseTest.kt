package com.tech.eventix.usecase

import app.cash.turbine.test
import com.tech.eventix.domain.Event
import com.tech.eventix.domain.Venue
import com.tech.eventix.repository.EventRepository
import com.tech.eventix.utils.ResultState
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BrowseEventsUseCaseTest {

    private lateinit var useCase: BrowseEventsUseCase
    private val mockRepository: EventRepository = mockk()

    @Before
    fun setUp() {
        useCase = BrowseEventsUseCase(mockRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }


    @Test
    fun invoke_WithSuccessfulRepositoryResponse_ShouldReturnProcessedEvents() = runTest {
        // Arrange
        val rawEvents = listOf(
            createValidEvent("Concert Event").copy(date = "2024-12-25", time = "19:00:00"),
            createValidEvent("Sports Event").copy(date = "2024-01-01", time = "14:30:00")
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Concert Event").copy(
                date = "Wed, 25 December",
                time = "7:00 pm"
            ),
            createValidEvent("Sports Event").copy(
                date = "Mon, 1 January", 
                time = "2:30 pm"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Events should match expected processed events", expectedEvents, actualEvents)
            
            awaitComplete()
        }

        verify(exactly = 1) { mockRepository.getEvents(1, 20, null) }
    }

    @Test
    fun invoke_WithRepositoryError_ShouldReturnSameError() = runTest {
        // Arrange
        val errorResult = ResultState.Error(RuntimeException("Network error"))
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(errorResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Error result", result is ResultState.Error)
            
            val errorMessage = (result as ResultState.Error).getErrorMessage()
            assertEquals("Network error", errorMessage)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithKeywordParameter_ShouldPassKeywordToRepository() = runTest {
        // Arrange
        val keyword = "concert"
        val mockResult = ResultState.Success(emptyList<Event>())
        
        every { mockRepository.getEvents(0, 10, keyword) } returns flowOf(mockResult)

        // Act
        useCase.invoke(0, 10, keyword).test {
            awaitItem()
            awaitComplete()
        }

        // Assert
        verify(exactly = 1) { mockRepository.getEvents(0, 10, keyword) }
    }

    @Test
    fun invoke_WithDefaultParameters_ShouldUseDefaultPageSize() = runTest {
        // Arrange
        val mockResult = ResultState.Success(emptyList<Event>())
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act
        useCase.invoke(1).test {
            awaitItem()
            awaitComplete()
        }

        // Assert
        verify(exactly = 1) { mockRepository.getEvents(1, 20, null) }
    }



    @Test
    fun invoke_WithInvalidEvents_ShouldFilterOutInvalidEvents() = runTest {
        // Arrange
        val rawEvents = listOf(
            createValidEvent("Valid Event").copy(date = "2024-06-15", time = "20:00:00"),
            createEventWithEmptyName(),
            createEventWithNullImageUrl(),
            createEventWithNullVenue(),
            createTestEvent(),
            createValidEvent("Another Valid Event").copy(date = "2024-12-01", time = "09:30:00")
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Valid Event").copy(
                date = "Sat, 15 June",
                time = "8:00 pm"
            ),
            createValidEvent("Another Valid Event").copy(
                date = "Sun, 1 December",
                time = "9:30 am"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should filter and format only valid events", expectedEvents, actualEvents)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithEmptyEventList_ShouldReturnEmptyList() = runTest {
        // Arrange
        val mockResult = ResultState.Success(emptyList<Event>())
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val events = (result as ResultState.Success).data
            assertTrue("Expected empty list", events.isEmpty())
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithAllInvalidEvents_ShouldReturnEmptyList() = runTest {
        // Arrange
        val events = listOf(
            createEventWithEmptyName(),
            createEventWithNullImageUrl(),
            createTestEvent()
        )
        val mockResult = ResultState.Success(events)
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val events = (result as ResultState.Success).data
            assertTrue("Expected empty list", events.isEmpty())
            
            awaitComplete()
        }
    }


    @Test
    fun invoke_WithValidDateFormats_ShouldFormatDatesCorrectly() = runTest {
        // Arrange
        val rawEvents = listOf(
            createValidEvent("Event 1").copy(
                date = "2024-12-25",
                time = "19:30:00"
            ),
            createValidEvent("Event 2").copy(
                date = "2024-01-01", 
                time = "14:15:00"
            )
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Event 1").copy(
                date = "Wed, 25 December",
                time = "7:30 pm"
            ),
            createValidEvent("Event 2").copy(
                date = "Mon, 1 January",
                time = "2:15 pm"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should format dates and times correctly", expectedEvents, actualEvents)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithInvalidDateFormats_ShouldKeepOriginalDatesAndTimes() = runTest {
        // Arrange
        val rawEvents = listOf(
            createValidEvent("Event with bad dates").copy(
                date = "invalid-date",
                time = "invalid-time"
            )
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Event with bad dates").copy(
                date = "invalid-date",
                time = "invalid-time"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should preserve invalid date/time formats", expectedEvents, actualEvents)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithMidnightTime_ShouldFormatTo12HourFormat() = runTest {
        // Arrange
        val rawEvents = listOf(
            createValidEvent("Midnight Event").copy(
                date = "2024-12-31",
                time = "00:00:00"
            ),
            createValidEvent("Noon Event").copy(
                date = "2024-12-31", 
                time = "12:00:00"
            )
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Midnight Event").copy(
                date = "Tue, 31 December",
                time = "12:00 am"
            ),
            createValidEvent("Noon Event").copy(
                date = "Tue, 31 December",
                time = "12:00 pm"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should format midnight and noon times correctly", expectedEvents, actualEvents)
            
            awaitComplete()
        }
    }


    @Test
    fun invoke_WithEventWithEmptyVenueName_ShouldFilterOut() = runTest {
        // Arrange - Test the first condition: venue.name.isNotEmpty()
        val rawEvents = listOf(
            createValidEvent("Valid Event").copy(date = "2024-02-14", time = "15:00:00"),
            createEventWithIncompleteVenue("Empty Name Event", "", "Valid City", "Valid State", "Valid Address")
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Valid Event").copy(
                date = "Wed, 14 February",
                time = "3:00 pm"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should filter out events with empty venue name", expectedEvents, actualEvents)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithValidVenueNameButEmptyCity_ShouldFilterOut() = runTest {
        // Arrange - Test where name is valid but city is empty (tests different branch)
        val rawEvents = listOf(
            createValidEvent("Valid Event").copy(date = "2024-04-10", time = "11:30:00"),
            createEventWithIncompleteVenue("Valid Name Empty City", "Valid Venue Name", "", "Valid State", "Valid Address")
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Valid Event").copy(
                date = "Wed, 10 April",
                time = "11:30 am"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should filter out events where venue name is valid but city is empty", expectedEvents, actualEvents)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithEventWithIncompleteVenue_ShouldFilterOut() = runTest {
        // Arrange
        val rawEvents = listOf(
            createValidEvent("Valid Event").copy(date = "2024-03-15", time = "18:00:00"),
            createEventWithIncompleteVenue("Missing City", "name", "", "state", "address"),
            createEventWithIncompleteVenue("Missing State", "name", "city", "", "address"),
            createEventWithIncompleteVenue("Missing Address", "name", "city", "state", "")
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Valid Event").copy(
                date = "Fri, 15 March",
                time = "6:00 pm"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should filter out events with incomplete venues", expectedEvents, actualEvents)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithEventWithEmptyImageUrl_ShouldFilterOut() = runTest {
        // Arrange
        val rawEvents = listOf(
            createValidEvent("Valid Event").copy(date = "2024-08-20", time = "16:45:00"),
            createValidEvent("Event with empty image").copy(imageUrl = "")
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Valid Event").copy(
                date = "Tue, 20 August",
                time = "4:45 pm"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should filter out events with empty image URLs", expectedEvents, actualEvents)
            
            awaitComplete()
        }
    }



    @Test
    fun invoke_WithMixedValidAndInvalidEventsAndFormatting_ShouldProcessCorrectly() = runTest {
        // Arrange
        val rawEvents = listOf(
            createValidEvent("Valid Event 1").copy(date = "2024-06-15", time = "20:00:00"),
            createTestEvent(), // Should be filtered out
            createValidEvent("Valid Event 2").copy(date = "invalid-date", time = "invalid-time"),
            createEventWithEmptyName(), // Should be filtered out
            createValidEvent("Valid Event 3").copy(date = "2024-12-01", time = "09:30:00")
        )
        val mockResult = ResultState.Success(rawEvents)
        
        val expectedEvents = listOf(
            createValidEvent("Valid Event 1").copy(
                date = "Sat, 15 June",
                time = "8:00 pm"
            ),
            createValidEvent("Valid Event 2").copy(
                date = "invalid-date", // Should remain unchanged
                time = "invalid-time"  // Should remain unchanged
            ),
            createValidEvent("Valid Event 3").copy(
                date = "Sun, 1 December",
                time = "9:30 am"
            )
        )
        
        every { mockRepository.getEvents(1, 20, null) } returns flowOf(mockResult)

        // Act & Assert
        useCase.invoke(1, 20, null).test {
            val result = awaitItem()
            assertTrue("Expected Success result", result is ResultState.Success)
            
            val actualEvents = (result as ResultState.Success).data
            assertEquals("Should process mixed events correctly", expectedEvents, actualEvents)
            
            awaitComplete()
        }
    }



    private fun createValidEvent(name: String): Event {
        return Event(
            name = name,
            imageUrl = "https://example.com/image.jpg",
            date = "2024-12-25",
            time = "19:00:00",
            venue = Venue(
                name = "Test Venue",
                city = "Test City",
                state = "TS",
                address = "123 Test St"
            ),
            test = false
        )
    }

    private fun createEventWithEmptyName(): Event {
        return createValidEvent("").copy(name = "")
    }

    private fun createEventWithNullImageUrl(): Event {
        return createValidEvent("Event with null image").copy(imageUrl = null)
    }

    private fun createEventWithNullVenue(): Event {
        return createValidEvent("Event with null venue").copy(venue = null)
    }

    private fun createTestEvent(): Event {
        return createValidEvent("Test Event").copy(test = true)
    }

    private fun createEventWithIncompleteVenue(
        eventName: String,
        venueName: String,
        city: String,
        state: String,
        address: String
    ): Event {
        return createValidEvent(eventName).copy(
            venue = Venue(
                name = venueName,
                city = city,
                state = state,
                address = address
            )
        )
    }
} 