package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityProductBinding
import com.example.test.productinfo.ProductDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONException
import org.json.JSONObject

class ProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductBinding
    private var qrScan: IntentIntegrator? = null
    private var textViewName: TextView? = null
    private var textViewAddress: TextView? = null
    private var textViewedate: TextView? = null
    private var textViewcdate: TextView? = null
    private var info: TextView? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var userUid: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textViewName = findViewById<View>(R.id.textViewName) as TextView
        textViewAddress = findViewById<View>(R.id.imageViewAddress) as TextView
        textViewedate = findViewById<View>(R.id.textViewedate) as TextView
        textViewcdate = findViewById<View>(R.id.textViewcdate)as TextView
        info=findViewById<View>(R.id.textViewinfo)as TextView

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // 사용자가 로그인되어 있지 않은 경우 처리
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 사용자의 제품 데이터베이스 참조
        userUid = currentUser.uid
        val name=intent.getStringExtra("name")
        val address=intent.getStringExtra("address")
        val edate=intent.getStringExtra("edate")
        val cdate=intent.getStringExtra("cdate")
        val pinfo=intent.getStringExtra("info")

        textViewName!!.text=name
        textViewAddress!!.text=address
        textViewedate!!.text=edate
        textViewcdate!!.text=cdate
        info!!.text=pinfo

        binding.btnSave.setOnClickListener {
            saveProduct("ColdStorage", ProductDB(name, address, cdate, edate, pinfo))
        }

        binding.btnFrozenSave.setOnClickListener {
            saveProduct("FrozenStorage", ProductDB(name, address, cdate, edate, pinfo))
        }

        binding.btnRoomSave.setOnClickListener {
            saveProduct("RoomStorage", ProductDB(name, address, cdate, edate, pinfo))
        }


        // QR Code Scanner
        qrScan = IntentIntegrator(this)
        binding.add.setOnClickListener {
            qrScan!!.setPrompt("Scanning...")
            qrScan!!.initiateScan()
        }





    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this@ProductActivity, "취소!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ProductActivity, "스캔완료!", Toast.LENGTH_SHORT).show()
                try {
                    val obj = JSONObject(result.contents)
                    textViewName!!.text = obj.getString("name")
                    textViewAddress!!.text = obj.getString("address")
                    textViewedate!!.text = obj.getString("edate")
                    textViewcdate!!.text = obj.getString("cdate")
                    info!!.text = obj.getString("info")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun saveProduct(storageType: String, product: ProductDB) {
        try {
            val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
                .getReference("users").child(userUid).child("products").child(storageType)
            val newProductRef = databaseReference.push()
            product.id = newProductRef.key.toString()  // Assigning ID to product
            newProductRef.setValue(product).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "저장 성공", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("ProductActivity", "Firebase 저장 실패: ${task.exception}")
                    Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("ProductActivity", "Exception: ${e.message}", e)
            Toast.makeText(this, "예외 발생: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



}
