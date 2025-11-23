package com.bozkurt.geminiwebapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.webView)
        
        // Settings optimized for modern Chat Apps
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        // Keep links inside the WebView
        webView.webViewClient = WebViewClient()

        // Load a public URL for debugging
        webView.loadUrl("https://gemini.google.com")
    }
}