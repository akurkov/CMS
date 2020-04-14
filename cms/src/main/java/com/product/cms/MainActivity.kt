package com.product.cms

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var stDecodedKey = "" // Android_ID
    var stDecodedValue = "" // User Token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получение настроек из файла настроек
        val spref = getPreferences(Context.MODE_PRIVATE)
        val stKey = spref.getString("Key","").toString()
        if (stKey.length>0){
            stDecodedKey = stKey.encrypt(getString(R.string.key_id))
            textView.setText(stDecodedKey)
        }
        val stValue = spref.getString("Value","").toString()
        if (stValue.length>0){
            stDecodedValue = stValue.encrypt(getString(R.string.key_id))
            textView.setText(stDecodedValue)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Сохранение настроек в файл настроек
        val spref = getPreferences(Context.MODE_PRIVATE)
        val ed = spref.edit()
        val stKey = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val stEncoded = stKey.encrypt(getString(R.string.key_id))
        ed.putString("Key", stEncoded)
        ed.commit()
    }
}

fun String.encrypt(stPass: String): String {
    var baXOR = StringBuilder()
    for (i in 0..(this.length-1)){
        baXOR.append((this[i].toInt() xor stPass[i % stPass.length].toInt()).toChar())
    }
    return baXOR.toString()
}