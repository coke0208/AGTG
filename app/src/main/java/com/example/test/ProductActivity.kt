package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityProductBinding
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
    private lateinit var binding: ActivityProductBinding
    private var textViewName: TextView? = null
    private var textViewAddres: TextView? = null
    private var textViewedate: TextView? = null
    private var textViewcdate: TextView? = null
    private var textViewinfo: TextView? = null

    private var mDatabase: DatabaseReference? = null
    private var btnSave: Button? = null

    //qr code scanner object
    private var qrScan: IntentIntegrator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //View Objects
        textViewName = findViewById<View>(R.id.textViewName) as TextView
        textViewAddres = findViewById<View>(R.id.textViewaddres) as TextView
        textViewedate = findViewById<View>(R.id.textViewedate) as TextView
        textViewcdate = findViewById<View>(R.id.textViewcdate) as TextView
        textViewinfo = findViewById<View>(R.id.textViewinfo) as TextView

        //intializing scan object
        qrScan = IntentIntegrator(this)

        //button onClick
        binding.add.setOnClickListener(){ //scan option
            qrScan!!.setPrompt("Scanning...")
            //qrScan.setOrientationLocked(false);
            qrScan!!.initiateScan()
        }
        btnSave = binding.btnSave
        mDatabase = FirebaseDatabase.getInstance().getReference()
        btnSave!!.setOnClickListener {
            val name = binding.textViewName.text.toString()
            val addres = binding.textViewaddres.text.toString()
            val edate = binding.textViewedate.text.toString()
            val cdate = binding.textViewcdate.text.toString()
            val info = binding.textViewinfo.text.toString()


            val productdb= ProductDB(name,addres,edate,cdate,info)
            FirebaseRF.productdb.child(name).setValue(productdb)
            Toast.makeText(this,"저장성공",Toast.LENGTH_LONG).show()
        }
    }

    //Getting the scan results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            //qrcode 가 없으면
            if (result.getContents() == null) {
                Toast.makeText(this@ProductActivity, "취소!", Toast.LENGTH_SHORT).show()
            } else {
                //qrcode 결과가 있으면
                Toast.makeText(this@ProductActivity, "스캔완료!", Toast.LENGTH_SHORT).show()
                try {
                    //data를 json으로 변환
                    val obj: JSONObject = JSONObject(result.getContents())
                    textViewName!!.text = obj.getString("name")
                    textViewAddres!!.text = obj.getString("addres")
                    textViewedate!!.text = obj.getString("edate")
                    textViewcdate!!.text = obj.getString("cdate")
                    textViewinfo!!.text = obj.getString("info")

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}