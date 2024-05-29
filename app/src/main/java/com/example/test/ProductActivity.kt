package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityProductBinding
import com.example.test.productinfo.ProductDB
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textViewName = findViewById<View>(R.id.textViewName) as TextView
        textViewAddress = findViewById<View>(R.id.textViewaddres) as TextView
        textViewedate = findViewById<View>(R.id.textViewedate) as TextView
        textViewcdate = findViewById<View>(R.id.textViewcdate)as TextView
        info=findViewById<View>(R.id.textViewinfo)as TextView

        binding.btnSave.setOnClickListener {
            saveProductData("ColdStorage")
            //startFragmentActivity("ColdStorage")
        }

        binding.btnFrozenSave.setOnClickListener {
            saveProductData("FrozenStorage")
            //startFragmentActivity("FrozenStorage")
        }

        binding.btnRoomSave.setOnClickListener {
            saveProductData("RoomStorage")
            //startFragmentActivity("RoomStorage")
        }

        val name=intent.getStringExtra("name")
        val address=intent.getStringExtra("addres")
        val edate=intent.getStringExtra("edate")
        val cdate=intent.getStringExtra("cdate")
        val pinfo=intent.getStringExtra("info")

        textViewName!!.text=name
        textViewAddress!!.text=address
        textViewedate!!.text=edate
        textViewcdate!!.text=cdate
        info!!.text=pinfo


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
                    textViewAddress!!.text = obj.getString("addres")
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

    private fun saveProductData(storageType: String) {
        val name = binding.textViewName.text.toString()
        val address = binding.textViewaddres.text.toString()
        val cdate = binding.textViewcdate.text.toString()
        val edate = binding.textViewedate.text.toString()
        val info = binding.textViewinfo.text.toString()

        val product = ProductDB(name, address, edate, cdate, info)

        val databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference(storageType)

        databaseReference.push().setValue(product).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "저장성공", Toast.LENGTH_LONG).show()
                clearInputFields()
            } else {
                Toast.makeText(this, "저장실패", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun clearInputFields() {
        binding.textViewName.text.clear()
        binding.textViewaddres.text.clear()
        binding.textViewcdate.text.clear()
        binding.textViewedate.text.clear()
        binding.textViewinfo.text.clear()
    }

    /*private fun startFragmentActivity(storageType: String) {
        val intent = Intent(this, HomeFragment::class.java)
        intent.putExtra("storageType", storageType)
        startActivity(intent)
    }*/
}
