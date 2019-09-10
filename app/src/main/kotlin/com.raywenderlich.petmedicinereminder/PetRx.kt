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

import android.app.Application
import android.support.v4.app.NotificationManagerCompat
import com.raywenderlich.petmedicinereminder.data.ReminderData
import com.raywenderlich.petmedicinereminder.notif.NotificationHelper

class PetRx : Application() {

  companion object {
    lateinit var instance: PetRx
      private set
  }

  override fun onCreate() {
    super.onCreate()
    instance = this

    NotificationHelper.createNotificationChannel(this,
        NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
        getString(R.string.app_name), "App notification channel.")
    NotificationHelper.createNotificationChannel(this,
        NotificationManagerCompat.IMPORTANCE_LOW, true,
        ReminderData.PetType.Cat.name, "Notification channel for cats.")
    NotificationHelper.createNotificationChannel(this,
        NotificationManagerCompat.IMPORTANCE_HIGH, true,
        ReminderData.PetType.Dog.name, "Notification channel for dogs.")
    NotificationHelper.createNotificationChannel(this,
        NotificationManagerCompat.IMPORTANCE_NONE, false,
        ReminderData.PetType.Other.name, "Notification channel for other pets.")
  }
}