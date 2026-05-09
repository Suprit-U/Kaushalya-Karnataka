package com.kaushalyakarnataka.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kaushalyakarnataka.app.data.local.ThemePreferenceManager
import com.kaushalyakarnataka.app.navigation.AppNavGraph
import com.kaushalyakarnataka.app.ui.theme.KaushalyaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-Activity entry point for the Kaushalya-Karnataka app.
 * Sets up edge-to-edge display, splash screen, and Compose content.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferenceManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            KaushalyaTheme(preferenceManager = themePreferenceManager) {
                AppNavGraph()
            }
        }
    }
}
