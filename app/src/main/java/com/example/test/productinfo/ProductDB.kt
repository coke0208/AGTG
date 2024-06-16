package com.example.test.productinfo

import android.os.Parcel
import android.os.Parcelable

data class ProductDB(
    var name: String? = null,
    var address: String? = null,
    var PROD: String? = null,
    var Usebydate: String? = null,
    var info: String? = null,
    var id: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(PROD)
        parcel.writeString(Usebydate)
        parcel.writeString(info)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductDB> {
        override fun createFromParcel(parcel: Parcel): ProductDB {
            return ProductDB(parcel)
        }

        override fun newArray(size: Int): Array<ProductDB?> {
            return arrayOfNulls(size)
        }
    }
}
