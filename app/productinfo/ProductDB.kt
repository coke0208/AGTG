package com.example.test.productinfo

import android.widget.CheckBox

data class ProductDB(
    var name:String?=null,
    var address:String?=null,
    var PROD:String?=null,
    var Usebydate:String?=null,
    var info:String?=null,
    var id: String="",
    var isChecked:Boolean = false
)