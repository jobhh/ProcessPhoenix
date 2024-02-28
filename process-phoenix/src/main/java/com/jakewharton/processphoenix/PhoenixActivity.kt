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

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.StrictMode
import android.os.StrictMode.VmPolicy

class PhoenixActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Kill original main process
    Process.killProcess(intent.getIntExtra(ProcessPhoenix.KEY_MAIN_PROCESS_PID, -1))

    val intents: Array<Intent>? = if (Build.VERSION.SDK_INT >= 33) {
      intent.getParcelableArrayListExtra(ProcessPhoenix.KEY_RESTART_INTENTS, Intent::class.java)
        ?.toTypedArray()
    } else {
      intent.getParcelableArrayListExtra<Intent>(ProcessPhoenix.KEY_RESTART_INTENTS)
        ?.toTypedArray()
    }

    if (Build.VERSION.SDK_INT > 31) {
      // Disable strict mode complaining about out-of-process intents. Normally you save and restore
      // the original policy, but this process will die almost immediately after the offending call.
      StrictMode.setVmPolicy(
        VmPolicy.Builder(StrictMode.getVmPolicy())
          .permitUnsafeIntentLaunch()
          .build()
      )
    }

    startActivities(intents)
    finish()
    Runtime.getRuntime().exit(0) // Kill kill kill!
  }
}
