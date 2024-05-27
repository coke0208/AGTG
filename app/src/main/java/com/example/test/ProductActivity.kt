package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityProductBinding
import com.example.test.databinding.FragmentAddBinding
import com.example.test.productinfo.FirebaseRF
import com.example.test.productinfo.ProductDB
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONException
import org.json.JSONObject


class ProductActivity : AppCompatActivity() {
    //view Objects
    private var btnSave: Button? = null
    private var title: TextView? = null
    private var content: TextView? = null
    private var info: TextView? = null
    private lateinit var binding: ActivityProductBinding
    private var qrScan: IntentIntegrator? = null


    private var mDatabase: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = findViewById<View>(R.id.etTitle) as TextView
        content = findViewById<View>(R.id.etContent) as TextView
        info = findViewById<View>(R.id.info) as TextView


        binding.back.setOnClickListener {
            finish()
        }

        qrScan = IntentIntegrator(this)
        //button onClick
        binding.btnAdd.setOnClickListener { //scan option
            qrScan!!.setPrompt("Scanning...")
            //qrScan.setOrientationLocked(false);
            qrScan!!.initiateScan()
        }

        //setContentView(R.layout.act)
        btnSave = binding.btnSave
        mDatabase = FirebaseDatabase.getInstance().getReference()
        btnSave!!.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.toString()
            val info = binding.info.text.toString()

            val productdb = ProductDB(title, content)
            FirebaseRF.productdb.child(title).setValue(productdb)
            Toast.makeText(this, "저장성공", Toast.LENGTH_LONG).show()
        }


    }

    //Getting the scan results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            //qrcode 가 없으면
            if (result.contents == null) {
                Toast.makeText(this@ProductActivity, "취소!", Toast.LENGTH_SHORT).show()
            } else {
                //qrcode 결과가 있으면
                Toast.makeText(this@ProductActivity, "스캔완료!", Toast.LENGTH_SHORT).show()
                try {
                    //data를 json으로 변환
                    val obj = JSONObject(result.contents)
                    title!!.text = obj.getString("name")
                    content!!.text = obj.getString("addres")
                } catch (e: JSONException) {
                    e.printStackTrace()
                    //Toast.makeText(MainActivity.this, result.getContents(), Toast.LENGTH_LONG).show();
                    info?.setText(result.contents)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}