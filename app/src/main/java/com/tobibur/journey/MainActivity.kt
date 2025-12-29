package com.tobibur.journey

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tobibur.journey.presentation.navigation.JournalNavHost
import com.tobibur.journey.presentation.screens.settings.SettingsViewModel
import com.tobibur.journey.ui.theme.JourneyTheme
import com.tobibur.journey.utils.BiometricAuthManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private var isAuthenticated = mutableStateOf(false)
    private var appLockRequired = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val appLockEnabled by settingsViewModel.appLockEnabled.collectAsState()

            LaunchedEffect(appLockEnabled) {
                appLockRequired.value = appLockEnabled
                if (appLockEnabled && !isAuthenticated.value) {
                    if (BiometricAuthManager.isBiometricAvailable(this@MainActivity)) {
                        authenticateUser()
                    } else {
                        // If biometric is not available but app lock is enabled,
                        // allow access (edge case where biometric was disabled after enabling app lock)
                        isAuthenticated.value = true
                    }
                } else if (!appLockEnabled) {
                    isAuthenticated.value = true
                }
            }

            JourneyTheme(settingsViewModel) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isAuthenticated.value || !appLockRequired.value) {
                        JournalNavHost()
                    } else {
                        // Show blank screen while waiting for authentication
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-authenticate when app comes back from background if app lock is enabled
        if (appLockRequired.value && !isAuthenticated.value) {
            if (BiometricAuthManager.isBiometricAvailable(this)) {
                authenticateUser()
            } else {
                // If biometric is not available, allow access (edge case)
                isAuthenticated.value = true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Lock the app when it goes to background if app lock is enabled
        if (appLockRequired.value) {
            isAuthenticated.value = false
        }
    }

    private fun authenticateUser() {
        val biometricAuthManager = BiometricAuthManager(this)
        biometricAuthManager.authenticate(
            title = "Unlock Journey",
            subtitle = "Use your biometric credentials to unlock the app",
            negativeButtonText = "Cancel",
            onSuccess = {
                isAuthenticated.value = true
            },
            onError = { errorMessage ->
                Toast.makeText(this, "Authentication error: $errorMessage", Toast.LENGTH_SHORT).show()
                // Close app if authentication fails
                if (errorMessage.contains("Cancel", ignoreCase = true)) {
                    finish()
                }
            },
            onFailed = {
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JourneyTheme() {
        JournalNavHost()
    }
}