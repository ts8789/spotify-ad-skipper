package com.spotify.adskipper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent

class SpotifyController(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastTriggerElapsedMs: Long = 0L
    private val cooldownMs: Long = 4_000L
    private val playDelayMs: Long = 800L

    fun triggerBypassAndPlay(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastTriggerElapsedMs < cooldownMs) {
            Log.d("SpotifyAdSkipper", "Übersprungen wegen Cooldown")
            return false
        }

        val deepLinkIntent = Intent().apply {
            component = ComponentName(
                "com.spotify.music",
                "com.spotify.checkout.deeplink.UcLoggedInDeepLinkActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(deepLinkIntent)
            lastTriggerElapsedMs = now
            Log.i("SpotifyAdSkipper", "UcLoggedInDeepLinkActivity gestartet")

            mainHandler.postDelayed({
                sendPlayCommand()
            }, playDelayMs)

            true
        } catch (e: SecurityException) {
            Log.e("SpotifyAdSkipper", "Start blockiert (nicht exportiert oder Berechtigung fehlt)", e)
            false
        } catch (e: Exception) {
            Log.e("SpotifyAdSkipper", "Fehler beim Starten des Intents", e)
            false
        }
    }

    fun sendPlayCommand() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY)

            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)
            Log.i("SpotifyAdSkipper", "Media-Play-Befehl gesendet")
        } catch (e: Exception) {
            Log.e("SpotifyAdSkipper", "Fehler beim Senden des Play-Befehls", e)
        }
    }
}
