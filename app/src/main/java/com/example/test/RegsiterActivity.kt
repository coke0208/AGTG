package com.example.test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityRegsiterBinding

class RegsiterActivity : AppCompatActivity() {
    val binding by lazy { ActivityRegsiterBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val finish = Intent(this, LoginActivity::class.java)
        binding.finish.setOnClickListener { startActivity(finish) }

        val back = Intent(this, LoginActivity::class.java)
        binding.back.setOnClickListener { startActivity(back) }
    }
}