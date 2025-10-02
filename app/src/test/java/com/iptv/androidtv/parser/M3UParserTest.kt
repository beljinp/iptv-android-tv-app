package com.iptv.androidtv.parser

import com.iptv.androidtv.data.MediaCategory
import org.junit.Test
import org.junit.Assert.*

class M3UParserTest {

    private val parser = M3UParser()

    private val sampleM3U = """
        #EXTM3U
        #EXTINF:-1 tvg-id="cnn" tvg-name="CNN" tvg-logo="http://example.com/cnn.png" group-title="News",CNN International
        http://stream.example.com/cnn
        #EXTINF:-1 tvg-id="bbc" tvg-name="BBC" tvg-logo="http://example.com/bbc.png" group-title="News",1. BBC World News
        http://stream.example.com/bbc
        #EXTINF:-1 tvg-id="movie1" tvg-name="Action Movie" tvg-logo="http://example.com/movie1.jpg" group-title="Movies",The Matrix
        http://stream.example.com/matrix
        #EXTINF:-1 tvg-id="sports1" tvg-name="ESPN" tvg-logo="http://example.com/espn.png" group-title="Sports",101. ESPN
        http://stream.example.com/espn
    """.trimIndent()

    @Test
    fun `parse extracts channels and movies correctly`() {
        val result = parser.parse(sampleM3U)
        
        assertTrue(result.channels.isNotEmpty())
        assertTrue(result.movies.isNotEmpty())
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `parse identifies movies by group title`() {
        val result = parser.parse(sampleM3U)
        
        val movies = result.movies
        assertTrue(movies.any { it.title == "The Matrix" })
        assertEquals(MediaCategory.MOVIES, movies.first().getCategory())
    }

    @Test
    fun `parse identifies channels correctly`() {
        val result = parser.parse(sampleM3U)
        
        val channels = result.channels
        assertTrue(channels.any { it.name == "CNN International" })
        assertTrue(channels.any { it.name == "BBC World News" })
        assertEquals(MediaCategory.TV_CHANNELS, channels.first().getCategory())
    }

    @Test
    fun `parse extracts channel numbers correctly`() {
        val result = parser.parse(sampleM3U)
        
        val bbcChannel = result.channels.find { it.name == "BBC World News" }
        assertNotNull(bbcChannel)
        assertEquals(1, bbcChannel?.number)
        
        val espnChannel = result.channels.find { it.name == "ESPN" }
        assertNotNull(espnChannel)
        assertEquals(101, espnChannel?.number)
    }

    @Test
    fun `parse extracts TVG attributes correctly`() {
        val result = parser.parse(sampleM3U)
        
        val cnnChannel = result.channels.find { it.name == "CNN International" }
        assertNotNull(cnnChannel)
        assertEquals("cnn", cnnChannel?.id)
        assertEquals("http://example.com/cnn.png", cnnChannel?.logoUrl)
        assertEquals("News", cnnChannel?.groupTitle)
    }

    @Test
    fun `parse handles malformed EXTINF lines`() {
        val malformedM3U = """
            #EXTM3U
            #EXTINF:invalid line
            http://stream.example.com/test
            #EXTINF:-1,Valid Channel
            http://stream.example.com/valid
        """.trimIndent()
        
        val result = parser.parse(malformedM3U)
        
        assertEquals(1, result.channels.size)
        assertEquals("Valid Channel", result.channels.first().name)
        assertTrue(result.errors.isNotEmpty())
    }

    @Test
    fun `parse handles stream URLs without EXTINF`() {
        val invalidM3U = """
            #EXTM3U
            http://stream.example.com/orphan
            #EXTINF:-1,Valid Channel
            http://stream.example.com/valid
        """.trimIndent()
        
        val result = parser.parse(invalidM3U)
        
        assertEquals(1, result.channels.size)
        assertTrue(result.errors.any { it.contains("Stream URL without EXTINF") })
    }

    @Test
    fun `parse sorts channels by number`() {
        val unsortedM3U = """
            #EXTM3U
            #EXTINF:-1,101. Channel 101
            http://stream.example.com/101
            #EXTINF:-1,5. Channel 5
            http://stream.example.com/5
            #EXTINF:-1,50. Channel 50
            http://stream.example.com/50
        """.trimIndent()
        
        val result = parser.parse(unsortedM3U)
        
        assertEquals(3, result.channels.size)
        assertEquals(5, result.channels[0].number)
        assertEquals(50, result.channels[1].number)
        assertEquals(101, result.channels[2].number)
    }

    @Test
    fun `parse handles channels without numbers`() {
        val noNumberM3U = """
            #EXTM3U
            #EXTINF:-1,Channel Without Number
            http://stream.example.com/no-number
            #EXTINF:-1,1. Channel With Number
            http://stream.example.com/with-number
        """.trimIndent()
        
        val result = parser.parse(noNumberM3U)
        
        assertEquals(2, result.channels.size)
        
        val withNumber = result.channels.find { it.number != null }
        val withoutNumber = result.channels.find { it.number == null }
        
        assertNotNull(withNumber)
        assertNotNull(withoutNumber)
        assertEquals("Channel With Number", withNumber?.name)
        assertEquals("Channel Without Number", withoutNumber?.name)
    }

    @Test
    fun `parse handles empty content`() {
        val result = parser.parse("")
        
        assertTrue(result.channels.isEmpty())
        assertTrue(result.movies.isEmpty())
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `parse handles content with only comments`() {
        val commentOnlyM3U = """
            #EXTM3U
            # This is a comment
            # Another comment
        """.trimIndent()
        
        val result = parser.parse(commentOnlyM3U)
        
        assertTrue(result.channels.isEmpty())
        assertTrue(result.movies.isEmpty())
        assertTrue(result.errors.isEmpty())
    }
}