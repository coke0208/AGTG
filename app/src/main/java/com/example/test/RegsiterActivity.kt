package com.example.test

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityRegsiterBinding
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

@Suppress("DEPRECATION")
class RegsiterActivity : AppCompatActivity() {
    val binding by lazy { ActivityRegsiterBinding.inflate(layoutInflater) }
    private lateinit var email: EditText
    private lateinit var pw: EditText
    private lateinit var samePw: EditText
    private lateinit var name: EditText
    private lateinit var imageView: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null
    private var isDefaultImage = false // 기본 이미지 여부를 나타내는 변수 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        email = binding.email
        pw = binding.password
        samePw = binding.samepassword
        name = binding.name
        imageView = binding.imageView5

        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        binding.finish.setOnClickListener {
            val emailText = email.text.toString()
            val pwText = pw.text.toString()
            val samePwText = samePw.text.toString()
            val nameText = name.text.toString()

            if (emailText.isNotEmpty() && pwText.isNotEmpty() && nameText.isNotEmpty() && samePwText.isNotEmpty() && (imageUri != null || isDefaultImage)) {
                if (pwText.length >= 6) {
                    if (pwText == samePwText) {
                        checkEmailAndRegister(emailText, pwText, nameText)
                    } else {
                        Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        binding.back.setOnClickListener {
            finish()
        }

        binding.same.setOnClickListener {
            val emailText = email.text.toString()
            if (emailText.isNotEmpty()) {
                checkEmailExists(emailText)
            } else {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.defaultImage.setOnClickListener {
            imageView.setImageResource(R.drawable.my)
            imageUri = null
            isDefaultImage = true // 기본 이미지 설정 시 true로 설정
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
            isDefaultImage = false // 선택한 이미지가 있을 때 false로 설정
        }
    }

    private fun checkEmailExists(email: String) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "사용 가능한 이메일입니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "이미 존재하는 이메일입니다", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "오류 발생: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkEmailAndRegister(email: String, password: String, name: String) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    uploadImageAndRegisterUser(email, password, name)
                } else {
                    Toast.makeText(this, "이미 존재하는 이메일입니다", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "오류 발생: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageAndRegisterUser(email: String, password: String, name: String) {
        if (imageUri != null) {
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("images/$fileName")
            ref.putFile(imageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        registerUser(email, password, name, imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // 기본 이미지 URL 사용
            val defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/YOUR_FIREBASE_STORAGE_BUCKET/o/default_image.png?alt=media"
            registerUser(email, password, name, defaultImageUrl)
        }
    }

    private fun registerUser(email: String, password: String, name: String, imageUrl: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                val user = hashMapOf(
                    "email" to email,
                    "name" to name,
                    "imageUrl" to imageUrl
                )
                db.collection("users").document(userId).set(user)
                    .addOnSuccessListener {
                        initializeStorageCollections(userId)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "회원정보 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                Toast.makeText(this, "회원가입 완료", Toast.LENGTH_SHORT).show()
                finish()

            } else {
                val exception = task.exception
                val errorMessage = exception?.message
                Toast.makeText(this, "회원가입 실패: $errorMessage", Toast.LENGTH_SHORT).show()
            }

        }
    }

    //사용자별 데이터
    private fun initializeStorageCollections(userId: String){
        val storageCollections= listOf("ColdStorage","FrozenStorage","RoomStorage")
        storageCollections.forEach{collection->
            db.collection("users").document(userId).collection(collection).add(hashMapOf("initialized" to true))
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "$collection 초기화 완료")
                }
                .addOnFailureListener{e->
                    Toast.makeText(this, "$collection 초기화 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
}
