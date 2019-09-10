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

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.raywenderlich.petmedicinereminder.data.ReminderData
import com.raywenderlich.petmedicinereminder.data.ReminderDbHelper
import com.raywenderlich.petmedicinereminder.data.SampleDataHelper
import com.raywenderlich.petmedicinereminder.notif.NotificationHelper
import com.raywenderlich.petmedicinereminder.reminder.ReminderDialog

class MainActivity : AppCompatActivity(), MainContracts.View, MainAdapter.Listener, ReminderDialog.OnCloseListener {

  private val delayTime = 500L

  private lateinit var handlerThread: HandlerThread
  private lateinit var backgroundHandler: Handler
  private lateinit var presenter: MainContracts.Presenter
  private lateinit var mainAdapter: MainAdapter

  private lateinit var textViewNoReminders: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var recyclerView: RecyclerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    handlerThread = HandlerThread("BackgroundWorker")
    handlerThread.start()
    backgroundHandler = Handler(handlerThread.looper)

    val mainModel = MainModel(ReminderDbHelper())
    presenter = MainPresenter(this, mainModel)

    val toolbar: Toolbar = findViewById(R.id.toolbar)
    setSupportActionBar(toolbar)

    textViewNoReminders = findViewById(R.id.textViewNoReminders)
    progressBar = findViewById(R.id.progressBar)
    recyclerView = findViewById(R.id.recyclerViewReminders)
    val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    recyclerView.layoutManager = layoutManager
    recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

    val fabCreateReminder: FloatingActionButton = findViewById(R.id.fabCreateReminder)
    fabCreateReminder.setOnClickListener {
      backgroundHandler.post {
        presenter.createReminder()
      }
    }
  }

  override fun onResume() {
    super.onResume()

    loadData(delayTime)
  }

  override fun onDestroy() {
    super.onDestroy()
    handlerThread.quit()
    backgroundHandler.removeCallbacksAndMessages(null)
  }

  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    val menuItemLoadData = menu.findItem(R.id.action_load_sample_data)
    menuItemLoadData.isVisible = recyclerView.visibility == View.GONE
    val menuItemDeleteData = menu.findItem(R.id.action_delete_data)
    menuItemDeleteData.isVisible = textViewNoReminders.visibility == View.GONE
    val menuItemManageChannels = menu.findItem(R.id.action_manage_channels)
    menuItemManageChannels.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId


    if (id == R.id.action_load_sample_data) {

      //.. Load the sample data
      progressBar.visibility = View.VISIBLE
      textViewNoReminders.visibility = View.GONE
      backgroundHandler.postDelayed({
        val reminderDataList = SampleDataHelper.loadSampleData(this@MainActivity)
        presenter.loadSampleData(reminderDataList!!)
        NotificationHelper.createSampleDataNotification(this@MainActivity,
            getString(R.string.sample_data_loaded_title),
            getString(R.string.sample_data_loaded_message),
            getString(R.string.sample_data_loaded_big_text), false)
      }, delayTime) //.. simulate load time
      return true
    } else if (id == R.id.action_delete_data) {
      backgroundHandler.post { deleteEverything() }
    } else if (id == R.id.action_manage_channels) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
      }
    }

    return super.onOptionsItemSelected(item)
  }

  private fun deleteEverything() {
    presenter.deleteAlarmsForSampleData(this@MainActivity)
    presenter.deleteReminders()
    displayDataDeletedNotification()
  }

  private fun displayDataDeletedNotification() {
    NotificationHelper.createSampleDataNotification(this@MainActivity,
        getString(R.string.sample_data_deleted_title),
        getString(R.string.sample_data_deleted_message),
        getString(R.string.sample_data_deleted_big_text), true)
  }

  override fun onClick(reminderData: ReminderData) {
    presenter.editReminder(reminderData)
  }

  override fun displayReminders(reminderList: List<ReminderData>) {
    runOnUiThread {
      recyclerView.visibility = View.VISIBLE
      textViewNoReminders.visibility = View.GONE
      mainAdapter = MainAdapter(this@MainActivity, reminderList)
      recyclerView.adapter = mainAdapter
      progressBar.visibility = View.GONE
      invalidateOptionsMenu()
    }
  }

  override fun displayEmptyState() {
    runOnUiThread {
      recyclerView.visibility = View.GONE
      textViewNoReminders.visibility = View.VISIBLE
      progressBar.visibility = View.GONE
      invalidateOptionsMenu()
    }
  }

  override fun displayCreateReminder() {
    //.. ReminderDialog for new reminders
    val reminderDialog = ReminderDialog.newInstance(null)
    reminderDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme)
    reminderDialog.show(supportFragmentManager, ReminderDialog.TAG)
  }

  override fun displayEditReminder(reminderData: ReminderData) {
    val args = Bundle()
    args.putParcelable(ReminderDialog.KEY_DATA, reminderData)
    val reminderDialog = ReminderDialog.newInstance(args)
    reminderDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme)
    reminderDialog.show(supportFragmentManager, ReminderDialog.TAG)
  }

  override fun displaySampleDataInserted(count: Int) {
    Snackbar.make(recyclerView, getString(R.string.snackbar_msg_sample_data_loaded, count), Snackbar.LENGTH_LONG)
        .setAction(getString(R.string.undo)) { backgroundHandler.post { deleteEverything() } }.show()

    backgroundHandler.post { presenter.scheduleAlarmsForSampleData(this@MainActivity) }
  }

  override fun onClose(opCode: Int, reminderData: ReminderData) {
    val message = when (opCode) {
      ReminderDialog.REMINDER_CREATED -> getString(R.string.created_reminder, reminderData.name)
      ReminderDialog.REMINDER_UPDATED -> getString(R.string.updated_reminder, reminderData.name)
      ReminderDialog.REMINDER_DELETED -> getString(R.string.deleted_reminder, reminderData.name)
      else -> getString(R.string.unknown_error)
    }
    Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show()
    loadData(0L)
  }

  private fun loadData(delayMillis: Long) {
    progressBar.visibility = View.VISIBLE
    backgroundHandler.postDelayed({ presenter.start() }, delayMillis) //.. simulate load time
  }
}