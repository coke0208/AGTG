package com.example.test

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.test.databinding.ActivityFoodBinding

class FoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodBinding
    private var textViewName: TextView? = null
    private var textViewAddress: ImageView? = null
    private var Manufacturedate: TextView? = null
    private var Usebydate: TextView? = null
    private var info: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textViewName = findViewById(R.id.textViewName)
        textViewAddress = findViewById(R.id.imageViewAddress)
        Manufacturedate = findViewById(R.id.Manufacturedate)
        Usebydate = findViewById(R.id.Usebydate)
        info = findViewById(R.id.textViewinfo)

        val name = intent.getStringExtra("name") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val prod = intent.getStringExtra("PROD") ?: ""
        val usebyDate = intent.getStringExtra("UsebyDate") ?: ""
        val pinfo = intent.getStringExtra("info") ?: ""

        textViewName!!.text = name
        Manufacturedate!!.text = prod
        Usebydate!!.text = usebyDate
        info!!.text = pinfo

        // Glide로 이미지 로드
        Glide.with(this)
            .load(imageUrl)
            .into(textViewAddress!!)
    }
}
