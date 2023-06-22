package com.example.myapplication_task3.models

import android.os.Parcel
import android.os.Parcelable

data class User (
    val id : String ="",
    val name: String = "",
    val email: String = "",
    val reenterPassword:String= "",
    val latitude: Double=0.0,
    val longitude: Double=0.0,
    val address: String="",
    val dateTime :String=""
   ):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(reenterPassword)
        writeDouble(latitude)
        writeDouble(longitude)
        writeString(address)
        writeString(dateTime)
    }

    override fun describeContents()=0



    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
