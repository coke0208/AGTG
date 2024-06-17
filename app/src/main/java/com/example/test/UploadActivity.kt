package com.example.test

import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.test.databinding.ActivityUploadBinding
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.IOException
import java.io.OutputStream

class UploadActivity : AppCompatActivity() {
    private var calender = Calendar.getInstance()
    private var year = calender.get(Calendar.YEAR)
    private var month = calender.get(Calendar.MONTH)
    private var day = calender.get(Calendar.DAY_OF_MONTH)

    val binding by lazy { ActivityUploadBinding.inflate(layoutInflater) }
    private var qrCodeBitmap: Bitmap? = null

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        qrCodeBitmap?.let { saveImageToGallery(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        binding.generateQRCodeButton.setOnClickListener {
            val makeName = binding.makeName.text.toString()
            val makeAddres = binding.makeAddres.text.toString()
            val makeSdate = binding.makeSdate.text.toString()
            val makeEdate = binding.makeEdate.text.toString()
            val makeInfo = binding.makeInfo.text.toString()

            if (makeName.isEmpty() || makeAddres.isEmpty() || makeSdate.isEmpty() || makeEdate.isEmpty() || makeInfo.isEmpty()) {
                Toast.makeText(this, "모든 값을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jsonData = mapOf(
                "name" to makeName,
                "address" to makeAddres,
                "PROD" to makeSdate,
                "Usebydate" to makeEdate,
                "info" to makeInfo
            )

            val jsonString = Gson().toJson(jsonData)
            qrCodeBitmap = generateQRCode(jsonString)
            binding.qrCodeImageView.setImageBitmap(qrCodeBitmap)
        }

        binding.qrCodeImageView.setOnClickListener {
            if (qrCodeBitmap != null) {
                checkAndRequestPermissions()
            } else {
                Toast.makeText(this, "먼저 QR 코드를 생성하세요", Toast.LENGTH_SHORT).show()
            }
        }
        binding.sCalendar.setOnClickListener(){
            val datePickerDialog = DatePickerDialog(this, {_,year, month, day ->
                binding.makeSdate.text = year.toString() + "-" + (month+1).toString() + "-" + day.toString()
            }, year, month, day)
            datePickerDialog.show()
        }

        binding.eCalendar.setOnClickListener(){
            val datePickerDialog = DatePickerDialog(this, {_,year, month, day ->
                binding.makeEdate.text = year.toString() + "-" + (month+1).toString() + "-" + day.toString()
            }, year, month, day)
            datePickerDialog.show()
        }
    }

    private fun generateQRCode(text: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionsLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        } else {
            qrCodeBitmap?.let { saveImageToGallery(it) }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "QRCode_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/QRCode")
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            try {
                val outputStream: OutputStream = contentResolver.openOutputStream(uri)!!
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                Toast.makeText(this, "이미지가 갤러리에 저장되었습니다", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "이미지 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "이미지 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
        }
    }
}
