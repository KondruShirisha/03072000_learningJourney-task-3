package com.example.myapplication_task3.models

import android.os.Parcel
import android.os.Parcelable

data class User (
    val id : String ="",
    val name: String = "",
    val email: String = "",
    val reenterPassword:String= "",
    val image: String = ""

        ):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(reenterPassword)
        writeString(image)
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
