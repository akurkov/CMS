package com.product.cms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private var sKey = "" // Android_ID
    private var sValue = "" // User Token

    // При создании лейаута
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получение настроек из файла настроек
        val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
        val sEncodedKey = spref.getString("Key","").toString()
        sKey = if(sEncodedKey.isNotEmpty()) sEncodedKey.encrypt(getString(R.string.key_id)) else ""
        val sEncodedValue = spref.getString("Value","").toString()
        sValue = if(sEncodedValue.isNotEmpty()) sEncodedValue.encrypt(getString(R.string.key_id)) else ""

        // Загрузка на экране, пока не пройдет проверка аутентификации по-умолчанию
        wvBusy.loadUrl("file:///android_asset/busy.gif")
        wvBusy.settings.useWideViewPort = true
        wvBusy.settings.loadWithOverviewMode = true
        wvBusy.webViewClient = WebViewClient()
        GlobalScope.async(Dispatchers.Main){
            fgetToken()
        }
    }

    // При закрытии лейаута
    override fun onDestroy() {
        super.onDestroy()

        // Сохранение настроек в файл настроек
        val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
        val ed = spref.edit()
        val sKey = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val sEncodedKey = sKey.encrypt(getString(R.string.key_id))
        ed.putString("Key", sEncodedKey)
        ed.apply()
    }

    // Проверка пользователя по токену, автовход
    private suspend fun fgetToken(){
        val sKeyID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Проверка, совпадает ли ID устройства, если нет, то сразу к логину
        if ((sKey == sKeyID) and (sValue.isNotEmpty())) {
            // Получим пользователя по Токену
            val client = HttpClient {
                defaultRequest {
                    header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
                }
            }
            val call = client.call(getString(R.string.server_uri)+getString(R.string.server_user)){
                method = HttpMethod.Post
                body = MultiPartFormDataContent(
                    formData {
                        append("exec", "authtoken")
                        append("token", sValue)
                    }
                )
            }
            val sRequest = call.response.readText()
            val aJSONArray = JSONArray(sRequest)
            if (aJSONArray.length() > 0){
                // Пользователь с таким токеном найден, авторизация прошла
                val intent = Intent(this@MainActivity, Work::class.java)
                intent.putExtra("login", aJSONArray.getJSONObject(0).getString("sLogin"))
                intent.putExtra("role", aJSONArray.getJSONObject(0).getString("sRole"))
                intent.putExtra("color", aJSONArray.getJSONObject(0).getString("sColor"))
                startActivity(intent)
                return
            }
        }

        // Авторизация не прошла, переходим к окну ввода логина и пароля
        val intent = Intent(this@MainActivity, Login::class.java)
        startActivity(intent)
    }
}