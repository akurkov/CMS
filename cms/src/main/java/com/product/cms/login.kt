package com.product.cms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray

class login : AppCompatActivity() {

    // При создании лейаута
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // При редактировании логина и пароля очищаем ошибку
        etLogin.setOnClickListener { tvAuthError.setText("") }
        etPass.setOnClickListener { tvAuthError.setText("") }
    }

    // При нажатии кнопки Назад свернем приложение
    override fun onBackPressed(){
        // Если регистрация, возвращаемся к логину
        if (btAuth.text.toString() == getString(R.string.tvRegister)){
            tvAuthError.setText("")
            btAuth.setText(R.string.auth)
            tvRegister.visibility = View.VISIBLE
            etLogin.setText("")
            etPass.setText("")
        }
        else {
            finishAffinity()
        }
    }

    // Обработчик нажатия кнопок Войти и зарегистрироваться
    fun onbtAuthClick(view: View){
        tvAuthError.setText("")
        when (view.id){
            R.id.btAuth -> {
                // Если окно логина, то при непустых логине и пароле входим
                if (btAuth.text.toString()==getString(R.string.auth)) {
                    if ((etLogin.text.length == 0) or (etPass.text.length == 0)) {
                        tvAuthError.setText("Не допускаются пустые логин или пароль")
                    } else {
                        GlobalScope.async(Dispatchers.Main) {
                            fDoAuth()
                        }
                    }
                }
                // Если окно регистрации, регистрируемся и входим
                else {
                    if ((etLogin.text.length == 0) or (etPass.text.length == 0)) {
                        tvAuthError.setText("Не допускаются пустые логин или пароль")
                    } else {
                        GlobalScope.async(Dispatchers.Main) {
                            fDoRegister()
                        }
                    }
                }
            }
            // Меняем на регистрацию
            R.id.tvRegister -> {
                btAuth.setText(R.string.tvRegister)
                tvRegister.visibility = View.INVISIBLE
                etLogin.setText("")
                etPass.setText("")
            }
        }

    }

    // Функция аутентификации
    suspend fun fDoAuth(){
        val client = HttpClient() {
            defaultRequest {
                header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0"
                )
            }
        }
        val call = client.call(getString(R.string.server_uri)+getString(R.string.server_user)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(
                formData {
                    append("exec", "authlogin")
                    append("login", etLogin.text.toString())
                    append("pass", etPass.text.toString().encrypt(getString(R.string.key_id)))
                }
            )
        }
        val sRequest = call.response.readText()
        val JSONArray = JSONArray(sRequest)
        if (JSONArray.length() == 0){
            tvAuthError.setText("Неверные логин или пароль")
        }
        else {
            // Надо сохранить токен
            val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
            val ed = spref.edit()
            val sEncodedValue = JSONArray.getJSONObject(0).getString("sToken").encrypt(getString(R.string.key_id))
            ed.putString("Value", sEncodedValue)
            ed.commit()
            val intent = Intent(this@login, work::class.java)
            intent.putExtra("login",JSONArray.getJSONObject(0).getString("sLogin"))
            intent.putExtra("role",JSONArray.getJSONObject(0).getString("sRole"))
            intent.putExtra("color",JSONArray.getJSONObject(0).getString("sColor"))
            startActivity(intent)
        }
    }

    // Функция регистрации
    suspend fun fDoRegister(){
        val client = HttpClient() {
            defaultRequest {
                header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
            }
        }
        val call = client.call(getString(R.string.server_uri)+getString(R.string.server_user)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(
                formData {
                    append("exec", "register")
                    append("login", etLogin.text.toString())
                    append("pass", etPass.text.toString().encrypt(getString(R.string.key_id)))
                    append("role", "Покупатель")
                }
            )
        }
        val sRequest = call.response.readText()
        val JSONArray = JSONArray(sRequest)
        if (JSONArray.length()==0){
            tvAuthError.setText("Такой пользователь уже существует")
        }
        else {
            // Надо сохранить токен
            val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
            val ed = spref.edit()
            val sEncodedValue = JSONArray.getJSONObject(0).getString("sToken").encrypt(getString(R.string.key_id))
            ed.putString("Value", sEncodedValue)
            ed.commit()
            val intent = Intent(this@login, work::class.java)
            intent.putExtra("login",JSONArray.getJSONObject(0).getString("sLogin"))
            intent.putExtra("role",JSONArray.getJSONObject(0).getString("sRole"))
            intent.putExtra("color",JSONArray.getJSONObject(0).getString("sColor"))
            startActivity(intent)
        }
    }
}
