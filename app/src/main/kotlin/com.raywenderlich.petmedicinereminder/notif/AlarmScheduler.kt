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
package com.raywenderlich.petmedicinereminder.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.raywenderlich.petmedicinereminder.R
import com.raywenderlich.petmedicinereminder.data.ReminderData
import com.raywenderlich.petmedicinereminder.reminder.ReminderDialog
import java.util.*
import java.util.Calendar.*

/**
 * Helpers to assist in scheduling alarms for ReminderData.
 */
object AlarmScheduler {

  /**
   * Schedules all the alarms for [ReminderData].
   *
   * @param context      current application context
   * @param reminderData ReminderData to use for the alarm
   */
  fun scheduleAlarmsForReminder(context: Context, reminderData: ReminderData) {

    // get the AlarmManager reference
    val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Schedule the alarms based on the days to administer the medicine
    val days = context.resources.getStringArray(R.array.days)
    if (reminderData.days != null) {
      for (index in reminderData.days!!.indices) {

        val day = reminderData.days!![index]
        if (day != null) {

          // get the PendingIntent for the alarm
          val alarmIntent = createPendingIntent(context, reminderData, day)

          // schedule the alarm
          val dayOfWeek = getDayOfWeek(days, day)
          scheduleAlarm(reminderData, dayOfWeek, alarmIntent, alarmMgr)
        }
      }
    }
  }

  /**
   * Schedules a single alarm
   */
  private fun scheduleAlarm(reminderData: ReminderData, dayOfWeek: Int, alarmIntent: PendingIntent?, alarmMgr: AlarmManager) {

    // Set up the time to schedule the alarm
    val datetimeToAlarm = Calendar.getInstance(Locale.getDefault())
    datetimeToAlarm.timeInMillis = System.currentTimeMillis()
    datetimeToAlarm.set(HOUR_OF_DAY, reminderData.hour)
    datetimeToAlarm.set(MINUTE, reminderData.minute)
    datetimeToAlarm.set(SECOND, 0)
    datetimeToAlarm.set(MILLISECOND, 0)
    datetimeToAlarm.set(DAY_OF_WEEK, dayOfWeek)

    // Compare the datetimeToAlarm to today
    val today = Calendar.getInstance(Locale.getDefault())
    if (shouldNotifyToday(dayOfWeek, today, datetimeToAlarm)) {

      // schedule for today
      alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
          datetimeToAlarm.timeInMillis, (1000 * 60 * 60 * 24 * 7).toLong(), alarmIntent)
      return
    }

    // schedule 1 week out from the day
    datetimeToAlarm.roll(WEEK_OF_YEAR, 1)
    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
        datetimeToAlarm.timeInMillis, (1000 * 60 * 60 * 24 * 7).toLong(), alarmIntent)
  }

  /**
   * Creates a [PendingIntent] for the Alarm using the [ReminderData]
   *
   * @param context      current application context
   * @param reminderData ReminderData for the notification
   * @param day          String representation of the day
   */
  private fun createPendingIntent(context: Context, reminderData: ReminderData, day: String?): PendingIntent? {
    // create the intent using a unique type
    val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
      action = context.getString(R.string.action_notify_administer_medication)
      type = "$day-${reminderData.name}-${reminderData.medicine}-${reminderData.type.name}"
      putExtra(ReminderDialog.KEY_ID, reminderData.id)
    }

    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  /**
   * Determines if the Alarm should be scheduled for today.
   *
   * @param dayOfWeek day of the week as an Int
   * @param today today's datetime
   * @param datetimeToAlarm Alarm's datetime
   */
  private fun shouldNotifyToday(dayOfWeek: Int, today: Calendar, datetimeToAlarm: Calendar): Boolean {
    return dayOfWeek == today.get(DAY_OF_WEEK) &&
        today.get(HOUR_OF_DAY) <= datetimeToAlarm.get(HOUR_OF_DAY) &&
        today.get(MINUTE) <= datetimeToAlarm.get(MINUTE)
  }

  /**
   * Updates a notification.
   * Note: this just calls [AlarmScheduler.scheduleAlarmsForReminder] since
   * alarms with exact matching pending intents will update if they are already set, otherwise
   * call [AlarmScheduler.removeAlarmsForReminder] if the medicine has been administered.
   *
   * @param context      current application context
   * @param reminderData ReminderData for the notification
   */
  fun updateAlarmsForReminder(context: Context, reminderData: ReminderData) {
    if (!reminderData.administered) {
      AlarmScheduler.scheduleAlarmsForReminder(context, reminderData)
    } else {
      AlarmScheduler.removeAlarmsForReminder(context, reminderData)
    }
  }

  /**
   * Removes the notification if it was previously scheduled.
   *
   * @param context      current application context
   * @param reminderData ReminderData for the notification
   */
  fun removeAlarmsForReminder(context: Context, reminderData: ReminderData) {
    val intent = Intent(context.applicationContext, AlarmReceiver::class.java)
    intent.action = context.getString(R.string.action_notify_administer_medication)
    intent.putExtra(ReminderDialog.KEY_ID, reminderData.id)

    // type must be unique so Intent.filterEquals passes the check to make distinct PendingIntents
    // Schedule the alarms based on the days to administer the medicine
    if (reminderData.days != null) {
      for (i in reminderData.days!!.indices) {
        val day = reminderData.days!![i]

        if (day != null) {
          val type = String.format(Locale.getDefault(), "%s-%s-%s-%s", day, reminderData.name, reminderData.medicine, reminderData.type.name)

          intent.type = type
          val alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

          val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
          alarmMgr.cancel(alarmIntent)
        }
      }
    }
  }

  /**
   * Returns the int representation for the day of the week.
   *
   * @param days      array from resources
   * @param dayOfWeek String representation of the day e.g "Sunday"
   * @return [Calendar.DAY_OF_WEEK] for given dayOfWeek
   */
  private fun getDayOfWeek(days: Array<String>, dayOfWeek: String): Int {
    return when {
      dayOfWeek.equals(days[0], ignoreCase = true) -> SUNDAY
      dayOfWeek.equals(days[1], ignoreCase = true) -> MONDAY
      dayOfWeek.equals(days[2], ignoreCase = true) -> TUESDAY
      dayOfWeek.equals(days[3], ignoreCase = true) -> WEDNESDAY
      dayOfWeek.equals(days[4], ignoreCase = true) -> THURSDAY
      dayOfWeek.equals(days[5], ignoreCase = true) -> FRIDAY
      else -> SATURDAY
    }
  }

}