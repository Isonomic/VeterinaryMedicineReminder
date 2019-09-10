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

import com.raywenderlich.petmedicinereminder.data.ReminderData
import java.util.*

class Presenter(
    private val view: ReminderContracts.View,
    private val model: Model
) : ReminderContracts.Presenter {

  override fun start() {
    if (model.getReminderData() != null) {
      view.displayExistingReminder(model.getReminderData()!!)
    }
  }

  override fun timeTapped() {
    val reminderData = model.getReminderData()
    if (reminderData != null) {
      view.displayTimeDialog(reminderData.hour, reminderData.minute)
    } else {
      val date = Calendar.getInstance()
      val hour = date.get(Calendar.HOUR_OF_DAY)
      val minute = date.get(Calendar.MINUTE)
      view.displayTimeDialog(hour, minute)
    }
  }

  override fun timeSelected(hourOfDay: Int, minute: Int) {
    model.setHourAndMinute(hourOfDay, minute)
  }

  override fun saveTapped(name: String, petType: ReminderData.PetType, medicine: String, description: String, days: Array<String?>, notes: String, administered: Boolean) {

    //.. required field error checking
    if (name.trim().isEmpty()) {
      view.displayError(ReminderDialog.ERROR_NO_NAME)
      return
    } else if (medicine.trim().isEmpty()) {
      view.displayError(ReminderDialog.ERROR_NO_MEDICINE)
      return
    } else if (description.trim().isEmpty()) {
      view.displayError(ReminderDialog.ERROR_NO_DESC)
      return
    } else if (model.getReminderData() == null) {
      view.displayError(ReminderDialog.ERROR_NO_TIME)
      return
    } else if (!validDays(days)) {
      view.displayError(ReminderDialog.ERROR_NO_DAYS)
      return
    }

    //.. handle save or update operation
    if (model.getIsEditing()) {
      val affectedRows = model.updateReminder(name, petType, medicine, description, notes, days, administered)
      if (affectedRows > 0) {
        view.close(ReminderDialog.REMINDER_UPDATED, model.getReminderData()!!)
      } else {
        view.displayError(ReminderDialog.ERROR_UPDATE_FAILED)
      }
    } else {
      val rowId = model.createReminder(name, petType, medicine, description, notes, days)
      if (rowId > -1) {
        view.close(ReminderDialog.REMINDER_CREATED, model.getReminderData()!!)
      } else {
        view.displayError(ReminderDialog.ERROR_SAVE_FAILED)
      }
    }
  }

  override fun deleteTapped() {
    val reminderData = model.getReminderData() ?: return
    val rowsAffected = model.deleteReminder(reminderData.id)
    if (rowsAffected > 0) {
      view.close(ReminderDialog.REMINDER_DELETED, reminderData)
    } else {
      view.displayError(ReminderDialog.ERROR_DELETE_FAILED)
    }

  }

  override fun administeredTapped() {
    if (!model.getIsEditing()) {
      view.displayAdministerError()
    }
  }

  private fun validDays(days: Array<String?>): Boolean {
    for (day in days) {
      if (day != null) {
        return true
      }
    }
    return false
  }
}