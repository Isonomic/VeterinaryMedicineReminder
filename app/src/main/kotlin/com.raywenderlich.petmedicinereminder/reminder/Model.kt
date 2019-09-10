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
package com.raywenderlich.petmedicinereminder.reminder

import com.raywenderlich.petmedicinereminder.data.DataUtils
import com.raywenderlich.petmedicinereminder.data.ReminderContract
import com.raywenderlich.petmedicinereminder.data.ReminderData
import com.raywenderlich.petmedicinereminder.data.ReminderDbHelper

class Model(
    private val reminderDbHelper: ReminderDbHelper,
    private var reminderData: ReminderData? = null
) {

  var editing = reminderData != null

  fun getReminderData(): ReminderData? {
    return reminderData
  }

  fun getIsEditing(): Boolean {
    return editing
  }

  fun setHourAndMinute(hourOfDay: Int, minute: Int) {
    if (reminderData == null) {
      reminderData = ReminderData()
    }
    reminderData?.hour = hourOfDay
    reminderData?.minute = minute
  }

  // the row ID of the newly inserted row, or -1 if an error occurred
  fun createReminder(name: String, petType: ReminderData.PetType, medicine: String,
                     desc: String, note: String, days: Array<String?>?): Long {

    reminderData?.name = name
    reminderData?.type = petType
    reminderData?.medicine = medicine
    reminderData?.desc = desc
    reminderData?.note = note
    reminderData?.days = days
    reminderData?.administered = false

    val sqLiteDatabase = reminderDbHelper.writableDatabase
    val contentValues = DataUtils.createContentValues(reminderData as ReminderData)
    val rowId = sqLiteDatabase.insert(ReminderContract.TABLE_NAME, null, contentValues)

    reminderData?.id = rowId.toInt()

    return rowId
  }

  // returns number of rows affected
  fun updateReminder(name: String, petType: ReminderData.PetType, medicine: String,
                     desc: String, note: String, days: Array<String?>?, administered: Boolean): Int {

    reminderData?.name = name
    reminderData?.type = petType
    reminderData?.medicine = medicine
    reminderData?.desc = desc
    reminderData?.note = note
    reminderData?.days = days
    reminderData?.administered = administered

    val sqLiteDatabase = reminderDbHelper.writableDatabase
    val contentValues = DataUtils.createContentValues(reminderData as ReminderData)
    return sqLiteDatabase.update(ReminderContract.TABLE_NAME, contentValues, ReminderContract.ID + " =? ",
        arrayOf(reminderData?.id.toString()))
  }


  // returns number of rows affected
  fun deleteReminder(id: Int): Int {
    val sqLiteDatabase = reminderDbHelper.writableDatabase
    return sqLiteDatabase.delete(ReminderContract.TABLE_NAME, ReminderContract.ID + " =? ",
        arrayOf(id.toString()))
  }
}