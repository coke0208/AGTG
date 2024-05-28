package com.example.test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityMypageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MypageActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null
    val binding by lazy { ActivityMypageBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.back.setOnClickListener {
            finish()
        }
        binding.button3.setOnClickListener {
            // 로그인 화면으로
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            auth?.signOut()
            finishAffinity()
        }
    }
}