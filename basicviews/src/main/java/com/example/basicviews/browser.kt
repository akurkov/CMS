package com.example.basicviews

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_browser.*

class browser : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        val uri = intent.getStringExtra("uri")
        wvText.settings.javaScriptEnabled = true
        wvText.loadUrl(uri)
        wvText.webViewClient = WebViewClient()
    }
}
