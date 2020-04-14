package com.example.basicviews

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.android.synthetic.main.activity_browser.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray

class browser : AppCompatActivity() {

    var JSONArray = JSONArray() // Глобальный массив с данными, куда закачиваются данные при первом поиске. Нужно для перемещения между записями JSON-массива
    var JSONArrayIndex = 0 // Номер текущей записи массива

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        // Из интента получаем URL и тип вывода текста
        val uri = intent.getStringExtra("uri")
        val texttype = intent.getStringExtra("texttype")
        when(texttype){
            "HTML" -> {
                // страница HTML просто подключается к WebView
                wvText.tag = "HTTP"
                wvText.settings.javaScriptEnabled = true
                wvText.loadUrl(uri)
                wvText.webViewClient = WebViewClient()
                btBack.visibility = ImageButton.GONE
                btForward.visibility = ImageButton.GONE
            }
            "JSON" ->{
                btBack.visibility = ImageButton.VISIBLE
                btForward.visibility = ImageButton.VISIBLE
                // Асинхронный вызов получения JSON-данных и формаирование JSON-массива
                val job = GlobalScope.async(Dispatchers.Main) {
                    val client = HttpClient() {
                        defaultRequest {
                            header(
                                "User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0"
                            )
                        }
                    }
                    val stRequest = "[" + client.get<String>(uri).substringAfter("[", "[]")
                    JSONArray = JSONArray(stRequest)
                    wvText.tag = "JSON"
                    fSetWebView()
                }
            }
        }

    }

    fun onbtForwardClick(view: View){
        // При нажатии на кнопку Вперед увеличивается индекс записи JSON-массива и обновляется WebView
        if (wvText.tag == "JSON"){
            if (JSONArrayIndex < (JSONArray.length()-1)){
                JSONArrayIndex = JSONArrayIndex + 1
                fSetWebView()
            }
        }
    }

    fun onbtBackClick(view: View){
        // При нажатии на кнопку Назад уменьшается индекс записи JSON-массива и обновляется WebView
        if (wvText.tag == "JSON"){
            if (JSONArrayIndex > 0){
                JSONArrayIndex = JSONArrayIndex - 1
                fSetWebView()
            }
        }
    }

    // Перегрузка обработчика аппаратной кнопки Назад для хождения внутри WebView
    override fun onBackPressed() {
        if (wvText.canGoBack()){
            wvText.goBack()
        }
        else {
            super.onBackPressed()
        }
    }

    // Вывод текущей записи JSON-массива в виде таблицы HTML
    fun fSetWebView(){
        if (JSONArray.length()>0){
            var stHTML = "<head></head><body><table border=\"1\">"
            for (j in 0..(JSONArray.getJSONObject(JSONArrayIndex).names().length() - 1)) {
                stHTML = stHTML + "<tr>"
                stHTML = stHTML + "<td>" + JSONArray.getJSONObject(JSONArrayIndex).names().getString(j) + "</td>"
                if (JSONArray.getJSONObject(JSONArrayIndex).names().getString(j) == "Изображения") {
                    stHTML = stHTML + "<td><a href=\"" + JSONArray.getJSONObject(JSONArrayIndex).getString(JSONArray.getJSONObject(JSONArrayIndex).names().getString(j)
                        ) + "\">Изображение</a></td>"
                } else {
                    stHTML = stHTML + "<td>" + JSONArray.getJSONObject(JSONArrayIndex).getString(JSONArray.getJSONObject(JSONArrayIndex).names().getString(j)
                        ) + "</td>"
                }
                stHTML = stHTML + "</tr>"
            }
            stHTML = stHTML + "</table></body>"
            wvText.loadData(stHTML, "text/html", "UTF-8")
            wvText.webViewClient = WebViewClient()
        }
    }
}