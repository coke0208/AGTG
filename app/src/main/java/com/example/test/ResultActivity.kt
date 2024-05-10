package com.example.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.URLUtil.isValidUrl
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MainActivity로부터 전달된 스캔 결과를 가져옴
        val scannedResult = intent.getStringExtra("SCAN_RESULT")


        // 가져온 결과를 WebView에 표시
        binding.webview.settings.javaScriptEnabled = true // JavaScript 활성화

        // 스캔된 결과가 URL 형식인지 확인
        if (scannedResult != null) {
            //binding.webview.loadData(scannedResult, "text/html", "UTF-8")
            binding.webview.loadUrl(scannedResult)
        }
        else {
            // 스캔된 결과가 URL 형식이 아닌 경우 처리 (예: 오류 메시지 표시)
            //Toast.makeText(this, "스캔된 결과가 유효한 URL이 아닙니다.", Toast.LENGTH_SHORT).show()
        }
    }
}

