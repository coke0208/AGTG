package com.example.test.productinfo

import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseRF {

    companion object{
        val database = Firebase.database("https://sukbinggotest-default-rtdb.firebaseio.com/")
        val productdb= database.getReference("ProductDB")
    }

}