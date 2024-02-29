/*
 * Copyright (C) 2014 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jakewharton.processphoenix

import android.app.IntentService
import android.content.Intent
import android.os.Build
import android.os.Process
import android.os.StrictMode

/**
 * Please note that restarting a Service multiple times will result in an increasingly long delay between restart times.
 * This is a safety mechanism, since Android registers the restart of this service as a crashed service.
 *
 * The observed delay periods are: 1s, 4s, 16s, 64s, 256s, 1024s. (on an Android 11 device)
 * Which seems to follow this pattern: 4^x, with x being the restart attempt minus 1.
 */
class PhoenixService: IntentService("PhoenixService") {

  override fun onHandleIntent(intent: Intent?) {
    intent ?: return

    // Kill original main process
    Process.killProcess(intent.getIntExtra(ProcessPhoenix.KEY_MAIN_PROCESS_PID, -1))

    val nextIntent: Intent? = if (Build.VERSION.SDK_INT >= 33) {
      intent.getParcelableExtra(ProcessPhoenix.KEY_RESTART_INTENT, Intent::class.java)
    } else {
      intent.getParcelableExtra(ProcessPhoenix.KEY_RESTART_INTENT)
    }

    if (Build.VERSION.SDK_INT > 31) {
      // Disable strict mode complaining about out-of-process intents. Normally you save and restore
      // the original policy, but this process will die almost immediately after the offending call.
      StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
          .permitUnsafeIntentLaunch()
          .build()
      )
    }

    if (Build.VERSION.SDK_INT >= 26) {
      startForegroundService(nextIntent)
    } else {
      startService(nextIntent)
    }

    Runtime.getRuntime().exit(0) // Kill kill kill!
  }
}
