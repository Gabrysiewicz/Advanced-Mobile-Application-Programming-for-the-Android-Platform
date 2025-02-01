package com.example.masterand

import android.app.Application
import android.view.OrientationEventListener
import android.widget.Toast
import com.example.masterand.di.appModule
import kotlinx.coroutines.delay
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import kotlinx.coroutines.GlobalScope

class MasterAndApplication : Application() {
    private lateinit var orientationListener: OrientationEventListener
    private var isToastShown = false  // Flag to control toast repetition

    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin {
            androidContext(this@MasterAndApplication)
            modules(appModule)
        }
        // Create an OrientationEventListener
        orientationListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                // We want to show the Toast when the device goes from portrait to landscape
                if (!isToastShown && (orientation in 75..135 || orientation in 225..315)) {
                    // Detect landscape orientation change
                    showOrientationMessage()
                    isToastShown = true  // Set the flag to prevent repeated toasts
                }

                // Reset the flag when the orientation changes back to portrait
                if (orientation in 0..44 || orientation in 136..224) {
                    isToastShown = false
                }
            }
        }

        // Enable the OrientationEventListener
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        } else {
            orientationListener.disable()
        }
    }
    // Show the Toast message at the bottom
    private fun showOrientationMessage() {
        val context = this
        Toast.makeText(context, "Hey! Hey! Hey! No rotations here!", Toast.LENGTH_SHORT).show()

        // Use GlobalScope to launch the coroutine
        GlobalScope.launch {
            delay(2000)  // Wait for 2 seconds before allowing the next message
            isToastShown = false
        }
    }

    // This method is called when the application is terminated
    override fun onTerminate() {
        super.onTerminate()
        // Disable the orientation listener to avoid memory leaks
        orientationListener.disable()
    }
}
