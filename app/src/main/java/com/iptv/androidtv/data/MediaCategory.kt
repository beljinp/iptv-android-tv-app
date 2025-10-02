package com.iptv.androidtv.data

/**
 * Enum representing different types of media content
 */
enum class MediaCategory(val displayName: String) {
    TV_CHANNELS("TV Channels"),
    MOVIES("Movies");

    companion object {
        fun fromString(value: String): MediaCategory? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}