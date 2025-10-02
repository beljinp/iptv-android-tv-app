package com.iptv.androidtv.security

import com.iptv.androidtv.data.IPTVCredentials
import org.junit.Test
import org.junit.Assert.*

class CredentialValidatorTest {

    @Test
    fun `validateHost returns success for valid HTTP URL`() {
        val result = CredentialValidator.validateHost("http://starshare.net:80")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `validateHost returns success for valid HTTPS URL`() {
        val result = CredentialValidator.validateHost("https://starshare.net:80")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateHost returns error for empty host`() {
        val result = CredentialValidator.validateHost("")
        assertFalse(result.isValid)
        assertEquals("Host URL is required", result.errorMessage)
    }

    @Test
    fun `validateHost returns error for invalid URL format`() {
        val result = CredentialValidator.validateHost("not-a-url")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `validateUsername returns success for valid username`() {
        val result = CredentialValidator.validateUsername("testuser123")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateUsername returns error for empty username`() {
        val result = CredentialValidator.validateUsername("")
        assertFalse(result.isValid)
        assertEquals("Username is required", result.errorMessage)
    }

    @Test
    fun `validateUsername returns error for short username`() {
        val result = CredentialValidator.validateUsername("a")
        assertFalse(result.isValid)
        assertEquals("Username must be at least 2 characters", result.errorMessage)
    }

    @Test
    fun `validateUsername returns error for invalid characters`() {
        val result = CredentialValidator.validateUsername("user@domain")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("can only contain"))
    }

    @Test
    fun `validatePassword returns success for valid password`() {
        val result = CredentialValidator.validatePassword("validpass123")
        assertTrue(result.isValid)
    }

    @Test
    fun `validatePassword returns error for empty password`() {
        val result = CredentialValidator.validatePassword("")
        assertFalse(result.isValid)
        assertEquals("Password is required", result.errorMessage)
    }

    @Test
    fun `validatePassword returns error for short password`() {
        val result = CredentialValidator.validatePassword("ab")
        assertFalse(result.isValid)
        assertEquals("Password must be at least 3 characters", result.errorMessage)
    }

    @Test
    fun `validateCredentials returns success for valid credentials`() {
        val credentials = IPTVCredentials(
            host = "http://starshare.net:80",
            username = "testuser",
            password = "testpass"
        )
        
        val result = CredentialValidator.validateCredentials(credentials)
        assertTrue(result.isValid)
    }

    @Test
    fun `validateCredentials returns error for invalid host`() {
        val credentials = IPTVCredentials(
            host = "invalid",
            username = "testuser",
            password = "testpass"
        )
        
        val result = CredentialValidator.validateCredentials(credentials)
        assertFalse(result.isValid)
    }

    @Test
    fun `validateCredentialsDetailed returns all errors`() {
        val credentials = IPTVCredentials(
            host = "",
            username = "a",
            password = ""
        )
        
        val errors = CredentialValidator.validateCredentialsDetailed(credentials)
        assertEquals(3, errors.size)
        assertTrue(errors.any { it.contains("Host") })
        assertTrue(errors.any { it.contains("Username") })
        assertTrue(errors.any { it.contains("Password") })
    }

    @Test
    fun `sanitizeHost adds protocol when missing`() {
        val result = CredentialValidator.sanitizeHost("starshare.net:80")
        assertEquals("http://starshare.net:80", result)
    }

    @Test
    fun `sanitizeHost removes trailing slash`() {
        val result = CredentialValidator.sanitizeHost("http://starshare.net:80/")
        assertEquals("http://starshare.net:80", result)
    }

    @Test
    fun `sanitizeHost preserves existing protocol`() {
        val result = CredentialValidator.sanitizeHost("https://starshare.net:80")
        assertEquals("https://starshare.net:80", result)
    }

    @Test
    fun `createSanitizedCredentials applies all sanitization`() {
        val credentials = CredentialValidator.createSanitizedCredentials(
            host = " starshare.net:80/ ",
            username = " testuser ",
            password = " testpass "
        )
        
        assertEquals("http://starshare.net:80", credentials.host)
        assertEquals("testuser", credentials.username)
        assertEquals("testpass", credentials.password)
    }
}