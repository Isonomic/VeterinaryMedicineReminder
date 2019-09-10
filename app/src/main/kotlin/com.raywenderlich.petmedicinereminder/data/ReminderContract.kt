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
package com.raywenderlich.petmedicinereminder.data

import android.provider.BaseColumns

/**
 * Database contract class containing column names and Create table declaration.
 */
object ReminderContract : BaseColumns {

  const val ID = BaseColumns._ID
  const val TABLE_NAME = "Reminders"
  const val KEY_NAME = "name"
  const val KEY_TYPE = "type"
  const val KEY_MEDICINE = "medicine"
  const val KEY_NOTE = "note"
  const val KEY_DESC = "desc"
  const val KEY_HOUR = "hour"
  const val KEY_MINUTE = "minute"
  const val KEY_DAYS = "days"
  const val KEY_ADMINISTERED = "administered"

  internal const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " ("
      + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
      + KEY_NAME + " TEXT NOT NULL, "
      + KEY_TYPE + " TEXT NOT NULL, "
      + KEY_MEDICINE + " TEXT NOT NULL, "
      + KEY_DESC + " TEXT NOT NULL, "
      + KEY_NOTE + " TEXT, "
      + KEY_HOUR + " INTEGER, "
      + KEY_MINUTE + " INTEGER, "
      + KEY_DAYS + " TEXT NOT NULL, "
      + KEY_ADMINISTERED + " INTEGER DEFAULT 0);")
}