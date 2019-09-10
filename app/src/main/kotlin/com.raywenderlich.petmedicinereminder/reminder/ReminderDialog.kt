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

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v4.app.DialogFragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.raywenderlich.petmedicinereminder.R
import com.raywenderlich.petmedicinereminder.data.ReminderData
import com.raywenderlich.petmedicinereminder.data.ReminderDbHelper
import com.raywenderlich.petmedicinereminder.notif.AlarmScheduler
import java.text.SimpleDateFormat
import java.util.*

class ReminderDialog : DialogFragment(), ReminderContracts.View {

  // Threading
  private var handlerThread: HandlerThread? = null
  private var backgroundHandler: Handler? = null
  private val foregroundHandler = Handler(Looper.getMainLooper())

  // Presenter
  private var presenter: ReminderContracts.Presenter? = null

  // Callback interface
  private var onCloseListener: OnCloseListener? = null

  // Views
  private var toolbar: Toolbar? = null
  private var textInputName: TextInputEditText? = null
  private var checkBoxAdministered: CheckBox? = null
  private var radioGroupType: RadioGroup? = null
  private var radioButtonDog: RadioButton? = null
  private var radioButtonCat: RadioButton? = null
  private var radioButtonOther: RadioButton? = null
  private var textInputMedicine: TextInputEditText? = null
  private var textInputDesc: TextInputEditText? = null
  private var buttonTime: Button? = null
  private var linearLayoutDays: LinearLayout? = null
  private var textInputNote: TextInputEditText? = null
  private var fabSaveReminder: FloatingActionButton? = null

  interface OnCloseListener {
    fun onClose(opCode: Int, reminderData: ReminderData)
  }

  companion object {

    const val TAG = "ReminderDialog"

    // key for passed in data
    const val KEY_DATA = "reminder_data"
    const val KEY_ID = "id"
    const val KEY_ADMINISTERED = "administered"

    // opcodes for success
    const val REMINDER_CREATED = 0
    const val REMINDER_UPDATED = 1
    const val REMINDER_DELETED = 2

    // error states for validation
    const val ERROR_NO_NAME = 0
    const val ERROR_NO_MEDICINE = 1
    const val ERROR_NO_TIME = 2
    const val ERROR_NO_DAYS = 3
    const val ERROR_NO_DESC = 4
    const val ERROR_SAVE_FAILED = 5
    const val ERROR_DELETE_FAILED = 6
    const val ERROR_UPDATE_FAILED = 7


    fun newInstance(args: Bundle?): ReminderDialog {
      val reminderDialog = ReminderDialog()
      if (args != null) {
        reminderDialog.arguments = args
      }
      return reminderDialog
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    handlerThread = HandlerThread("ReminderBAckgroudn")
    handlerThread!!.start()
    backgroundHandler = Handler(handlerThread!!.looper)

    val args = arguments
    var reminderData: ReminderData? = null
    if (args != null) {
      reminderData = args.getParcelable(KEY_DATA)
    }
    val reminderDbHelper = ReminderDbHelper()
    val model = Model(reminderDbHelper, reminderData)

    presenter = Presenter(this, model)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_reminder_dialog, container, false)
    cacheViews(view)
    toolbar!!.setNavigationOnClickListener(navigationListener)
    buttonTime!!.setOnClickListener(timeListener)
    fabSaveReminder!!.setOnClickListener(saveListener)
    checkBoxAdministered!!.setOnCheckedChangeListener(administerListener)
    buildCheckBoxes()
    return view
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    try {
      onCloseListener = context as OnCloseListener?
    } catch (ex: ClassCastException) {
      throw RuntimeException("Parent must implement the ReminderDialog.OnCloseListener interface ")
    }

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    backgroundHandler!!.post { presenter!!.start() }
  }

  override fun onDestroy() {
    super.onDestroy()
    handlerThread!!.quit()
    foregroundHandler.removeCallbacksAndMessages(null)
    backgroundHandler!!.removeCallbacksAndMessages(null)
  }

  override fun displayExistingReminder(reminderData: ReminderData) {
    foregroundHandler.post {
      toolbar!!.inflateMenu(R.menu.menu_reminder)
      toolbar!!.setOnMenuItemClickListener(menuItemListener)
      toolbar!!.title = getString(R.string.edit_reminder)
      textInputName!!.setText(reminderData.name)
      checkBoxAdministered!!.isChecked = reminderData.administered
      textInputMedicine!!.setText(reminderData.medicine)
      textInputDesc!!.setText(reminderData.desc)
      textInputNote!!.setText(reminderData.note)

      setupTypeRadioGroup(reminderData)
      setTimeButtonText(reminderData.hour, reminderData.minute)
      setupDaysCheckBoxes(reminderData)
    }
  }

  override fun displayTimeDialog(hour: Int, minute: Int) {
    val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
      presenter!!.timeSelected(hourOfDay, minute)
      setTimeButtonText(hourOfDay, minute)
    }, hour, minute, false)
    timePickerDialog.show()
  }

  override fun displayError(errorCode: Int) {
    foregroundHandler.post {
      val message: String
      when (errorCode) {
        ERROR_NO_NAME -> message = getString(R.string.name_required)
        ERROR_NO_MEDICINE -> message = getString(R.string.medicine_required)
        ERROR_NO_DESC -> message = getString(R.string.desc_required)
        ERROR_NO_TIME -> message = getString(R.string.time_required)
        ERROR_NO_DAYS -> message = getString(R.string.days_required)
        ERROR_SAVE_FAILED -> message = getString(R.string.save_failed)
        ERROR_UPDATE_FAILED -> message = getString(R.string.update_failed)
        ERROR_DELETE_FAILED -> message = getString(R.string.delete_failed)
        else -> message = getString(R.string.unknown_error)
      }

      if (view != null) {
        Snackbar.make(view!!, message, Snackbar.LENGTH_SHORT).show()
      }
    }
  }

  override fun displayAdministerError() {
    if (view != null) {
      Snackbar.make(view!!, getString(R.string.cant_administer_medicine), Snackbar.LENGTH_SHORT).show()
    }
    checkBoxAdministered!!.isChecked = false
  }

  override fun close(opCode: Int, reminderData: ReminderData) {
    foregroundHandler.post {
      if (opCode == ReminderDialog.REMINDER_CREATED && context != null) {

        // schedule an alarm to notify later
        AlarmScheduler.scheduleAlarmsForReminder(context!!, reminderData)

      } else if (opCode == ReminderDialog.REMINDER_UPDATED && context != null) {

        // update the alarm
        AlarmScheduler.updateAlarmsForReminder(context!!, reminderData)
      } else if (opCode == ReminderDialog.REMINDER_DELETED && context != null) {

        // remove alarm
        AlarmScheduler.removeAlarmsForReminder(context!!, reminderData)
      }

      onCloseListener!!.onClose(opCode, reminderData)
      dismiss()
    }
  }

  private fun cacheViews(view: View) {
    toolbar = view.findViewById(R.id.toolbarReminder)
    textInputName = view.findViewById(R.id.textInputName)
    radioGroupType = view.findViewById(R.id.radioGroupType)
    radioButtonDog = view.findViewById(R.id.radioButtonDog)
    radioButtonCat = view.findViewById(R.id.radioButtonCat)
    radioButtonOther = view.findViewById(R.id.radioButtonOther)
    radioGroupType = view.findViewById(R.id.radioGroupType)
    textInputMedicine = view.findViewById(R.id.textInputMedicine)
    textInputDesc = view.findViewById(R.id.textInputDesc)
    buttonTime = view.findViewById(R.id.buttonTime)
    linearLayoutDays = view.findViewById(R.id.linearLayoutDates)
    textInputNote = view.findViewById(R.id.textInputNote)
    fabSaveReminder = view.findViewById(R.id.fabSaveReminder)
    checkBoxAdministered = view.findViewById(R.id.checkBoxAdministered)
  }

  private fun buildCheckBoxes() {
    linearLayoutDays!!.removeAllViews()
    val days = resources.getStringArray(R.array.days)
    for (day in days) {
      val checkBox = CheckBox(context)
      checkBox.text = day
      linearLayoutDays!!.addView(checkBox)
    }
  }

  private fun setupTypeRadioGroup(reminderData: ReminderData) {
    if (reminderData.type === ReminderData.PetType.Dog) {
      radioButtonDog!!.isChecked = true
    } else if (reminderData.type === ReminderData.PetType.Cat) {
      radioButtonCat!!.isChecked = true
    } else {
      radioButtonOther!!.isChecked = true
    }
  }

  private fun setTimeButtonText(hourOfDay: Int, minute: Int) {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
    calendar.set(Calendar.MINUTE, minute)
    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    buttonTime!!.text = dateFormat.format(calendar.time)
  }

  private fun setupDaysCheckBoxes(reminderData: ReminderData) {
    for (i in 0 until linearLayoutDays!!.childCount) {
      if (linearLayoutDays!!.getChildAt(i) is CheckBox) {
        val checkBox = linearLayoutDays!!.getChildAt(i) as CheckBox
        for (j in reminderData.days!!.indices) {
          if (checkBox.text.toString().equals(reminderData.days!![j], ignoreCase = true)) {
            checkBox.isChecked = true
          }
        }
      }
    }
  }

  private val navigationListener = View.OnClickListener { dismiss() }

  private val menuItemListener = Toolbar.OnMenuItemClickListener { menuItem ->
    if (menuItem.itemId == R.id.action_delete_reminder) {
      backgroundHandler!!.post { presenter!!.deleteTapped() }

      return@OnMenuItemClickListener true
    }
    false
  }

  private val timeListener = View.OnClickListener { presenter!!.timeTapped() }

  private val administerListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked -> presenter!!.administeredTapped() }

  private val saveListener = View.OnClickListener {
    // Gather all the fields
    val name = textInputName!!.text!!.toString()
    val checkedId = radioGroupType!!.checkedRadioButtonId
    val petType: ReminderData.PetType
    if (checkedId == R.id.radioButtonDog) {
      petType = ReminderData.PetType.Dog
    } else if (checkedId == R.id.radioButtonCat) {
      petType = ReminderData.PetType.Cat
    } else {
      petType = ReminderData.PetType.Other
    }
    val medicine = textInputMedicine!!.text!!.toString()
    val desc = textInputDesc!!.text!!.toString()
    val note = textInputNote!!.text!!.toString()

    val daysItems = resources.getStringArray(R.array.days)

    for (i in 0 until linearLayoutDays!!.childCount) {
      if (linearLayoutDays!!.getChildAt(i) is CheckBox) {
        val checkBox = linearLayoutDays!!.getChildAt(i) as CheckBox
        if (!checkBox.isChecked) {
          daysItems[i] = null
        }
      }
    }

    val administered = checkBoxAdministered!!.isChecked

    backgroundHandler!!.post { presenter!!.saveTapped(name, petType, medicine, desc, daysItems, note, administered) }
  }
}