// MypageActivity.kt
package com.example.test

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import android.widget.Toast
import java.util.UUID

@Suppress("DEPRECATION")
class MypageActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private val binding by lazy { ActivityMypageBinding.inflate(layoutInflater) }
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        loadUserInfo()

        binding.back.setOnClickListener {
            finish()
        }
        binding.logout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            auth?.signOut()
            finishAffinity()
        }

        binding.profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.update.setOnClickListener {
            updateUserInfo()
        }

        binding.defaultImage.setOnClickListener {
            binding.profileImage.setImageResource(R.drawable.my)
            imageUri = null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.profileImage.setImageURI(imageUri)
        }
    }

    private fun loadUserInfo() {
        val currentUser = auth?.currentUser
        currentUser?.let { user ->
            db.collection("users").whereEqualTo("email", user.email).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val name = document.getString("name")
                        val imageUrl = document.getString("imageUrl")
                        binding.name.setText(name)
                        if (imageUrl != null) {
                            Picasso.get().load(imageUrl).into(binding.profileImage)
                        } else {
                            binding.profileImage.setImageResource(R.drawable.my)
                        }
                        binding.emailId.setText(user.email)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "오류 발생: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserInfo() {
        val currentUser = auth?.currentUser
        currentUser?.let { user ->
            val name = binding.name.text.toString()
            val newPassword = binding.editTextTextPassword2.text.toString()
            val confirmPassword = binding.editTextTextPassword3.text.toString()

            if (newPassword.isNotEmpty()) {
                if (newPassword.length >= 6) {
                    if (newPassword == confirmPassword) {
                        user.updatePassword(newPassword)
                            .addOnSuccessListener {
                                uploadImageAndSaveUserInfo(name)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "비밀번호 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                uploadImageAndSaveUserInfo(name)
            }
        }
    }

    private fun uploadImageAndSaveUserInfo(name: String) {
        if (imageUri != null) {
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("images/$fileName")
            ref.putFile(imageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveUserInfo(name, imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveUserInfo(name, null)
        }
    }

    private fun saveUserInfo(name: String, imageUrl: String?) {
        val currentUser = auth?.currentUser
        currentUser?.let { user ->
            val userInfo = hashMapOf(
                "name" to name,
                "email" to user.email
            )
            if (imageUrl != null) {
                userInfo["imageUrl"] = imageUrl
            }
            db.collection("users").whereEqualTo("email", user.email).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("users").document(document.id).set(userInfo)
                            .addOnSuccessListener {
                                Toast.makeText(this, "정보 업데이트 완료", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "정보 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            Toast.makeText(this, "정보 업데이트 완료", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
}
