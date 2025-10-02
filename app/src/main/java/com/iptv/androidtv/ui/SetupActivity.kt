package com.iptv.androidtv.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.iptv.androidtv.R
import com.iptv.androidtv.data.IPTVCredentials
import com.iptv.androidtv.security.CredentialManager
import com.iptv.androidtv.security.CredentialValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetupActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (savedInstanceState == null) {
            GuidedStepSupportFragment.addAsRoot(this, SetupFragment(), android.R.id.content)
        }
    }

    class SetupFragment : GuidedStepSupportFragment() {
        
        companion object {
            private const val ACTION_HOST = 1L
            private const val ACTION_USERNAME = 2L
            private const val ACTION_PASSWORD = 3L
            private const val ACTION_SAVE = 4L
        }

        private var hostValue = ""
        private var usernameValue = ""
        private var passwordValue = ""

        override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
            return GuidanceStylist.Guidance(
                getString(R.string.setup_title),
                getString(R.string.setup_description),
                "",
                null
            )
        }

        override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
            // Host input
            actions.add(
                GuidedAction.Builder(requireContext())
                    .id(ACTION_HOST)
                    .title(getString(R.string.host_label))
                    .description(getString(R.string.host_hint))
                    .editable(true)
                    .build()
            )

            // Username input
            actions.add(
                GuidedAction.Builder(requireContext())
                    .id(ACTION_USERNAME)
                    .title(getString(R.string.username_label))
                    .description(getString(R.string.username_hint))
                    .editable(true)
                    .build()
            )

            // Password input
            actions.add(
                GuidedAction.Builder(requireContext())
                    .id(ACTION_PASSWORD)
                    .title(getString(R.string.password_label))
                    .description(getString(R.string.password_hint))
                    .editable(true)
                    .inputType(android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .build()
            )

            // Save button
            actions.add(
                GuidedAction.Builder(requireContext())
                    .id(ACTION_SAVE)
                    .title(getString(R.string.save_button))
                    .build()
            )
        }

        override fun onGuidedActionClicked(action: GuidedAction) {
            when (action.id) {
                ACTION_SAVE -> {
                    saveCredentials()
                }
            }
        }

        override fun onGuidedActionEditedAndProceed(action: GuidedAction): Long {
            when (action.id) {
                ACTION_HOST -> {
                    hostValue = action.editTitle?.toString() ?: ""
                    validateHost()
                }
                ACTION_USERNAME -> {
                    usernameValue = action.editTitle?.toString() ?: ""
                    validateUsername()
                }
                ACTION_PASSWORD -> {
                    passwordValue = action.editTitle?.toString() ?: ""
                    validatePassword()
                }
            }
            return super.onGuidedActionEditedAndProceed(action)
        }

        private fun validateHost() {
            val validation = CredentialValidator.validateHost(hostValue)
            if (!validation.isValid) {
                // Show error - in a real implementation, you'd update the UI
                // For now, we'll just store the validation result
            }
        }

        private fun validateUsername() {
            val validation = CredentialValidator.validateUsername(usernameValue)
            if (!validation.isValid) {
                // Show error
            }
        }

        private fun validatePassword() {
            val validation = CredentialValidator.validatePassword(passwordValue)
            if (!validation.isValid) {
                // Show error
            }
        }

        private fun saveCredentials() {
            val credentials = CredentialValidator.createSanitizedCredentials(
                hostValue, usernameValue, passwordValue
            )

            val validation = CredentialValidator.validateCredentials(credentials)
            if (!validation.isValid) {
                // Show validation error
                return
            }

            val credentialManager = CredentialManager(requireContext())
            
            CoroutineScope(Dispatchers.Main).launch {
                val result = credentialManager.saveCredentials(credentials)
                
                result.fold(
                    onSuccess = {
                        // Navigate to main activity
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    },
                    onFailure = {
                        // Show error message
                    }
                )
            }
        }
    }
}