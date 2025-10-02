package com.iptv.androidtv.data

import org.junit.Test
import org.junit.Assert.*

class MovieTest {

    @Test
    fun `getDisplaySubtitle returns year and genre when both available`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = "http://test.com/stream",
            year = 2023,
            genre = "Action"
        )
        
        assertEquals("2023 • Action", movie.getDisplaySubtitle())
    }

    @Test
    fun `getDisplaySubtitle returns year only when genre not available`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = "http://test.com/stream",
            year = 2023,
            genre = null
        )
        
        assertEquals("2023", movie.getDisplaySubtitle())
    }

    @Test
    fun `getDisplaySubtitle returns genre only when year not available`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = "http://test.com/stream",
            year = null,
            genre = "Action"
        )
        
        assertEquals("Action", movie.getDisplaySubtitle())
    }

    @Test
    fun `getDisplaySubtitle returns null when neither year nor genre available`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = "http://test.com/stream",
            year = null,
            genre = null
        )
        
        assertNull(movie.getDisplaySubtitle())
    }

    @Test
    fun `getFormattedDuration returns hours and minutes`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = "http://test.com/stream",
            duration = 5400000L // 1.5 hours in milliseconds
        )
        
        assertEquals("1h 30m", movie.getFormattedDuration())
    }

    @Test
    fun `getFormattedDuration returns minutes only for short duration`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = "http://test.com/stream",
            duration = 1800000L // 30 minutes in milliseconds
        )
        
        assertEquals("30m", movie.getFormattedDuration())
    }

    @Test
    fun `getFormattedDuration returns null for zero duration`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = "http://test.com/stream",
            duration = 0L
        )
        
        assertNull(movie.getFormattedDuration())
    }

    @Test
    fun `getCategory returns MOVIES`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = "http://test.com/stream"
        )
        
        assertEquals(MediaCategory.MOVIES, movie.getCategory())
    }

    @Test
    fun `isValid returns true for valid movie`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = "http://test.com/stream"
        )
        
        assertTrue(movie.isValid())
    }

    @Test
    fun `isValid returns false for empty id`() {
        val movie = Movie(
            id = "",
            title = "Test Movie",
            streamUrl = "http://test.com/stream"
        )
        
        assertFalse(movie.isValid())
    }

    @Test
    fun `isValid returns false for empty title`() {
        val movie = Movie(
            id = "1",
            title = "",
            streamUrl = "http://test.com/stream"
        )
        
        assertFalse(movie.isValid())
    }

    @Test
    fun `isValid returns false for empty streamUrl`() {
        val movie = Movie(
            id = "1",
            title = "Test Movie",
            streamUrl = ""
        )
        
        assertFalse(movie.isValid())
    }
}