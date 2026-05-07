package com.kaushalyakarnataka.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Kaushalya-Karnataka.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 * across the entire application.
 */
@HiltAndroidApp
class KaushalyaApp : Application()
