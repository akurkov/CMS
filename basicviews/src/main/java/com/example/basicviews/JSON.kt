package com.example.basicviews

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class JSON : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_j_s_o_n)
    }

    override fun onStart() {
        super.onStart()
        val uri = intent.getStringExtra("uri")
        val sJSON = GlobalScope.launch {
            URL(uri).getText()
        }
    }
}

fun URL.getText(): String {
    return openConnection().run {
        this as HttpURLConnection
        inputStream.bufferedReader().readText()
    }
}