package com.iptv.androidtv.data

import java.net.URL

/**
 * Data class representing IPTV server credentials
 */
data class IPTVCredentials(
    val host: String,
    val username: String,
    val password: String
) {
    /**
     * Builds the base URL for IPTV API calls
     */
    fun buildBaseUrl(): String {
        val cleanHost = host.removeSuffix("/")
        return "$cleanHost/get.php?username=$username&password=$password"
    }

    /**
     * Builds the M3U playlist URL
     */
    fun buildPlaylistUrl(): String {
        return "${buildBaseUrl()}&type=m3u_plus&output=ts"
    }

    /**
     * Builds the EPG URL
     */
    fun buildEpgUrl(): String {
        return "${buildBaseUrl()}&type=epg&output=xml"
    }

    /**
     * Validates the host URL format
     */
    fun isValidHost(): Boolean {
        return try {
            val url = URL(host)
            url.protocol in listOf("http", "https")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validates that all required fields are present
     */
    fun isValid(): Boolean {
        return host.isNotBlank() && 
               username.isNotBlank() && 
               password.isNotBlank() && 
               isValidHost()
    }
}