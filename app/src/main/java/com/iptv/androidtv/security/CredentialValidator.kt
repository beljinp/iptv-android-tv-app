package com.iptv.androidtv.security

import com.iptv.androidtv.data.IPTVCredentials
import java.net.URL
import java.util.regex.Pattern

/**
 * Utility class for validating IPTV credentials and input fields
 */
object CredentialValidator {

    private val URL_PATTERN = Pattern.compile(
        "^(https?://)?" +                     // Protocol (optional)
        "([\\w\\-]+\\.)*[\\w\\-]+" +          // Domain
        "(:\\d+)?" +                          // Port (optional)
        "(/.*)?$"                             // Path (optional)
    )

    /**
     * Validation result containing success status and error message
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success() = ValidationResult(true)
            fun error(message: String) = ValidationResult(false, message)
        }
    }

    /**
     * Validates the host URL format and accessibility
     */
    fun validateHost(host: String): ValidationResult {
        if (host.isBlank()) {
            return ValidationResult.error("Host URL is required")
        }

        val trimmedHost = host.trim()
        
        // Check if it's a valid URL format
        if (!URL_PATTERN.matcher(trimmedHost).matches()) {
            return ValidationResult.error("Invalid URL format")
        }

        // Try to parse as URL to validate further
        return try {
            val url = URL(if (!trimmedHost.startsWith("http")) "http://$trimmedHost" else trimmedHost)
            
            if (url.protocol !in listOf("http", "https")) {
                ValidationResult.error("Only HTTP and HTTPS protocols are supported")
            } else {
                ValidationResult.success()
            }
        } catch (e: Exception) {
            ValidationResult.error("Invalid URL: ${e.message}")
        }
    }

    /**
     * Validates the username field
     */
    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult.error("Username is required")
            username.length < 2 -> ValidationResult.error("Username must be at least 2 characters")
            username.length > 50 -> ValidationResult.error("Username must be less than 50 characters")
            !username.matches(Regex("^[a-zA-Z0-9._-]+$")) -> 
                ValidationResult.error("Username can only contain letters, numbers, dots, underscores, and hyphens")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates the password field
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.error("Password is required")
            password.length < 3 -> ValidationResult.error("Password must be at least 3 characters")
            password.length > 100 -> ValidationResult.error("Password must be less than 100 characters")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates complete IPTV credentials
     */
    fun validateCredentials(credentials: IPTVCredentials): ValidationResult {
        val hostValidation = validateHost(credentials.host)
        if (!hostValidation.isValid) {
            return hostValidation
        }

        val usernameValidation = validateUsername(credentials.username)
        if (!usernameValidation.isValid) {
            return usernameValidation
        }

        val passwordValidation = validatePassword(credentials.password)
        if (!passwordValidation.isValid) {
            return passwordValidation
        }

        return ValidationResult.success()
    }

    /**
     * Validates credentials and returns a list of all validation errors
     */
    fun validateCredentialsDetailed(credentials: IPTVCredentials): List<String> {
        val errors = mutableListOf<String>()

        val hostValidation = validateHost(credentials.host)
        if (!hostValidation.isValid) {
            hostValidation.errorMessage?.let { errors.add("Host: $it") }
        }

        val usernameValidation = validateUsername(credentials.username)
        if (!usernameValidation.isValid) {
            usernameValidation.errorMessage?.let { errors.add("Username: $it") }
        }

        val passwordValidation = validatePassword(credentials.password)
        if (!passwordValidation.isValid) {
            passwordValidation.errorMessage?.let { errors.add("Password: $it") }
        }

        return errors
    }

    /**
     * Sanitizes host URL by adding protocol if missing and removing trailing slash
     */
    fun sanitizeHost(host: String): String {
        val trimmed = host.trim()
        val withProtocol = if (!trimmed.startsWith("http")) "http://$trimmed" else trimmed
        return withProtocol.removeSuffix("/")
    }

    /**
     * Sanitizes username by trimming whitespace
     */
    fun sanitizeUsername(username: String): String {
        return username.trim()
    }

    /**
     * Sanitizes password by trimming whitespace
     */
    fun sanitizePassword(password: String): String {
        return password.trim()
    }

    /**
     * Creates sanitized credentials from raw input
     */
    fun createSanitizedCredentials(host: String, username: String, password: String): IPTVCredentials {
        return IPTVCredentials(
            host = sanitizeHost(host),
            username = sanitizeUsername(username),
            password = sanitizePassword(password)
        )
    }
}