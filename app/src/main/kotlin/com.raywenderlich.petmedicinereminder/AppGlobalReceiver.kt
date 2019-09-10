/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.raywenderlich.petmedicinereminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import com.raywenderlich.petmedicinereminder.data.DataUtils
import com.raywenderlich.petmedicinereminder.notif.AlarmScheduler
import com.raywenderlich.petmedicinereminder.reminder.ReminderDialog

/**
 * Handles any global application broadcasts.
 * <p>
 * Note: this really only handles the action from the
 * pet notification to administer the medicine but it could be used for any other actions.
 */
class AppGlobalReceiver : BroadcastReceiver() {

  companion object {
    const val NOTIFICATION_ID = "notification_id"
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    if (context != null && intent != null && intent.action != null) {

      // Handle the action to set the Medicine Administered
      if (intent.action!!.equals(context.getString(R.string.action_medicine_administered), ignoreCase = true)) {

        val extras = intent.extras
        if (extras != null) {

          val notificationId = extras.getInt(NOTIFICATION_ID)

          val reminderId = extras.getInt(ReminderDialog.KEY_ID)
          val medicineAdministered = extras.getBoolean(ReminderDialog.KEY_ADMINISTERED)

          // Lookup the reminder for sanity
          val reminderData = DataUtils.getReminderById(reminderId)

          if (reminderData != null) {

            // Update the database
            DataUtils.setMedicineAdministered(reminderId, medicineAdministered)

            // Remove the alarm
            AlarmScheduler.removeAlarmsForReminder(context, reminderData)
          }

          // finally, cancel the notification
          if (notificationId != -1) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(notificationId)
            notificationManager.cancelAll() // testing
          }
        }
      }
    }
  }
}