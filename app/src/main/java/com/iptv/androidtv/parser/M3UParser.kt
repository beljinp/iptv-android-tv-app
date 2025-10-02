package com.iptv.androidtv.parser

import com.iptv.androidtv.data.Channel
import com.iptv.androidtv.data.Movie
import com.iptv.androidtv.data.MediaItem
import java.util.regex.Pattern

/**
 * Parser for M3U playlist files to extract channels and movies
 */
class M3UParser {
    
    companion object {
        private val EXTINF_PATTERN = Pattern.compile(
            "#EXTINF:(-?\\d+(?:\\.\\d+)?),?(.*)$"
        )
        
        private val TVG_INFO_PATTERN = Pattern.compile(
            "tvg-id=\"([^\"]*)\"|tvg-name=\"([^\"]*)\"|tvg-logo=\"([^\"]*)\"|group-title=\"([^\"]*)\""
        )
        
        private val CHANNEL_NUMBER_PATTERN = Pattern.compile(
            "^(\\d+)[.\\s]*(.+)$"
        )
        
        // Common movie indicators in group titles or names
        private val MOVIE_INDICATORS = setOf(
            "movie", "movies", "film", "films", "cinema", "vod", 
            "on demand", "series", "tv shows", "shows"
        )
    }

    /**
     * Result of M3U parsing operation
     */
    data class ParseResult(
        val channels: List<Channel>,
        val movies: List<Movie>,
        val errors: List<String> = emptyList()
    )

    /**
     * Parses M3U playlist content and extracts channels and movies
     * @param content The M3U playlist content
     * @return ParseResult containing separated channels and movies
     */
    fun parse(content: String): ParseResult {
        val lines = content.lines()
        val channels = mutableListOf<Channel>()
        val movies = mutableListOf<Movie>()
        val errors = mutableListOf<String>()
        
        var currentExtinf: ExtinfInfo? = null
        var lineNumber = 0
        
        for (line in lines) {
            lineNumber++
            val trimmedLine = line.trim()
            
            when {
                trimmedLine.startsWith("#EXTINF:") -> {
                    currentExtinf = parseExtinf(trimmedLine, lineNumber)
                    if (currentExtinf == null) {
                        errors.add("Line $lineNumber: Invalid EXTINF format")
                    }
                }
                
                trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#") -> {
                    // This should be a stream URL
                    if (currentExtinf != null) {
                        try {
                            val mediaItem = createMediaItem(currentExtinf, trimmedLine)
                            when (mediaItem) {
                                is Channel -> channels.add(mediaItem)
                                is Movie -> movies.add(mediaItem)
                            }
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: Error creating media item - ${e.message}")
                        }
                        currentExtinf = null
                    } else {
                        errors.add("Line $lineNumber: Stream URL without EXTINF")
                    }
                }
            }
        }
        
        return ParseResult(
            channels = channels.sortedBy { it.number ?: Int.MAX_VALUE },
            movies = movies.sortedBy { it.title },
            errors = errors
        )
    }

    /**
     * Parses EXTINF line to extract metadata
     */
    private fun parseExtinf(line: String, lineNumber: Int): ExtinfInfo? {
        val matcher = EXTINF_PATTERN.matcher(line)
        if (!matcher.find()) {
            return null
        }
        
        val duration = matcher.group(1)?.toDoubleOrNull() ?: -1.0
        val info = matcher.group(2) ?: ""
        
        // Extract TVG attributes and title
        val attributes = extractTvgAttributes(info)
        val title = extractTitle(info)
        
        return ExtinfInfo(
            duration = duration,
            title = title,
            tvgId = attributes["tvg-id"],
            tvgName = attributes["tvg-name"],
            tvgLogo = attributes["tvg-logo"],
            groupTitle = attributes["group-title"],
            lineNumber = lineNumber
        )
    }

    /**
     * Extracts TVG attributes from EXTINF info
     */
    private fun extractTvgAttributes(info: String): Map<String, String> {
        val attributes = mutableMapOf<String, String>()
        val matcher = TVG_INFO_PATTERN.matcher(info)
        
        while (matcher.find()) {
            when {
                matcher.group(1) != null -> attributes["tvg-id"] = matcher.group(1)
                matcher.group(2) != null -> attributes["tvg-name"] = matcher.group(2)
                matcher.group(3) != null -> attributes["tvg-logo"] = matcher.group(3)
                matcher.group(4) != null -> attributes["group-title"] = matcher.group(4)
            }
        }
        
        return attributes
    }

    /**
     * Extracts title from EXTINF info by removing attributes
     */
    private fun extractTitle(info: String): String {
        // Remove all TVG attributes to get the clean title
        var title = info
        val matcher = TVG_INFO_PATTERN.matcher(info)
        
        while (matcher.find()) {
            title = title.replace(matcher.group(0), "")
        }
        
        return title.trim().replace(Regex("\\s+"), " ")
    }

    /**
     * Creates appropriate MediaItem (Channel or Movie) based on metadata
     */
    private fun createMediaItem(extinf: ExtinfInfo, streamUrl: String): MediaItem {
        val isMovie = isMovieContent(extinf)
        
        return if (isMovie) {
            createMovie(extinf, streamUrl)
        } else {
            createChannel(extinf, streamUrl)
        }
    }

    /**
     * Determines if content is a movie based on group title and name
     */
    private fun isMovieContent(extinf: ExtinfInfo): Boolean {
        val groupTitle = extinf.groupTitle?.lowercase() ?: ""
        val title = extinf.title.lowercase()
        
        return MOVIE_INDICATORS.any { indicator ->
            groupTitle.contains(indicator) || title.contains(indicator)
        }
    }

    /**
     * Creates a Channel object from EXTINF info
     */
    private fun createChannel(extinf: ExtinfInfo, streamUrl: String): Channel {
        val (channelNumber, cleanName) = extractChannelNumber(extinf.title)
        
        return Channel(
            id = extinf.tvgId ?: generateId(extinf.title),
            name = cleanName,
            number = channelNumber,
            groupTitle = extinf.groupTitle,
            logoUrl = extinf.tvgLogo,
            streamUrl = streamUrl
        )
    }

    /**
     * Creates a Movie object from EXTINF info
     */
    private fun createMovie(extinf: ExtinfInfo, streamUrl: String): Movie {
        val duration = if (extinf.duration > 0) (extinf.duration * 1000).toLong() else null
        
        return Movie(
            id = extinf.tvgId ?: generateId(extinf.title),
            title = extinf.title,
            description = extinf.groupTitle,
            posterUrl = extinf.tvgLogo,
            streamUrl = streamUrl,
            duration = duration
        )
    }

    /**
     * Extracts channel number from title if present
     */
    private fun extractChannelNumber(title: String): Pair<Int?, String> {
        val matcher = CHANNEL_NUMBER_PATTERN.matcher(title.trim())
        
        return if (matcher.find()) {
            val number = matcher.group(1)?.toIntOrNull()
            val name = matcher.group(2)?.trim() ?: title
            Pair(number, name)
        } else {
            Pair(null, title)
        }
    }

    /**
     * Generates a unique ID from title
     */
    private fun generateId(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .take(50)
    }

    /**
     * Data class holding parsed EXTINF information
     */
    private data class ExtinfInfo(
        val duration: Double,
        val title: String,
        val tvgId: String?,
        val tvgName: String?,
        val tvgLogo: String?,
        val groupTitle: String?,
        val lineNumber: Int
    )
}