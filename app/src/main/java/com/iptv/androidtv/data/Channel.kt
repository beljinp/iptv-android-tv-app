package com.iptv.androidtv.data

/**
 * Data class representing a TV channel
 */
data class Channel(
    val id: String,
    val name: String,
    val number: Int? = null,
    val groupTitle: String? = null,
    val logoUrl: String? = null,
    val streamUrl: String
) : MediaItem {
    
    override fun getDisplayTitle(): String = name
    
    override fun getDisplaySubtitle(): String? = number?.let { "Channel $it" }
    
    override fun getImageUrl(): String? = logoUrl
    
    override fun getPlaybackUrl(): String = streamUrl
    
    override fun getCategory(): MediaCategory = MediaCategory.TV_CHANNELS

    /**
     * Creates a formatted display name with channel number if available
     */
    fun getFormattedName(): String {
        return if (number != null) {
            "$number. $name"
        } else {
            name
        }
    }

    /**
     * Validates that the channel has required fields
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && 
               name.isNotBlank() && 
               streamUrl.isNotBlank()
    }
}