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

import com.raywenderlich.petmedicinereminder.data.DataUtils
import com.raywenderlich.petmedicinereminder.data.ReminderContract
import com.raywenderlich.petmedicinereminder.data.ReminderData
import com.raywenderlich.petmedicinereminder.data.ReminderDbHelper

class MainModel(
    private val reminderDbHelper: ReminderDbHelper
) {

  fun loadAllReminders(): List<ReminderData> {
    val sqLiteDatabase = reminderDbHelper.readableDatabase
    val cursor = sqLiteDatabase.query(ReminderContract.TABLE_NAME, null, null, null, null, null, null)
    val reminders = ArrayList<ReminderData>(cursor.count)
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast) {
        val reminderData = DataUtils.createReminderFromCursor(cursor)
        reminders.add(reminderData)
        cursor.moveToNext()
      }
    }
    cursor.close()

    return reminders
  }

  fun insertSampleReminders(reminderDataList: List<ReminderData>): Int {
    val sqLiteDatabase = reminderDbHelper.writableDatabase
    var count = 0 // track successful insert count
    for (reminderData in reminderDataList) {
      val contentValues = DataUtils.createContentValues(reminderData)
      val rowId = sqLiteDatabase.insert(ReminderContract.TABLE_NAME, null, contentValues)
      if (rowId > -1) {
        count++
      }
    }
    return count
  }


  fun deleteAllReminders() {
    val sqLiteDatabase = reminderDbHelper.readableDatabase
    sqLiteDatabase.delete(ReminderContract.TABLE_NAME, null, null)
  }
}