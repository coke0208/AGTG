package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityLoginBinding

import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private var db_data = ArrayList<String>()
    val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth







    override fun onCreate(savedInstanceState: Bundle?) {
        val id: EditText = binding.email
        val pw: EditText = binding.password
        auth = Firebase.auth

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val login = Intent(this, MainActivity::class.java)
        binding.login.setOnClickListener {
            login(id.text.toString(), pw.text.toString()) }

        val regsiter = Intent(this, RegsiterActivity::class.java)
        binding.regsiter.setOnClickListener { startActivity(regsiter) }
    }

    private fun login(id: String, pw: String) {

        if (id.isEmpty() || pw.isEmpty()) {
            Toast.makeText(this, "빈 값을 입력하셨습니다.", Toast.LENGTH_SHORT).show()
        } else {
            auth.signInWithEmailAndPassword(id, pw)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "로그인에 성공했습니다!", Toast.LENGTH_SHORT).show()

                        val main = Intent(this, MainActivity::class.java)
                        startActivity(main)
                        finish()
                    }
                    else {
                        Toast.makeText(this, "아이디와 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }


    var pressedTime : Long=0
    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (System.currentTimeMillis() - pressedTime <= 2000) {
            finish()
        } else {
            pressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

}




