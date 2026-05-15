package com.madhusiri.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MadhuSiriApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channels
        com.madhusiri.app.core.notifications.NotificationHelper.createNotificationChannels(this)
        initFirebaseServices()
    }

    private fun initFirebaseServices() {
        // Correct way to enable/disable Crashlytics collection
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)

        // Enable Analytics data collection
        Firebase.analytics.setAnalyticsCollectionEnabled(true)

        // Log a custom event to verify analytics is working on first launch
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
    }
}
