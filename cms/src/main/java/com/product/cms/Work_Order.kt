// Функции класса work по работе с заказами
package com.product.cms

import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import kotlinx.android.synthetic.main.activity_work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray

// Первичная подготовка к выводу данных
fun work.fOrderInit() {
    swBC.visibility = View.GONE
    bLoading = false;
}

// Получение состава корзины
suspend fun work.fOrderGetList(){
    bLoading = true
    tlInfo.removeAllViews()
    etSearch.clearFocus()
    aOrders.clear()
    // Получаем состав заказа
    val client = HttpClient() {
        defaultRequest {
            header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
        }
    }
    val call = client.call(getString(R.string.server_uri)+getString(R.string.server_order)){
        method = HttpMethod.Post
        body = MultiPartFormDataContent(
            formData {
                append("exec", "getlistorders")
                append("login", sLogin)
            }
        )
    }
    val sRequest = call.response.readText()
    val JSONArray = JSONArray(sRequest)
    // Заголовок
    val tvTitle = TextView(this)
    tvTitle.setLayoutParams(
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT))
    tvTitle.setText("Заказы")
    tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    (tvTitle.layoutParams as LinearLayout.LayoutParams).setMargins(0,20,0,20)
    tvTitle.gravity = Gravity.CENTER
    tvTitle.setTextColor(Color.parseColor("#000000"))
    if (bStop and (sMode != "Orders")){
        bStop = false
        bLoading = false
        return
    }
    tlInfo.addView(tvTitle)
    // Выводим перечень заказов
    for (i in 0..(JSONArray.length()-1)){
        aOrders.add(mutableListOf(JSONArray.getJSONObject(i).getInt("iID"),0,0))
        val call = client.call(getString(R.string.server_uri)+getString(R.string.server_order)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(
                formData {
                    append("exec", "getorderattr")
                    append("id", JSONArray.getJSONObject(i).getInt("iID"))
                }
            )
        }
        val sRequest = call.response.readText()
        val JSONAttrArray = JSONArray(sRequest)
        var sOrderStatus = ""
        var sOrderData = ""
        for (j in 0..(JSONAttrArray.length()-1)){
            if (JSONAttrArray.getJSONObject(j).getString("sAttrName") == "Дата заказа"){
                sOrderData = JSONAttrArray.getJSONObject(j).getString("dValue")
            }
            if (JSONAttrArray.getJSONObject(j).getString("sAttrName") == "Статус"){
                sOrderStatus = JSONAttrArray.getJSONObject(j).getString("sValue")
            }
        }
        val sOrder = "№ " + JSONArray.getJSONObject(i).getString("iID") + " от " + sOrderData.convertDate() + "   Статус: " + sOrderStatus
        val tvOrder = TextView(this)
        tvOrder.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
        tvOrder.setText(sOrder)
        tvOrder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        tvOrder.setTypeface(null, Typeface.BOLD)
        (tvOrder.layoutParams as LinearLayout.LayoutParams).setMargins(10,20,10,20)
        tvOrder.setOnClickListener { view ->
            if (aOrders[i].get(1) == 0){ // Заказ свернут, разворачиваем
                // Сохраним виды в память
                GlobalScope.async(Dispatchers.Main) {
                    val vViewStore = mutableListOf<View>()
                    val iViewPos = tlInfo.indexOfChild(view) + 1
                    val iViewCount = tlInfo.childCount
                    for (j in iViewPos..(iViewCount - 1)) {
                        vViewStore.add(tlInfo.getChildAt(iViewPos))
                        tlInfo.removeViewAt(iViewPos)
                    }
                    // Получим состав заказа
                    val call = client.call(getString(R.string.server_uri)+getString(R.string.server_order)){
                        method = HttpMethod.Post
                        body = MultiPartFormDataContent(
                            formData {
                                append("exec", "getorder")
                                append("id", JSONArray.getJSONObject(i).getInt("iID"))
                            }
                        )
                    }
                    val sRequest = call.response.readText()
                    val JSONGoodArray = JSONArray(sRequest)
                    aOrders[i].set(2,JSONGoodArray.length())
                    for (j in 0..(JSONGoodArray.length() - 1)) {
                        // Атрибуты товаров
                        val call = client.call(getString(R.string.server_uri)+getString(R.string.server_good)){
                            method = HttpMethod.Post
                            body = MultiPartFormDataContent(
                                formData {
                                    append("exec", "getgoodattr")
                                    append("id", JSONGoodArray.getJSONObject(j).getInt("iIDGood"))
                                    append("brief", "0")
                                }
                            )
                        }
                        val sRequest = call.response.readText()
                        val JSONAttrArray = JSONArray(sRequest)
                        var iPriceIndex = -1
                        for (k in 0..(JSONAttrArray.length() - 1)) {
                            if (JSONAttrArray.getJSONObject(k).getString("sAttrName") == "Цена") {
                                iPriceIndex = k
                            }
                        }
                        val llGood = LinearLayout(this@fOrderGetList)
                        llGood.setLayoutParams(
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        )
                        llGood.orientation = LinearLayout.HORIZONTAL
                        val flGood = FrameLayout(this@fOrderGetList)
                        flGood.setLayoutParams(
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                0.2f
                            )
                        )
                        val tvGood = TextView(this@fOrderGetList)
                        tvGood.setLayoutParams(
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        )
                        tvGood.setText(JSONGoodArray.getJSONObject(j).getString("sName"))
                        tvGood.gravity = Gravity.CENTER
                        val flCount = FrameLayout(this@fOrderGetList)
                        flCount.setLayoutParams(
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                0.4f
                            )
                        )
                        val tvCount = TextView(this@fOrderGetList)
                        tvCount.setLayoutParams(
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                        )
                        tvCount.setText(JSONGoodArray.getJSONObject(j).getString("iCount") + " шт")
                        tvCount.gravity = Gravity.CENTER
                        val flPrice = FrameLayout(this@fOrderGetList)
                        flPrice.setLayoutParams(
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                0.4f
                            )
                        )
                        val tvPrice = TextView(this@fOrderGetList)
                        tvPrice.setLayoutParams(
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                        )
                        if (iPriceIndex >= 0) {
                            tvPrice.setText(
                                String.format(
                                    "%.2f",
                                    JSONAttrArray.getJSONObject(iPriceIndex).getDouble("fValue")
                                )
                            )
                        }
                        tvPrice.gravity = Gravity.CENTER
                        tlInfo.addView(llGood)
                        llGood.addView(flGood)
                        llGood.addView(flPrice)
                        llGood.addView(flCount)
                        flPrice.addView(tvPrice)
                        flGood.addView(tvGood)
                        flCount.addView(tvCount)
                    }
                    // Вставим виды обратно
                    for (j in 0..(vViewStore.count() - 1)) {
                        tlInfo.addView(vViewStore.get(j))
                    }
                    aOrders[i].set(1, 1)
                }
            }
            else { // Заказ развернут, сворачиваем
                for(j in 0..(aOrders[i].get(2)-1)){
                    tlInfo.removeViewAt(tlInfo.indexOfChild(view)+1)
                }
                aOrders[i].set(1,0)
                aOrders[i].set(2,0)
            }
        }
        if (bStop and (sMode != "Order")){
            bStop = false
            bLoading = false
            return
        }
        tlInfo.addView(tvOrder)
    }
    bLoading = false
}