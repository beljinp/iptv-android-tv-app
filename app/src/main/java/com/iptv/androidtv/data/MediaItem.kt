package com.iptv.androidtv.data

/**
 * Interface for all media items (channels and movies)
 */
interface MediaItem {
    /**
     * Gets the primary display title
     */
    fun getDisplayTitle(): String
    
    /**
     * Gets the secondary display text (subtitle)
     */
    fun getDisplaySubtitle(): String?
    
    /**
     * Gets the image URL for thumbnails/logos
     */
    fun getImageUrl(): String?
    
    /**
     * Gets the URL for playback
     */
    fun getPlaybackUrl(): String
    
    /**
     * Gets the media category
     */
    fun getCategory(): MediaCategory
}