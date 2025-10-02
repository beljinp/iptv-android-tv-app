package com.iptv.androidtv.data

/**
 * Data class representing a movie
 */
data class Movie(
    val id: String,
    val title: String,
    val description: String? = null,
    val posterUrl: String? = null,
    val streamUrl: String,
    val duration: Long? = null,
    val year: Int? = null,
    val genre: String? = null
) : MediaItem {
    
    override fun getDisplayTitle(): String = title
    
    override fun getDisplaySubtitle(): String? {
        return when {
            year != null && genre != null -> "$year • $genre"
            year != null -> year.toString()
            genre != null -> genre
            else -> null
        }
    }
    
    override fun getImageUrl(): String? = posterUrl
    
    override fun getPlaybackUrl(): String = streamUrl
    
    override fun getCategory(): MediaCategory = MediaCategory.MOVIES

    /**
     * Gets formatted duration string (e.g., "1h 30m")
     */
    fun getFormattedDuration(): String? {
        return duration?.let { durationMs ->
            val hours = durationMs / 3600000
            val minutes = (durationMs % 3600000) / 60000
            
            when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> null
            }
        }
    }

    /**
     * Validates that the movie has required fields
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && 
               title.isNotBlank() && 
               streamUrl.isNotBlank()
    }
}