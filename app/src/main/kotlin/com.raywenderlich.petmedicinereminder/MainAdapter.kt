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

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.raywenderlich.petmedicinereminder.data.ReminderData
import java.text.SimpleDateFormat
import java.util.*

class MainAdapter(
    private val listener: Listener,
    private val reminderDataList: List<ReminderData>?
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

  private val dateFormat = SimpleDateFormat("h:mma",Locale.getDefault());

  interface Listener {
    fun onClick(reminderData: ReminderData)
  }

  override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
    val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_reminder_row, viewGroup, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
    if (reminderDataList != null) {
      val reminderData = reminderDataList[i]

      viewHolder.textViewName.text = reminderData.name
      viewHolder.textViewMedicine.text = reminderData.medicine
      val date = Calendar.getInstance()
      date.set(Calendar.HOUR_OF_DAY, reminderData.hour)
      date.set(Calendar.MINUTE, reminderData.minute)
      viewHolder.textViewTimeToAdminister.text = dateFormat.format(date.time).toLowerCase()

      var daysText = Arrays.toString(reminderData.days)
      daysText = daysText.replace("[", "")
      daysText = daysText.replace("]", "")
      daysText = daysText.replace(",", " ·")
      viewHolder.textViewDays.text = daysText

      val drawable = when {
        reminderData.type == ReminderData.PetType.Dog -> ContextCompat.getDrawable(viewHolder.imageViewIcon.context, R.drawable.dog)
        reminderData.type == ReminderData.PetType.Cat -> ContextCompat.getDrawable(viewHolder.imageViewIcon.context, R.drawable.cat)
        else -> ContextCompat.getDrawable(viewHolder.imageViewIcon.context, R.drawable.other)
      }
      viewHolder.imageViewIcon.setImageDrawable(drawable)

      viewHolder.checkBoxAdministered.isChecked = reminderData.administered

      viewHolder.itemView.setOnClickListener {
        listener.onClick(reminderData)
      }

    }
  }

  override fun getItemCount(): Int {
    return reminderDataList?.size ?: 0
  }


  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var imageViewIcon: ImageView
    var textViewName: TextView
    var textViewMedicine: TextView
    var textViewTimeToAdminister: TextView
    var textViewDays: TextView
    var checkBoxAdministered: CheckBox

    init {

      imageViewIcon = itemView.findViewById(R.id.imageViewIcon)
      textViewName = itemView.findViewById(R.id.textViewName)
      textViewMedicine = itemView.findViewById(R.id.textViewMedicine)
      textViewTimeToAdminister = itemView.findViewById(R.id.textViewTimeToAdminister)
      textViewDays = itemView.findViewById(R.id.textViewDays)
      checkBoxAdministered = itemView.findViewById(R.id.checkBoxAdministered)
    }
  }
}