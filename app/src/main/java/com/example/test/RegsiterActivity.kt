package com.example.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityRegsiterBinding

import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegsiterActivity : AppCompatActivity() {
    val binding by lazy { ActivityRegsiterBinding.inflate(layoutInflater) }
    private lateinit var email: EditText
    private lateinit var pw: EditText
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        email = binding.email
        pw= binding.password

        binding.finish.setOnClickListener {
            var email = email.text.toString()
            var pw = pw.text.toString()

            auth = Firebase.auth
            init(email, pw)
        }

        binding.back.setOnClickListener {
            finish()
        }
    }
    private fun init(email: String, pw: String){

        if(email.isNullOrEmpty() || pw.isNullOrEmpty()) {
            Toast.makeText(this, "이메일 혹인 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
        }
        else {
            auth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "회원가입 완료", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "이메일 형식인지 확인 또는 비밀번호 6자리이상 입력해주세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}