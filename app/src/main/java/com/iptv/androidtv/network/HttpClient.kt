package com.iptv.androidtv.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP client for IPTV server communication with timeout and retry mechanisms
 */
class HttpClient {
    
    companion object {
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 60L
        private const val WRITE_TIMEOUT = 30L
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Makes a GET request to the specified URL with retry mechanism
     * @param url The URL to request
     * @return Result containing the response body or error
     */
    suspend fun get(url: String): Result<String> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "IPTV-AndroidTV/1.0")
                    .build()

                val response: Response = okHttpClient.newCall(request).execute()
                
                return@withContext if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(IOException("Empty response body"))
                    }
                } else {
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
                
            } catch (e: Exception) {
                lastException = e
                
                // Don't retry on the last attempt
                if (attempt < MAX_RETRIES - 1) {
                    kotlinx.coroutines.delay(RETRY_DELAY_MS * (attempt + 1))
                }
            }
        }
        
        Result.failure(lastException ?: IOException("Unknown network error"))
    }

    /**
     * Tests connectivity to a URL without downloading content
     * @param url The URL to test
     * @return Result indicating success or failure
     */
    suspend fun testConnection(url: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .head() // HEAD request for connectivity test
                .addHeader("User-Agent", "IPTV-AndroidTV/1.0")
                .build()

            val response: Response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads content with progress callback
     * @param url The URL to download from
     * @param onProgress Callback for download progress (0.0 to 1.0)
     * @return Result containing the downloaded content
     */
    suspend fun downloadWithProgress(
        url: String, 
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "IPTV-AndroidTV/1.0")
                .build()

            val response: Response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body
            if (body == null) {
                return@withContext Result.failure(IOException("Empty response body"))
            }

            val contentLength = body.contentLength()
            val inputStream = body.byteStream()
            val buffer = ByteArray(8192)
            val result = StringBuilder()
            var totalBytesRead = 0L
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                result.append(String(buffer, 0, bytesRead))
                totalBytesRead += bytesRead
                
                if (contentLength > 0 && onProgress != null) {
                    val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                    onProgress(progress.coerceIn(0f, 1f))
                }
            }

            onProgress?.invoke(1f) // Complete
            Result.success(result.toString())
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Closes the HTTP client and releases resources
     */
    fun close() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
    }
}