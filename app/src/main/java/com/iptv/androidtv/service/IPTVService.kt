package com.iptv.androidtv.service

import com.iptv.androidtv.data.Channel
import com.iptv.androidtv.data.IPTVCredentials
import com.iptv.androidtv.data.Movie
import com.iptv.androidtv.network.HttpClient
import com.iptv.androidtv.parser.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for communicating with IPTV server and managing content
 */
class IPTVService {
    
    private val httpClient = HttpClient()
    private val m3uParser = M3UParser()
    
    /**
     * Result of IPTV service operations
     */
    sealed class ServiceResult<T> {
        data class Success<T>(val data: T) : ServiceResult<T>()
        data class Error<T>(val message: String, val exception: Throwable? = null) : ServiceResult<T>()
    }

    /**
     * Content data containing channels and movies
     */
    data class ContentData(
        val channels: List<Channel>,
        val movies: List<Movie>,
        val totalItems: Int = channels.size + movies.size
    )

    /**
     * Connection status information
     */
    data class ConnectionStatus(
        val isConnected: Boolean,
        val serverUrl: String,
        val lastChecked: Long = System.currentTimeMillis(),
        val errorMessage: String? = null
    )

    /**
     * Tests connection to IPTV server
     * @param credentials Server credentials
     * @return ServiceResult indicating connection status
     */
    suspend fun testConnection(credentials: IPTVCredentials): ServiceResult<ConnectionStatus> {
        return withContext(Dispatchers.IO) {
            try {
                val testUrl = credentials.buildBaseUrl()
                val result = httpClient.testConnection(testUrl)
                
                result.fold(
                    onSuccess = {
                        ServiceResult.Success(
                            ConnectionStatus(
                                isConnected = true,
                                serverUrl = credentials.host
                            )
                        )
                    },
                    onFailure = { exception ->
                        ServiceResult.Error(
                            "Connection failed: ${exception.message}",
                            exception
                        )
                    }
                )
            } catch (e: Exception) {
                ServiceResult.Error("Connection test failed: ${e.message}", e)
            }
        }
    }

    /**
     * Connects to IPTV server and validates credentials
     * @param credentials Server credentials
     * @return ServiceResult indicating success or failure
     */
    suspend fun connect(credentials: IPTVCredentials): ServiceResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!credentials.isValid()) {
                    return@withContext ServiceResult.Error("Invalid credentials provided")
                }

                val playlistUrl = credentials.buildPlaylistUrl()
                val result = httpClient.get(playlistUrl)
                
                result.fold(
                    onSuccess = { content ->
                        // Validate that we received M3U content
                        if (content.trim().startsWith("#EXTM3U") || content.contains("#EXTINF")) {
                            ServiceResult.Success(true)
                        } else {
                            ServiceResult.Error("Invalid response: Not a valid M3U playlist")
                        }
                    },
                    onFailure = { exception ->
                        ServiceResult.Error("Connection failed: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                ServiceResult.Error("Connection error: ${e.message}", e)
            }
        }
    }

    /**
     * Retrieves and parses content from IPTV server
     * @param credentials Server credentials
     * @param onProgress Optional progress callback (0.0 to 1.0)
     * @return ServiceResult containing parsed content data
     */
    suspend fun getContent(
        credentials: IPTVCredentials,
        onProgress: ((Float) -> Unit)? = null
    ): ServiceResult<ContentData> {
        return withContext(Dispatchers.IO) {
            try {
                val playlistUrl = credentials.buildPlaylistUrl()
                
                onProgress?.invoke(0.1f) // Starting download
                
                val result = httpClient.downloadWithProgress(playlistUrl) { progress ->
                    // Map download progress to 10-80% of total progress
                    onProgress?.invoke(0.1f + (progress * 0.7f))
                }
                
                result.fold(
                    onSuccess = { content ->
                        onProgress?.invoke(0.8f) // Starting parsing
                        
                        val parseResult = m3uParser.parse(content)
                        
                        onProgress?.invoke(1.0f) // Complete
                        
                        if (parseResult.errors.isNotEmpty()) {
                            // Log errors but don't fail if we got some content
                            println("M3U parsing warnings: ${parseResult.errors.joinToString(", ")}")
                        }
                        
                        ServiceResult.Success(
                            ContentData(
                                channels = parseResult.channels,
                                movies = parseResult.movies
                            )
                        )
                    },
                    onFailure = { exception ->
                        ServiceResult.Error("Failed to download playlist: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                ServiceResult.Error("Content retrieval error: ${e.message}", e)
            }
        }
    }

    /**
     * Gets only TV channels from the server
     * @param credentials Server credentials
     * @return ServiceResult containing list of channels
     */
    suspend fun getChannels(credentials: IPTVCredentials): ServiceResult<List<Channel>> {
        return when (val result = getContent(credentials)) {
            is ServiceResult.Success -> ServiceResult.Success(result.data.channels)
            is ServiceResult.Error -> ServiceResult.Error(result.message, result.exception)
        }
    }

    /**
     * Gets only movies from the server
     * @param credentials Server credentials
     * @return ServiceResult containing list of movies
     */
    suspend fun getMovies(credentials: IPTVCredentials): ServiceResult<List<Movie>> {
        return when (val result = getContent(credentials)) {
            is ServiceResult.Success -> ServiceResult.Success(result.data.movies)
            is ServiceResult.Error -> ServiceResult.Error(result.message, result.exception)
        }
    }

    /**
     * Builds authenticated stream URL for a given base stream URL
     * @param credentials Server credentials
     * @param streamUrl Base stream URL
     * @return Complete authenticated stream URL
     */
    fun buildAuthenticatedStreamUrl(credentials: IPTVCredentials, streamUrl: String): String {
        return if (streamUrl.contains("?")) {
            "$streamUrl&username=${credentials.username}&password=${credentials.password}"
        } else {
            "$streamUrl?username=${credentials.username}&password=${credentials.password}"
        }
    }

    /**
     * Searches for channels by name or number
     * @param credentials Server credentials
     * @param query Search query
     * @return ServiceResult containing matching channels
     */
    suspend fun searchChannels(credentials: IPTVCredentials, query: String): ServiceResult<List<Channel>> {
        return when (val result = getChannels(credentials)) {
            is ServiceResult.Success -> {
                val filteredChannels = result.data.filter { channel ->
                    channel.name.contains(query, ignoreCase = true) ||
                    channel.number?.toString()?.contains(query) == true ||
                    channel.groupTitle?.contains(query, ignoreCase = true) == true
                }
                ServiceResult.Success(filteredChannels)
            }
            is ServiceResult.Error -> ServiceResult.Error(result.message, result.exception)
        }
    }

    /**
     * Searches for movies by title
     * @param credentials Server credentials
     * @param query Search query
     * @return ServiceResult containing matching movies
     */
    suspend fun searchMovies(credentials: IPTVCredentials, query: String): ServiceResult<List<Movie>> {
        return when (val result = getMovies(credentials)) {
            is ServiceResult.Success -> {
                val filteredMovies = result.data.filter { movie ->
                    movie.title.contains(query, ignoreCase = true) ||
                    movie.description?.contains(query, ignoreCase = true) == true ||
                    movie.genre?.contains(query, ignoreCase = true) == true
                }
                ServiceResult.Success(filteredMovies)
            }
            is ServiceResult.Error -> ServiceResult.Error(result.message, result.exception)
        }
    }

    /**
     * Gets channel by number
     * @param credentials Server credentials
     * @param channelNumber Channel number to find
     * @return ServiceResult containing the channel if found
     */
    suspend fun getChannelByNumber(credentials: IPTVCredentials, channelNumber: Int): ServiceResult<Channel?> {
        return when (val result = getChannels(credentials)) {
            is ServiceResult.Success -> {
                val channel = result.data.find { it.number == channelNumber }
                ServiceResult.Success(channel)
            }
            is ServiceResult.Error -> ServiceResult.Error(result.message, result.exception)
        }
    }

    /**
     * Releases resources used by the service
     */
    fun close() {
        httpClient.close()
    }
}