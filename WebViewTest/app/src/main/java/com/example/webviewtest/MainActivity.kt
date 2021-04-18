package com.example.webviewtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.FileUtils
import android.webkit.WebViewClient
import com.example.webviewtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webView.webViewClient = WebViewClient()

        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.javaScriptEnabled = true
        val url = getPdfUrl()
        binding.webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$url")
    }



    fun getPdfUrl(): String {
        return "http://www.germanyshoppingsquare.com/server/testfile/paypalthanhtoan.pdf"
    }
}