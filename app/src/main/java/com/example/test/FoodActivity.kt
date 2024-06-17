package com.example.test

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityFoodBinding

class FoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodBinding
    private var textViewName: TextView? = null
    private var textViewAddress: TextView? = null
    private var Manufacturedate: TextView? = null
    private var Usebydate: TextView? = null
    private var info: TextView? = null
    //private var nutrition:TextView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textViewName = findViewById(R.id.textViewName)
        textViewAddress = findViewById(R.id.imageViewAddress)
        Manufacturedate = findViewById(R.id.Manufacturedate)
        Usebydate = findViewById(R.id.Usebydate)
        info = findViewById(R.id.textViewinfo)
        //nutrition=findViewById(R.id.nutrition)

        val name = intent.getStringExtra("name") ?: ""
        val address = intent.getStringExtra("address") ?: ""
        val Prod = intent.getStringExtra("PROD") ?: ""
        val UsebyDate = intent.getStringExtra("UsebyDate") ?: ""
        val pinfo = intent.getStringExtra("info") ?: ""
       // val nutrinfo=intent.getStringExtra("nutrition")?:""

        textViewName!!.text = name
        textViewAddress!!.text = address
        Manufacturedate!!.text = Prod
        Usebydate!!.text = UsebyDate
        info!!.text = pinfo
        //nutrition!!.text=nutrinfo
    }
}