package com.iptv.androidtv.service

import com.iptv.androidtv.data.IPTVCredentials
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class IPTVServiceTest {

    private lateinit var iptvService: IPTVService
    
    private val validCredentials = IPTVCredentials(
        host = "http://starshare.net:80",
        username = "testuser",
        password = "testpass"
    )

    private val invalidCredentials = IPTVCredentials(
        host = "",
        username = "",
        password = ""
    )

    @Before
    fun setup() {
        iptvService = IPTVService()
    }

    @Test
    fun `connect returns error for invalid credentials`() = runTest {
        val result = iptvService.connect(invalidCredentials)
        
        assertTrue(result is IPTVService.ServiceResult.Error)
        val error = result as IPTVService.ServiceResult.Error
        assertTrue(error.message.contains("Invalid credentials"))
    }

    @Test
    fun `buildAuthenticatedStreamUrl adds credentials to URL without query params`() {
        val streamUrl = "http://stream.example.com/channel1"
        val result = iptvService.buildAuthenticatedStreamUrl(validCredentials, streamUrl)
        
        assertEquals("http://stream.example.com/channel1?username=testuser&password=testpass", result)
    }

    @Test
    fun `buildAuthenticatedStreamUrl adds credentials to URL with existing query params`() {
        val streamUrl = "http://stream.example.com/channel1?format=ts"
        val result = iptvService.buildAuthenticatedStreamUrl(validCredentials, streamUrl)
        
        assertEquals("http://stream.example.com/channel1?format=ts&username=testuser&password=testpass", result)
    }

    @Test
    fun `ContentData calculates total items correctly`() {
        val channels = listOf(
            createTestChannel("1", "Channel 1"),
            createTestChannel("2", "Channel 2")
        )
        val movies = listOf(
            createTestMovie("1", "Movie 1"),
            createTestMovie("2", "Movie 2"),
            createTestMovie("3", "Movie 3")
        )
        
        val contentData = IPTVService.ContentData(channels, movies)
        
        assertEquals(5, contentData.totalItems)
        assertEquals(2, contentData.channels.size)
        assertEquals(3, contentData.movies.size)
    }

    @Test
    fun `ConnectionStatus creates with correct default values`() {
        val status = IPTVService.ConnectionStatus(
            isConnected = true,
            serverUrl = "http://test.com"
        )
        
        assertTrue(status.isConnected)
        assertEquals("http://test.com", status.serverUrl)
        assertNull(status.errorMessage)
        assertTrue(status.lastChecked > 0)
    }

    @Test
    fun `ServiceResult Success contains correct data`() {
        val testData = "test data"
        val result = IPTVService.ServiceResult.Success(testData)
        
        assertEquals(testData, result.data)
    }

    @Test
    fun `ServiceResult Error contains message and exception`() {
        val exception = RuntimeException("Test exception")
        val result = IPTVService.ServiceResult.Error<String>("Test error", exception)
        
        assertEquals("Test error", result.message)
        assertEquals(exception, result.exception)
    }

    @Test
    fun `close method completes without error`() {
        // Should not throw any exceptions
        iptvService.close()
    }

    // Helper methods for creating test data
    private fun createTestChannel(id: String, name: String) = 
        com.iptv.androidtv.data.Channel(
            id = id,
            name = name,
            streamUrl = "http://test.com/stream$id"
        )

    private fun createTestMovie(id: String, title: String) = 
        com.iptv.androidtv.data.Movie(
            id = id,
            title = title,
            streamUrl = "http://test.com/movie$id"
        )
}