package com.spotify.adskipper

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class SpotifyAdListener : NotificationListenerService() {

    private lateinit var spotifyController: SpotifyController

    override fun onCreate() {
        super.onCreate()
        spotifyController = SpotifyController(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null || sbn.packageName != "com.spotify.music") return

        if (isAdNotification(sbn.notification)) {
            Log.i("SpotifyAdSkipper", "Werbung in Spotify-Benachrichtigung erkannt")
            spotifyController.triggerBypassAndPlay()
        }
    }

    private fun isAdNotification(notification: Notification?): Boolean {
        val extras = notification?.extras ?: return false
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()

        val combined = "$title $text $subText".lowercase()
        val adKeywords = listOf(
            "advertisement",
            "werbung",
            "anzeige",
            "spotify free"
        )

        return adKeywords.any { combined.contains(it) }
    }
}
