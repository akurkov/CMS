package com.product.cms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray

class login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // При редактировании логина и пароля очищаем ошибку
        etLogin.setOnClickListener { tvAuthError.setText("") }
        etPass.setOnClickListener { tvAuthError.setText("") }
    }

    // При нажатии кнопки Назад свернем приложение
    override fun onBackPressed(){
        finishAffinity()
    }

    // Обработчик нажатия кнопки Войти
    fun onbtAuthClick(view: View){
        tvAuthError.setText("")
        if ((etLogin.text.length==0) or (etPass.text.length==0)){
            tvAuthError.setText("Не допускаются пустые логин или пароль")
        }
        GlobalScope.async(Dispatchers.Main) {
            fDoAuth()
        }
    }

    // Функция аутентификации
    suspend fun fDoAuth(){
        val client = HttpClient() {
            defaultRequest {
                header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
            }
        }
        val stRequest = "[" + client.get<String>(getString(R.string.server_uri)+getString(R.string.server_auth)+"?login="+etLogin.text+"&pass="+etPass.text).substringAfter("[", "[]")
        val JSONArray = JSONArray(stRequest)
        if (JSONArray.length()==0){
            tvAuthError.setText("Неверные логин или пароль")
        }
        else {
            // Надо сохранить токен
            val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
            val ed = spref.edit()
            val stEncodedValue = JSONArray.getJSONObject(0).getString("sToken").encrypt(getString(R.string.key_id))
            ed.putString("Value", stEncodedValue)
            ed.commit()
            // Пользователь с таким токеном найден, авторизация прошла
            val intent = Intent(this@login, work::class.java)
            intent.putExtra("login",JSONArray.getJSONObject(0).getString("sLogin"))
            intent.putExtra("role",JSONArray.getJSONObject(0).getString("sRole"))
            intent.putExtra("color",JSONArray.getJSONObject(0).getString("sColor"))
            startActivity(intent)
        }
    }
}
