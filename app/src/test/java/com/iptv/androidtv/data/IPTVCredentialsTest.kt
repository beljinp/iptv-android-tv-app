package com.iptv.androidtv.data

import org.junit.Test
import org.junit.Assert.*

class IPTVCredentialsTest {

    @Test
    fun `buildBaseUrl creates correct URL format`() {
        val credentials = IPTVCredentials(
            host = "http://starshare.net:80",
            username = "testuser",
            password = "testpass"
        )
        
        val expected = "http://starshare.net:80/get.php?username=testuser&password=testpass"
        assertEquals(expected, credentials.buildBaseUrl())
    }

    @Test
    fun `buildBaseUrl removes trailing slash from host`() {
        val credentials = IPTVCredentials(
            host = "http://starshare.net:80/",
            username = "testuser",
            password = "testpass"
        )
        
        val expected = "http://starshare.net:80/get.php?username=testuser&password=testpass"
        assertEquals(expected, credentials.buildBaseUrl())
    }

    @Test
    fun `buildPlaylistUrl includes correct parameters`() {
        val credentials = IPTVCredentials(
            host = "http://starshare.net:80",
            username = "testuser",
            password = "testpass"
        )
        
        val result = credentials.buildPlaylistUrl()
        assertTrue(result.contains("type=m3u_plus"))
        assertTrue(result.contains("output=ts"))
        assertTrue(result.contains("username=testuser"))
        assertTrue(result.contains("password=testpass"))
    }

    @Test
    fun `isValidHost returns true for valid HTTP URL`() {
        val credentials = IPTVCredentials(
            host = "http://starshare.net:80",
            username = "testuser",
            password = "testpass"
        )
        
        assertTrue(credentials.isValidHost())
    }

    @Test
    fun `isValidHost returns true for valid HTTPS URL`() {
        val credentials = IPTVCredentials(
            host = "https://starshare.net:80",
            username = "testuser",
            password = "testpass"
        )
        
        assertTrue(credentials.isValidHost())
    }

    @Test
    fun `isValidHost returns false for invalid URL`() {
        val credentials = IPTVCredentials(
            host = "not-a-url",
            username = "testuser",
            password = "testpass"
        )
        
        assertFalse(credentials.isValidHost())
    }

    @Test
    fun `isValid returns true for complete valid credentials`() {
        val credentials = IPTVCredentials(
            host = "http://starshare.net:80",
            username = "testuser",
            password = "testpass"
        )
        
        assertTrue(credentials.isValid())
    }

    @Test
    fun `isValid returns false for empty username`() {
        val credentials = IPTVCredentials(
            host = "http://starshare.net:80",
            username = "",
            password = "testpass"
        )
        
        assertFalse(credentials.isValid())
    }

    @Test
    fun `isValid returns false for empty password`() {
        val credentials = IPTVCredentials(
            host = "http://starshare.net:80",
            username = "testuser",
            password = ""
        )
        
        assertFalse(credentials.isValid())
    }

    @Test
    fun `isValid returns false for invalid host`() {
        val credentials = IPTVCredentials(
            host = "invalid-host",
            username = "testuser",
            password = "testpass"
        )
        
        assertFalse(credentials.isValid())
    }
}