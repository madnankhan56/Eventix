package com.tech.eventix.usecase

import app.cash.turbine.test
import com.tech.eventix.domain.EventDetail
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

class GetEventDetailsUseCaseTest {

    private lateinit var useCase: GetEventDetailsUseCase
    private val mockRepository: EventRepository = mockk()

    @Before
    fun setUp() {
        useCase = GetEventDetailsUseCase(mockRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun invoke_WithValidEventId_ShouldReturnFormattedEventDetails() = runTest {
        // Arrange
        val eventId = "test-event-123"
        val rawEventDetail = createValidEventDetail().copy(
            date = "2024-12-25",
            time = "19:30:00"
        )
        val repositoryResult = ResultState.Success(rawEventDetail)
        
        val expectedEventDetail = rawEventDetail.copy(
            date = "Wed, 25 December",
            time = "7:30 pm"
        )
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(repositoryResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Success result", result is ResultState.Success)
            val actualEventDetail = (result as ResultState.Success).data
            assertEquals("Should format date and time correctly", expectedEventDetail, actualEventDetail)
            
            awaitComplete()
        }

        verify(exactly = 1) { mockRepository.getEventDetails(eventId) }
    }

    @Test
    fun invoke_WithRepositoryError_ShouldReturnSameError() = runTest {
        // Arrange
        val eventId = "invalid-event-id"
        val errorResult = ResultState.Error(RuntimeException("Event not found"))
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(errorResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Error result", result is ResultState.Error)
            val errorMessage = (result as ResultState.Error).getErrorMessage()
            assertEquals("Event not found", errorMessage)
            
            awaitComplete()
        }

        verify(exactly = 1) { mockRepository.getEventDetails(eventId) }
    }

    @Test
    fun invoke_WithMidnightTime_ShouldFormatTo12AmFormat() = runTest {
        // Arrange
        val eventId = "midnight-event"
        val rawEventDetail = createValidEventDetail().copy(
            date = "2024-12-31",
            time = "00:00:00"
        )
        val repositoryResult = ResultState.Success(rawEventDetail)
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(repositoryResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Success result", result is ResultState.Success)
            val eventDetail = (result as ResultState.Success).data
            assertEquals("Should format midnight as 12:00 am", "12:00 am", eventDetail.time)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithNoonTime_ShouldFormatTo12PmFormat() = runTest {
        // Arrange
        val eventId = "noon-event"
        val rawEventDetail = createValidEventDetail().copy(
            date = "2024-12-31",
            time = "12:00:00"
        )
        val repositoryResult = ResultState.Success(rawEventDetail)
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(repositoryResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Success result", result is ResultState.Success)
            val eventDetail = (result as ResultState.Success).data
            assertEquals("Should format noon as 12:00 pm", "12:00 pm", eventDetail.time)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithMorningTime_ShouldReturnAmFormat() = runTest {
        // Arrange
        val eventId = "morning-event"
        val rawEventDetail = createValidEventDetail().copy(
            date = "2024-06-15",
            time = "09:30:00"
        )
        val repositoryResult = ResultState.Success(rawEventDetail)
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(repositoryResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Success result", result is ResultState.Success)
            val eventDetail = (result as ResultState.Success).data
            assertEquals("Should format morning time correctly", "9:30 am", eventDetail.time)
            assertEquals("Should format date correctly", "Sat, 15 June", eventDetail.date)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithEveningTime_ShouldReturnPmFormat() = runTest {
        // Arrange
        val eventId = "evening-event"
        val rawEventDetail = createValidEventDetail().copy(
            date = "2024-01-01",
            time = "18:45:00"
        )
        val repositoryResult = ResultState.Success(rawEventDetail)
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(repositoryResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Success result", result is ResultState.Success)
            val eventDetail = (result as ResultState.Success).data
            assertEquals("Should format evening time correctly", "6:45 pm", eventDetail.time)
            assertEquals("Should format date correctly", "Mon, 1 January", eventDetail.date)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithInvalidDateFormat_ShouldKeepOriginalDate() = runTest {
        // Arrange
        val eventId = "invalid-date-event"
        val invalidDate = "not-a-valid-date"
        val rawEventDetail = createValidEventDetail().copy(
            date = invalidDate,
            time = "19:00:00"
        )
        val repositoryResult = ResultState.Success(rawEventDetail)
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(repositoryResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Success result", result is ResultState.Success)
            val eventDetail = (result as ResultState.Success).data
            assertEquals("Should preserve invalid date format", invalidDate, eventDetail.date)
            assertEquals("Should still format valid time", "7:00 pm", eventDetail.time)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithInvalidTimeFormat_ShouldKeepOriginalTime() = runTest {
        // Arrange
        val eventId = "invalid-time-event"
        val invalidTime = "not-a-valid-time"
        val rawEventDetail = createValidEventDetail().copy(
            date = "2024-12-25",
            time = invalidTime
        )
        val repositoryResult = ResultState.Success(rawEventDetail)
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(repositoryResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Success result", result is ResultState.Success)
            val eventDetail = (result as ResultState.Success).data
            assertEquals("Should still format valid date", "Wed, 25 December", eventDetail.date)
            assertEquals("Should preserve invalid time format", invalidTime, eventDetail.time)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithInvalidDateAndTime_ShouldKeepOriginalValues() = runTest {
        // Arrange
        val eventId = "invalid-datetime-event"
        val invalidDate = "bad-date"
        val invalidTime = "bad-time"
        val rawEventDetail = createValidEventDetail().copy(
            date = invalidDate,
            time = invalidTime
        )
        val repositoryResult = ResultState.Success(rawEventDetail)
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(repositoryResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Success result", result is ResultState.Success)
            val eventDetail = (result as ResultState.Success).data
            assertEquals("Should preserve invalid date format", invalidDate, eventDetail.date)
            assertEquals("Should preserve invalid time format", invalidTime, eventDetail.time)
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithRepositoryException_ShouldReturnErrorState() = runTest {
        // Arrange
        val eventId = "error-event"
        val exception = RuntimeException("Network timeout")
        val errorResult = ResultState.Error(exception)
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(errorResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Error result", result is ResultState.Error)
            val error = result as ResultState.Error
            assertEquals("Should preserve error message", "Network timeout", error.getErrorMessage())
            
            awaitComplete()
        }
    }

    @Test
    fun invoke_WithAllOptionalFieldsNull_ShouldHandleGracefully() = runTest {
        // Arrange
        val eventId = "minimal-event"
        val rawEventDetail = EventDetail(
            id = eventId,
            name = "Minimal Event",
            imageUrl = null,
            date = "2024-07-04",
            time = "16:00:00",
            venue = null,
            info = null,
            seatmapUrl = null,
            price = null,
            products = emptyList(),
            genre = null,
            ticketLimit = null,
            ageRestrictions = null,
            ticketUrl = null
        )
        val repositoryResult = ResultState.Success(rawEventDetail)
        
        every { mockRepository.getEventDetails(eventId) } returns flowOf(repositoryResult)

        // Act & Assert
        useCase.invoke(eventId).test {
            val result = awaitItem()
            
            assertTrue("Expected Success result", result is ResultState.Success)
            val eventDetail = (result as ResultState.Success).data
            assertEquals("Should format date correctly", "Thu, 4 July", eventDetail.date)
            assertEquals("Should format time correctly", "4:00 pm", eventDetail.time)
            assertNull("Should preserve null venue", eventDetail.venue)
            
            awaitComplete()
        }
    }

    private fun createValidEventDetail(): EventDetail {
        return EventDetail(
            id = "test-event-123",
            name = "Test Event",
            imageUrl = "https://example.com/image.jpg",
            date = "2024-12-25",
            time = "19:00:00",
            venue = Venue(
                name = "Test Venue",
                city = "Test City",
                state = "TS",
                address = "123 Test St"
            ),
            info = "Test event info",
            seatmapUrl = "https://example.com/seatmap",
            price = "$50.00",
            products = listOf("VIP", "General"),
            genre = "Concert",
            ticketLimit = "8",
            ageRestrictions = "18+",
            ticketUrl = "https://example.com/tickets"
        )
    }
}