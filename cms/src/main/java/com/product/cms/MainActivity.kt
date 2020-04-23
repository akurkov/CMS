package com.product.cms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    var stKey = "" // Android_ID
    var stValue = "" // User Token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получение настроек из файла настроек
        val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
        val stEncodedKey = spref.getString("Key","").toString()
        stKey = if(stEncodedKey.length>0) stEncodedKey.encrypt(getString(R.string.key_id)) else ""
        val stEncodedValue = spref.getString("Value","").toString()
        stValue = if(stEncodedValue.length>0) stEncodedValue.encrypt(getString(R.string.key_id)) else ""

        // Загрузка на экране, пока не пройдет проверка аутентификации по-умолчанию
        wvBusy.loadUrl("file:///android_asset/busy.gif")
        wvBusy.settings.useWideViewPort = true
        wvBusy.settings.loadWithOverviewMode = true
        wvBusy.webViewClient = WebViewClient()
        GlobalScope.async(Dispatchers.Main){
            fgetToken()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Сохранение настроек в файл настроек
        val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
        val ed = spref.edit()
        val stKey = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val stEncodedKey = stKey.encrypt(getString(R.string.key_id))
        ed.putString("Key", stEncodedKey)
        ed.commit()
    }

    suspend fun fgetToken(){
        val stKeyID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Проверка, совпадает ли ID устройства, если нет, то сразу к логину
        if ((stKey == stKeyID) and (stValue.length>0)) {
            // Получим пользователя по Токену
            val client = HttpClient() {
                defaultRequest {
                    header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
                }
            }
            val stRequest = "[" + client.get<String>(getString(R.string.server_uri)+getString(R.string.server_user)+"?exec=authtoken&token="+stValue).substringAfter("[", "[]")
            val JSONArray = JSONArray(stRequest)
            if (JSONArray.length()>0){
                // Пользователь с таким токеном найден, авторизация прошла
                val intent = Intent(this@MainActivity, work::class.java)
                intent.putExtra("login",JSONArray.getJSONObject(0).getString("sLogin"))
                intent.putExtra("role",JSONArray.getJSONObject(0).getString("sRole"))
                intent.putExtra("color",JSONArray.getJSONObject(0).getString("sColor"))
                startActivity(intent)
                return
            }
        }
        val intent = Intent(this@MainActivity, login::class.java)
        startActivity(intent)
    }
}