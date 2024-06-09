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
import com.example.test.productutils.ProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONException
import org.json.JSONObject

class ProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductBinding
    private var qrScan: IntentIntegrator? = null
    private var textViewName: TextView? = null
    private var textViewAddress: TextView? = null
    private var Manufacturedate: TextView? = null
    private var Usebydate: TextView? = null
    private var info: TextView? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var userUid: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
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

        // 사용자의 제품 데이터베이스 참조
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

        binding.btnSave.setOnClickListener {
            saveProduct("ColdStorage", ProductDB(name, address, Prod, UsebyDate, pinfo))
        }

        binding.btnFrozenSave.setOnClickListener {
            saveProduct("FrozenStorage", ProductDB(name, address, Prod, UsebyDate, pinfo))
        }

        binding.btnRoomSave.setOnClickListener {
            saveProduct("RoomStorage", ProductDB(name, address,  Prod, UsebyDate, pinfo))
        }


        // QR Code Scanner
        qrScan = IntentIntegrator(this)
        binding.add.setOnClickListener {
            qrScan!!.setPrompt("Scanning...")
            qrScan!!.initiateScan()
        }





    }

    @Deprecated("Deprecated in Java")
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
                    Manufacturedate!!.text = obj.getString("PROD")
                    Usebydate!!.text = obj.getString("Usebydate")
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

            // Ensure the product object is populated with data
            product.name = binding.textViewName.text.toString()
            product.address = binding.imageViewAddress.text.toString()
            product.PROD = binding.Manufacturedate.text.toString()
            product.Usebydate = binding.Usebydate.text.toString()
            product.info = binding.textViewinfo.text.toString()

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
