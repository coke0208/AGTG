package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.test.databinding.ActivityFoodBinding
import com.example.test.databinding.ActivityProductBinding
import com.example.test.productinfo.ProductDB
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator

class FoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodBinding
    private var textViewName: TextView? = null
    private var textViewAddress: TextView? = null
    private var Manufacturedate: TextView? = null
    private var Usebydate: TextView? = null
    private var info: TextView? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var userUid: String
    private lateinit var productList: ArrayList<ProductDB>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textViewName = findViewById<View>(R.id.textViewName) as TextView
        textViewAddress = findViewById<View>(R.id.imageViewAddress) as TextView
        Manufacturedate = findViewById<View>(R.id.Manufacturedate) as TextView
        Usebydate = findViewById<View>(R.id.Usebydate)as TextView
        info=findViewById<View>(R.id.textViewinfo)as TextView

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // 사용자가 로그인되어 있지 않은 경우 처리
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userUid = currentUser.uid
        val name=intent.getStringExtra("name")?:""
        val address=intent.getStringExtra("address")?:""
        val Prod=intent.getStringExtra("PROD")?:""
        val UsebyDate=intent.getStringExtra("UsebyDate")?:""
        val pinfo=intent.getStringExtra("info")?:""

        textViewName!!.text=name
        textViewAddress!!.text=address
        Manufacturedate!!.text=Prod
        Usebydate!!.text=UsebyDate
        info!!.text=pinfo

        val product = productList[position]

        binding.back.setOnClickListener {
            finish()
        }

        Glide.with(this)
            .load(product.address)
            .into(binding.imageViewAddress)
    }
}