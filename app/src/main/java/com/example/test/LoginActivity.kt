package com.example.test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val login = Intent(this, MainActivity::class.java)
        binding.login.setOnClickListener { startActivity(login) }

        val regsiter = Intent(this, RegsiterActivity::class.java)
        binding.regsiter.setOnClickListener { startActivity(regsiter) }

    }
}