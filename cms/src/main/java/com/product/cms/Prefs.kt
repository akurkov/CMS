package com.product.cms

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray

class Prefs : AppCompatActivity() {

    var sLogin = "" // Имя пользователя
    var sMode = "" // Режим обображения настроек
    var sColor = "" // Цвет заголовка окна роли

    // Создание лейаута
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prefs)
        sLogin = intent.getStringExtra("login")
        sMode = intent.getStringExtra("mode")
        sColor = intent.getStringExtra("color")
        // Установим цвет ActionBar в зависимости от роли
        if (sColor.length > 6) {
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor(sColor)))
        }
        supportActionBar!!.title = sMode
        if (sMode == "Профиль"){
            GlobalScope.async(Dispatchers.Main) {
                fsetProfile()
            }
        }
    }

    // Отображение настроек пользователя
    suspend fun fsetProfile(){
        val client = HttpClient() {
            defaultRequest {
                header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
            }
        }
        val call = client.call(getString(R.string.server_uri)+getString(R.string.server_user)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(
                formData {
                    append("exec", "getprofile")
                    append("login", sLogin)
                }
            )
        }
        val sRequest = call.response.readText()
        val JSONArray = JSONArray(sRequest)
        Toast.makeText(this,JSONArray.getJSONObject(0).getString("iID") + " " + JSONArray.getJSONObject(0).getString("sRole"),Toast.LENGTH_SHORT).show()
    }

}
