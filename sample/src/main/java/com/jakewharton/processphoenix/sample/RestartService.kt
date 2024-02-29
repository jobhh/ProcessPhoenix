package com.jakewharton.processphoenix.sample

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.util.Log
import com.jakewharton.processphoenix.NotificationBuilder
import com.jakewharton.processphoenix.ProcessPhoenix
import java.util.concurrent.Executors

/**
 * This [RestartService] will attempt to restart after 3 seconds
 *
 * Please note that restarting a Service multiple times will result in an increasingly long delay between restart times.
 * This is a safety mechanism, since Android registers the restart as a restart of a crashed service.
 *
 * The observed delay periods are: 1s, 4s, 16s, 64s, 256s, 1024s. (on an Android 11 device)
 * Which seems to follow this pattern: 4^x, with x being the restart attempt minus 1.
 */
class RestartService: Service() {

  @SuppressLint("ForegroundServiceType")
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // Log something to console to easily track successful restarts
    Log.d(
    "ProcessPhoenix",
    "--- RestartService started with PID: ${Process.myPid()} ---"
    )

    if (Build.VERSION.SDK_INT >= 26) {
      startForeground(1337, NotificationBuilder.createNotification(this@RestartService))
    }

    // Trigger rebirth from a separate thread, such that the onStartCommand can finish properly
    Executors.newSingleThreadExecutor().execute {
      Thread.sleep(3000)
      ProcessPhoenix.triggerRebirth(this@RestartService, RestartService::class.java)
    }

    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
