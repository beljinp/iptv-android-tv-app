package com.iptv.androidtv.data

import org.junit.Test
import org.junit.Assert.*

class ChannelTest {

    @Test
    fun `getFormattedName includes channel number when available`() {
        val channel = Channel(
            id = "1",
            name = "Test Channel",
            number = 101,
            streamUrl = "http://test.com/stream"
        )
        
        assertEquals("101. Test Channel", channel.getFormattedName())
    }

    @Test
    fun `getFormattedName returns name only when no number`() {
        val channel = Channel(
            id = "1",
            name = "Test Channel",
            number = null,
            streamUrl = "http://test.com/stream"
        )
        
        assertEquals("Test Channel", channel.getFormattedName())
    }

    @Test
    fun `getDisplayTitle returns channel name`() {
        val channel = Channel(
            id = "1",
            name = "Test Channel",
            streamUrl = "http://test.com/stream"
        )
        
        assertEquals("Test Channel", channel.getDisplayTitle())
    }

    @Test
    fun `getDisplaySubtitle returns channel number format`() {
        val channel = Channel(
            id = "1",
            name = "Test Channel",
            number = 101,
            streamUrl = "http://test.com/stream"
        )
        
        assertEquals("Channel 101", channel.getDisplaySubtitle())
    }

    @Test
    fun `getDisplaySubtitle returns null when no number`() {
        val channel = Channel(
            id = "1",
            name = "Test Channel",
            number = null,
            streamUrl = "http://test.com/stream"
        )
        
        assertNull(channel.getDisplaySubtitle())
    }

    @Test
    fun `getCategory returns TV_CHANNELS`() {
        val channel = Channel(
            id = "1",
            name = "Test Channel",
            streamUrl = "http://test.com/stream"
        )
        
        assertEquals(MediaCategory.TV_CHANNELS, channel.getCategory())
    }

    @Test
    fun `isValid returns true for valid channel`() {
        val channel = Channel(
            id = "1",
            name = "Test Channel",
            streamUrl = "http://test.com/stream"
        )
        
        assertTrue(channel.isValid())
    }

    @Test
    fun `isValid returns false for empty id`() {
        val channel = Channel(
            id = "",
            name = "Test Channel",
            streamUrl = "http://test.com/stream"
        )
        
        assertFalse(channel.isValid())
    }

    @Test
    fun `isValid returns false for empty name`() {
        val channel = Channel(
            id = "1",
            name = "",
            streamUrl = "http://test.com/stream"
        )
        
        assertFalse(channel.isValid())
    }

    @Test
    fun `isValid returns false for empty streamUrl`() {
        val channel = Channel(
            id = "1",
            name = "Test Channel",
            streamUrl = ""
        )
        
        assertFalse(channel.isValid())
    }
}