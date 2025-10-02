package com.iptv.androidtv.integration

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class VLCIntegrationManagerTest {

    private lateinit var context: Context
    private lateinit var vlcManager: VLCIntegrationManager
    private lateinit var mockPackageManager: PackageManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockPackageManager = mock(PackageManager::class.java)
        vlcManager = VLCIntegrationManager(context)
    }

    @Test
    fun `isValidStreamUrl returns true for valid HTTP URL`() {
        assertTrue(vlcManager.isValidStreamUrl("http://example.com/stream.m3u8"))
    }

    @Test
    fun `isValidStreamUrl returns true for valid HTTPS URL`() {
        assertTrue(vlcManager.isValidStreamUrl("https://example.com/stream.ts"))
    }

    @Test
    fun `isValidStreamUrl returns true for RTMP URL`() {
        assertTrue(vlcManager.isValidStreamUrl("rtmp://example.com/live/stream"))
    }

    @Test
    fun `isValidStreamUrl returns true for RTSP URL`() {
        assertTrue(vlcManager.isValidStreamUrl("rtsp://example.com:554/stream"))
    }

    @Test
    fun `isValidStreamUrl returns false for empty URL`() {
        assertFalse(vlcManager.isValidStreamUrl(""))
    }

    @Test
    fun `isValidStreamUrl returns false for blank URL`() {
        assertFalse(vlcManager.isValidStreamUrl("   "))
    }

    @Test
    fun `isValidStreamUrl returns false for invalid scheme`() {
        assertFalse(vlcManager.isValidStreamUrl("ftp://example.com/stream"))
    }

    @Test
    fun `isValidStreamUrl returns false for malformed URL`() {
        assertFalse(vlcManager.isValidStreamUrl("not-a-url"))
    }

    @Test
    fun `createVLCIntent creates proper intent for stream`() {
        val streamUrl = "http://example.com/stream.m3u8"
        val title = "Test Stream"
        
        val intent = vlcManager.createVLCIntent(streamUrl, title)
        
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(Uri.parse(streamUrl), intent.data)
        assertEquals("video/*", intent.type)
        assertEquals(title, intent.getStringExtra("title"))
        assertEquals(title, intent.getStringExtra("itemTitle"))
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `createVLCIntent creates intent without title`() {
        val streamUrl = "http://example.com/stream.m3u8"
        
        val intent = vlcManager.createVLCIntent(streamUrl)
        
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(Uri.parse(streamUrl), intent.data)
        assertNull(intent.getStringExtra("title"))
    }

    @Test
    fun `createVLCIntent sets package when provided`() {
        val streamUrl = "http://example.com/stream.m3u8"
        val packageName = "org.videolan.vlc"
        
        val intent = vlcManager.createVLCIntent(streamUrl, packageName = packageName)
        
        assertEquals(packageName, intent.`package`)
    }

    @Test
    fun `getSupportedFormats returns expected formats`() {
        val formats = vlcManager.getSupportedFormats()
        
        assertTrue(formats.contains("mp4"))
        assertTrue(formats.contains("m3u8"))
        assertTrue(formats.contains("ts"))
        assertTrue(formats.contains("rtmp"))
        assertTrue(formats.contains("http"))
        assertTrue(formats.contains("https"))
    }

    @Test
    fun `isFormatSupported returns true for supported formats`() {
        assertTrue(vlcManager.isFormatSupported("stream.mp4"))
        assertTrue(vlcManager.isFormatSupported("playlist.m3u8"))
        assertTrue(vlcManager.isFormatSupported("http://example.com/stream"))
        assertTrue(vlcManager.isFormatSupported("rtmp://server/stream"))
    }

    @Test
    fun `isFormatSupported returns false for unsupported formats`() {
        assertFalse(vlcManager.isFormatSupported("document.pdf"))
        assertFalse(vlcManager.isFormatSupported("image.jpg"))
        assertFalse(vlcManager.isFormatSupported("audio.mp3"))
    }

    @Test
    fun `isFormatSupported handles case insensitive matching`() {
        assertTrue(vlcManager.isFormatSupported("STREAM.MP4"))
        assertTrue(vlcManager.isFormatSupported("Playlist.M3U8"))
        assertTrue(vlcManager.isFormatSupported("HTTP://EXAMPLE.COM/STREAM"))
    }

    @Test
    fun `VLCResult Success is properly created`() {
        val result = VLCIntegrationManager.VLCResult.Success
        assertTrue(result is VLCIntegrationManager.VLCResult.Success)
    }

    @Test
    fun `VLCResult Error contains message and install flag`() {
        val result = VLCIntegrationManager.VLCResult.Error("Test error", true)
        
        assertTrue(result is VLCIntegrationManager.VLCResult.Error)
        assertEquals("Test error", result.message)
        assertTrue(result.canInstall)
    }

    @Test
    fun `VLCResult Error defaults canInstall to false`() {
        val result = VLCIntegrationManager.VLCResult.Error("Test error")
        
        assertFalse(result.canInstall)
    }

    @Test
    fun `VLCStatus creates with correct values`() {
        val status = VLCIntegrationManager.VLCStatus(
            isInstalled = true,
            packageName = "org.videolan.vlc",
            versionName = "3.5.0",
            versionCode = 13050000L
        )
        
        assertTrue(status.isInstalled)
        assertEquals("org.videolan.vlc", status.packageName)
        assertEquals("3.5.0", status.versionName)
        assertEquals(13050000L, status.versionCode)
    }

    @Test
    fun `VLCStatus creates with minimal values`() {
        val status = VLCIntegrationManager.VLCStatus(isInstalled = false)
        
        assertFalse(status.isInstalled)
        assertNull(status.packageName)
        assertNull(status.versionName)
        assertNull(status.versionCode)
    }
}