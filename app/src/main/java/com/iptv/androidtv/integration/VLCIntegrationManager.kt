package com.iptv.androidtv.integration

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

/**
 * Manages integration with VLC Media Player for stream playback
 */
class VLCIntegrationManager(private val context: Context) {
    
    companion object {
        // VLC package names for different platforms
        private const val VLC_PACKAGE_NAME = "org.videolan.vlc"
        private const val VLC_BETA_PACKAGE_NAME = "org.videolan.vlc.beta"
        
        // VLC intent actions
        private const val VLC_ACTION_VIEW = "org.videolan.vlc.player.result"
        private const val VLC_ACTION_PLAY = "org.videolan.vlc.gui.MainActivity"
        
        // Play Store URLs
        private const val VLC_PLAY_STORE_URL = "market://details?id=$VLC_PACKAGE_NAME"
        private const val VLC_WEB_STORE_URL = "https://play.google.com/store/apps/details?id=$VLC_PACKAGE_NAME"
    }

    /**
     * Result of VLC operations
     */
    sealed class VLCResult {
        object Success : VLCResult()
        data class Error(val message: String, val canInstall: Boolean = false) : VLCResult()
    }

    /**
     * VLC installation status
     */
    data class VLCStatus(
        val isInstalled: Boolean,
        val packageName: String? = null,
        val versionName: String? = null,
        val versionCode: Long? = null
    )

    /**
     * Checks if VLC Media Player is installed on the device
     * @return VLCStatus containing installation information
     */
    fun getVLCStatus(): VLCStatus {
        val packageManager = context.packageManager
        
        // Check main VLC package first
        val vlcPackageInfo = try {
            packageManager.getPackageInfo(VLC_PACKAGE_NAME, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        
        if (vlcPackageInfo != null) {
            return VLCStatus(
                isInstalled = true,
                packageName = VLC_PACKAGE_NAME,
                versionName = vlcPackageInfo.versionName,
                versionCode = vlcPackageInfo.longVersionCode
            )
        }
        
        // Check VLC beta package as fallback
        val vlcBetaPackageInfo = try {
            packageManager.getPackageInfo(VLC_BETA_PACKAGE_NAME, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        
        if (vlcBetaPackageInfo != null) {
            return VLCStatus(
                isInstalled = true,
                packageName = VLC_BETA_PACKAGE_NAME,
                versionName = vlcBetaPackageInfo.versionName,
                versionCode = vlcBetaPackageInfo.longVersionCode
            )
        }
        
        return VLCStatus(isInstalled = false)
    }

    /**
     * Checks if VLC is installed (simple boolean check)
     * @return true if VLC is installed, false otherwise
     */
    fun isVLCInstalled(): Boolean {
        return getVLCStatus().isInstalled
    }

    /**
     * Launches a stream in VLC Media Player
     * @param streamUrl The URL of the stream to play
     * @param title Optional title for the stream
     * @return VLCResult indicating success or failure
     */
    fun launchStream(streamUrl: String, title: String? = null): VLCResult {
        val vlcStatus = getVLCStatus()
        
        if (!vlcStatus.isInstalled) {
            return VLCResult.Error(
                "VLC Media Player is not installed. Please install it from the Play Store.",
                canInstall = true
            )
        }
        
        return try {
            val intent = createVLCIntent(streamUrl, title, vlcStatus.packageName!!)
            context.startActivity(intent)
            VLCResult.Success
        } catch (e: Exception) {
            VLCResult.Error("Failed to launch VLC: ${e.message}")
        }
    }

    /**
     * Creates an intent to launch VLC with the specified stream
     * @param streamUrl The stream URL
     * @param title Optional title
     * @param packageName VLC package name to use
     * @return Intent configured for VLC
     */
    fun createVLCIntent(streamUrl: String, title: String? = null, packageName: String? = null): Intent {
        val uri = Uri.parse(streamUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // Set VLC as the target package if specified
        val targetPackage = packageName ?: getVLCStatus().packageName
        if (targetPackage != null) {
            intent.setPackage(targetPackage)
        }
        
        // Add VLC-specific extras
        intent.apply {
            type = "video/*"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // Add title if provided
            title?.let {
                putExtra("title", it)
                putExtra("itemTitle", it)
            }
            
            // VLC-specific options
            putExtra("from_start", false)
            putExtra("play_from_start", true)
        }
        
        return intent
    }

    /**
     * Opens VLC installation page in Play Store
     * @return VLCResult indicating success or failure
     */
    fun openVLCInstallPage(): VLCResult {
        return try {
            // Try to open Play Store app first
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse(VLC_PLAY_STORE_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (playStoreIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(playStoreIntent)
            } else {
                // Fallback to web browser
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(VLC_WEB_STORE_URL)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
            
            VLCResult.Success
        } catch (e: Exception) {
            VLCResult.Error("Failed to open Play Store: ${e.message}")
        }
    }

    /**
     * Shows a toast message prompting user to install VLC
     */
    fun showVLCInstallPrompt() {
        Toast.makeText(
            context,
            "VLC Media Player is required. Please install it from the Play Store.",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Launches stream with error handling and user feedback
     * @param streamUrl The stream URL
     * @param title Optional title
     * @param showToast Whether to show toast messages for errors
     * @return VLCResult indicating success or failure
     */
    fun launchStreamWithFeedback(
        streamUrl: String, 
        title: String? = null, 
        showToast: Boolean = true
    ): VLCResult {
        val result = launchStream(streamUrl, title)
        
        if (result is VLCResult.Error && showToast) {
            if (result.canInstall) {
                showVLCInstallPrompt()
            } else {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
        }
        
        return result
    }

    /**
     * Validates that a stream URL is compatible with VLC
     * @param streamUrl The stream URL to validate
     * @return true if the URL appears to be a valid stream URL
     */
    fun isValidStreamUrl(streamUrl: String): Boolean {
        if (streamUrl.isBlank()) return false
        
        val uri = try {
            Uri.parse(streamUrl)
        } catch (e: Exception) {
            return false
        }
        
        // Check for valid scheme
        val scheme = uri.scheme?.lowercase()
        if (scheme !in listOf("http", "https", "rtmp", "rtsp", "mms", "file")) {
            return false
        }
        
        // Basic URL validation
        return uri.host != null || scheme == "file"
    }

    /**
     * Gets supported stream formats by VLC
     * @return List of supported file extensions/formats
     */
    fun getSupportedFormats(): List<String> {
        return listOf(
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm",
            "m3u8", "ts", "m4v", "3gp", "ogv", "asf",
            "rtmp", "rtsp", "mms", "http", "https"
        )
    }

    /**
     * Checks if a stream format is supported by VLC
     * @param url The stream URL or file extension
     * @return true if the format is supported
     */
    fun isFormatSupported(url: String): Boolean {
        val supportedFormats = getSupportedFormats()
        val lowerUrl = url.lowercase()
        
        return supportedFormats.any { format ->
            lowerUrl.contains(format) || lowerUrl.endsWith(".$format")
        }
    }
}