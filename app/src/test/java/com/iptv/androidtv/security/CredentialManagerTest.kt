package com.iptv.androidtv.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iptv.androidtv.data.IPTVCredentials
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CredentialManagerTest {

    private lateinit var credentialManager: CredentialManager
    private lateinit var context: Context

    private val validCredentials = IPTVCredentials(
        host = "http://starshare.net:80",
        username = "testuser",
        password = "testpass"
    )

    private val invalidCredentials = IPTVCredentials(
        host = "invalid-url",
        username = "",
        password = "testpass"
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        credentialManager = CredentialManager(context)
        // Clear any existing credentials
        credentialManager.clearCredentials()
    }

    @Test
    fun `hasCredentials returns false initially`() {
        assertFalse(credentialManager.hasCredentials())
    }

    @Test
    fun `saveCredentials stores valid credentials successfully`() {
        val result = credentialManager.saveCredentials(validCredentials)
        
        assertTrue(result.isSuccess)
        assertTrue(credentialManager.hasCredentials())
    }

    @Test
    fun `saveCredentials fails for invalid credentials`() {
        val result = credentialManager.saveCredentials(invalidCredentials)
        
        assertTrue(result.isFailure)
        assertFalse(credentialManager.hasCredentials())
    }

    @Test
    fun `getCredentials returns null when no credentials stored`() {
        val credentials = credentialManager.getCredentials()
        
        assertNull(credentials)
    }

    @Test
    fun `getCredentials returns stored credentials`() {
        credentialManager.saveCredentials(validCredentials)
        
        val retrievedCredentials = credentialManager.getCredentials()
        
        assertNotNull(retrievedCredentials)
        assertEquals(validCredentials.host, retrievedCredentials?.host)
        assertEquals(validCredentials.username, retrievedCredentials?.username)
        assertEquals(validCredentials.password, retrievedCredentials?.password)
    }

    @Test
    fun `clearCredentials removes stored credentials`() {
        credentialManager.saveCredentials(validCredentials)
        assertTrue(credentialManager.hasCredentials())
        
        val result = credentialManager.clearCredentials()
        
        assertTrue(result.isSuccess)
        assertFalse(credentialManager.hasCredentials())
        assertNull(credentialManager.getCredentials())
    }

    @Test
    fun `updateCredentials replaces existing credentials`() {
        credentialManager.saveCredentials(validCredentials)
        
        val newCredentials = IPTVCredentials(
            host = "http://newserver.com:80",
            username = "newuser",
            password = "newpass"
        )
        
        val result = credentialManager.updateCredentials(newCredentials)
        
        assertTrue(result.isSuccess)
        
        val retrievedCredentials = credentialManager.getCredentials()
        assertEquals(newCredentials.host, retrievedCredentials?.host)
        assertEquals(newCredentials.username, retrievedCredentials?.username)
        assertEquals(newCredentials.password, retrievedCredentials?.password)
    }

    @Test
    fun `areStoredCredentialsValid returns true for valid stored credentials`() {
        credentialManager.saveCredentials(validCredentials)
        
        assertTrue(credentialManager.areStoredCredentialsValid())
    }

    @Test
    fun `areStoredCredentialsValid returns false when no credentials stored`() {
        assertFalse(credentialManager.areStoredCredentialsValid())
    }

    @Test
    fun `multiple save and retrieve operations work correctly`() {
        // Save first set
        credentialManager.saveCredentials(validCredentials)
        val first = credentialManager.getCredentials()
        assertEquals(validCredentials.username, first?.username)
        
        // Update with new credentials
        val newCredentials = IPTVCredentials(
            host = "http://another.server:80",
            username = "anotheruser",
            password = "anotherpass"
        )
        credentialManager.updateCredentials(newCredentials)
        
        val second = credentialManager.getCredentials()
        assertEquals(newCredentials.username, second?.username)
        assertNotEquals(validCredentials.username, second?.username)
    }
}