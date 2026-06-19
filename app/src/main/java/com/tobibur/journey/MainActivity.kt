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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieCompositionFactory
import com.tobibur.journey.di.ThemeBootstrapEntryPoint
import com.tobibur.journey.presentation.navigation.JournalNavHost
import com.tobibur.journey.presentation.screens.settings.SettingsViewModel
import com.tobibur.journey.ui.theme.JourneyTheme
import com.tobibur.journey.utils.BiometricAuthManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private var isAuthenticated = mutableStateOf(false)
    private var appLockRequired = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = EntryPointAccessors.fromApplication(
            applicationContext,
            ThemeBootstrapEntryPoint::class.java,
        ).settingsPreferences()
        val useDarkTheme = runBlocking { prefs.darkThemeFlow.first() }
        setTheme(if (useDarkTheme) R.style.Theme_Journey_Dark else R.style.Theme_Journey_Light)

        // Read the app-lock setting synchronously so the very first frame already
        // reflects the real value. Relying on the SettingsViewModel StateFlow here
        // is unsafe because it emits its placeholder `false` before DataStore loads,
        // which would latch isAuthenticated = true and skip the biometric prompt on
        // a fresh (cold) launch.
        val appLockEnabled = runBlocking { prefs.appLockEnabledFlow.first() }
        appLockRequired.value = appLockEnabled
        isAuthenticated.value = !appLockEnabled

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        preloadLottieAnimations()

        // Keep appLockRequired in sync with live setting changes (e.g. the user
        // toggling app lock in Settings while the app is open). The raw flow emits
        // the stored value first, so there is no spurious unlock.
        lifecycleScope.launch {
            prefs.appLockEnabledFlow.collect { enabled ->
                appLockRequired.value = enabled
                if (!enabled) isAuthenticated.value = true
            }
        }

        // Trigger the initial unlock. onResume runs after onCreate on a fresh
        // launch and again whenever returning from background, so it covers both
        // cold start and resume.
        if (appLockRequired.value && !isAuthenticated.value &&
            !BiometricAuthManager.isBiometricAvailable(this)
        ) {
            // Biometric was disabled after app lock was enabled; allow access.
            isAuthenticated.value = true
        }

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()

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

    private fun preloadLottieAnimations() {
        lifecycleScope.launch(Dispatchers.IO) {
            listOf(R.raw.empty_ghost, R.raw.thumbsupbird).forEach { resId ->
                LottieCompositionFactory.fromRawRes(this@MainActivity, resId)
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