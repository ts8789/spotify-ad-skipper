package com.spotify.adskipper

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var spotifyController: SpotifyController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spotifyController = SpotifyController(applicationContext)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (24 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        statusText = TextView(this).apply {
            textSize = 16f
        }

        val permissionButton = Button(this).apply {
            text = "Benachrichtigungszugriff öffnen"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val testButton = Button(this).apply {
            text = "Intent + Play jetzt testen"
            setOnClickListener {
                spotifyController.triggerBypassAndPlay()
            }
        }

        root.addView(statusText)
        root.addView(permissionButton)
        root.addView(testButton)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val enabled = isNotificationServiceEnabled()
        statusText.text = if (enabled) {
            "Status: Benachrichtigungszugriff aktiv (Shizuku deaktiviert/entfernt)"
        } else {
            "Status: Bitte Benachrichtigungszugriff für diese App aktivieren"
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?: return false
        val component = ComponentName(this, SpotifyAdListener::class.java).flattenToString()
        return flat.split(":").any { it.equals(component, ignoreCase = true) }
    }
}
