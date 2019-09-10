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

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Data representation for a reminder we schedule and issue notifications for.
 */
data class ReminderData(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("type")
    var type: PetType = PetType.Other,
    @SerializedName("medicine")
    var medicine: String? = null,
    @SerializedName("note")
    var note: String? = null,
    @SerializedName("desc")
    var desc: String? = null,
    @SerializedName("hour")
    var hour: Int = 0,
    @SerializedName("minute")
    var minute: Int = 0,
    @SerializedName("days")
    var days: Array<String?>? = null,
    @SerializedName("administered")
    var administered: Boolean = false
) : Parcelable {

  enum class PetType {
    Other,
    Cat,
    Dog
  }

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.apply {
      writeInt(id)
      writeString(name)
      writeString(type.name)
      writeString(medicine)
      writeString(note)
      writeString(desc)
      writeInt(hour)
      writeInt(minute)
      writeStringArray(days)
      writeInt(if (administered) 1 else 0)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ReminderData

    if (id != other.id) return false
    if (name != other.name) return false
    if (type != other.type) return false
    if (medicine != other.medicine) return false
    if (note != other.note) return false
    if (desc != other.desc) return false
    if (hour != other.hour) return false
    if (minute != other.minute) return false
    if (days != null) {
      if (other.days == null) return false
      if (!(days as Array).contentEquals(other.days as Array<out String>)) return false
    } else if (other.days != null) return false
    if (administered != other.administered) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id
    result = 31 * result + (name?.hashCode() ?: 0)
    result = 31 * result + type.hashCode()
    result = 31 * result + (medicine?.hashCode() ?: 0)
    result = 31 * result + (note?.hashCode() ?: 0)
    result = 31 * result + (desc?.hashCode() ?: 0)
    result = 31 * result + hour
    result = 31 * result + minute
    result = 31 * result + (days?.contentHashCode() ?: 0)
    result = 31 * result + administered.hashCode()
    return result
  }

  companion object CREATOR : Parcelable.Creator<ReminderData> {
    override fun createFromParcel(source: Parcel): ReminderData {
      return ReminderData().apply {
        id = source.readInt()
        name = source.readString()
        type = ReminderData.PetType.valueOf(source.readString())
        medicine = source.readString()
        note = source.readString()
        desc = source.readString()
        hour = source.readInt()
        minute = source.readInt()
        source.readStringArray(days)
        administered = source.readInt() == 1
      }
    }

    override fun newArray(size: Int): Array<ReminderData?> {
      return arrayOfNulls(size)
    }
  }


}