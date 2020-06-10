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
import io.ktor.client.call.call
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import kotlinx.android.synthetic.main.activity_work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray

// Первичная подготовка к выводу данных
fun Work.fOrderInit() {
    if (bLoading){
        bStop = true
    }
    swBC.visibility = View.GONE
}

// Получение состава корзины
suspend fun Work.fOrderGetList(){
    bLoading = true
    tlInfo.removeAllViews()
    etSearch.clearFocus()
    aOrders.clear()
    // Получаем состав заказа
    var call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_order)){
        method = HttpMethod.Post
        body = MultiPartFormDataContent(
            formData {
                append("exec", "getlistorders")
                append("login", sLogin)
            }
        )
    }
    var sRequest = call.response.readText()
    val aJSONArray = JSONArray(sRequest)
    // Заголовок
    val tvTitle = TextView(this)
    tvTitle.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    tvTitle.text = "Заказы"
    tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    (tvTitle.layoutParams as LinearLayout.LayoutParams).setMargins(0,20,0,20)
    tvTitle.gravity = Gravity.CENTER
    tvTitle.setTextColor(Color.parseColor("#000000"))
    if (bStop){
        bStop = false
        return
    }
    tlInfo.addView(tvTitle)
    // Выводим перечень заказов
    for (i in 0 until aJSONArray.length()){
        aOrders.add(mutableListOf(aJSONArray.getJSONObject(i).getInt("iID"),0,0))
        call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_order)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(
                formData {
                    append("exec", "getorderattr")
                    append("id", aJSONArray.getJSONObject(i).getInt("iID"))
                }
            )
        }
        sRequest = call.response.readText()
        var aJSONAttrArray = JSONArray(sRequest)
        var sOrderStatus = ""
        var sOrderData = ""
        for (j in 0 until aJSONAttrArray.length()){
            if (aJSONAttrArray.getJSONObject(j).getString("sAttrName") == "Дата заказа"){
                sOrderData = aJSONAttrArray.getJSONObject(j).getString("dValue")
            }
            if (aJSONAttrArray.getJSONObject(j).getString("sAttrName") == "Статус"){
                sOrderStatus = aJSONAttrArray.getJSONObject(j).getString("sValue")
            }
        }
        val sOrder = "№ " + aJSONArray.getJSONObject(i).getString("iID") + " от " + sOrderData.convertDate() + "   Статус: " + sOrderStatus
        val tvOrder = TextView(this)
        tvOrder.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        tvOrder.text = sOrder
        tvOrder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        tvOrder.setTypeface(null, Typeface.BOLD)
        (tvOrder.layoutParams as LinearLayout.LayoutParams).setMargins(10,20,10,20)
        tvOrder.setOnClickListener { view ->
            if (bLoading){
                return@setOnClickListener
            }
            if (aOrders[i][1] == 0){ // Заказ свернут, разворачиваем
                bLoading = true
                GlobalScope.async(Dispatchers.Main) {
                    var iViewPos = tlInfo.indexOfChild(view) + 1
                    // Получим состав заказа
                    call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_order)){
                        method = HttpMethod.Post
                        body = MultiPartFormDataContent(
                            formData {
                                append("exec", "getorder")
                                append("id", aJSONArray.getJSONObject(i).getInt("iID"))
                            }
                        )
                    }
                    sRequest = call.response.readText()
                    val aJSONGoodArray = JSONArray(sRequest)
                    aOrders[i][2] = aJSONGoodArray.length()
                    for (j in 0 until aJSONGoodArray.length()) {
                        // Атрибуты товаров
                        call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_good)){
                            method = HttpMethod.Post
                            body = MultiPartFormDataContent(
                                formData {
                                    append("exec", "getgoodattr")
                                    append("id", aJSONGoodArray.getJSONObject(j).getInt("iIDGood"))
                                    append("brief", "0")
                                }
                            )
                        }
                        sRequest = call.response.readText()
                        aJSONAttrArray = JSONArray(sRequest)
                        var iPriceIndex = -1
                        for (k in 0 until aJSONAttrArray.length()) {
                            if (aJSONAttrArray.getJSONObject(k).getString("sAttrName") == "Цена") {
                                iPriceIndex = k
                            }
                        }
                        val llGood = LinearLayout(this@fOrderGetList)
                        llGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        llGood.orientation = LinearLayout.HORIZONTAL
                        val flGood = FrameLayout(this@fOrderGetList)
                        flGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f)
                        val tvGood = TextView(this@fOrderGetList)
                        tvGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        tvGood.text = aJSONGoodArray.getJSONObject(j).getString("sName")
                        tvGood.gravity = Gravity.CENTER
                        val flCount = FrameLayout(this@fOrderGetList)
                        flCount.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.4f)
                        val tvCount = TextView(this@fOrderGetList)
                        tvCount.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                        tvCount.text = aJSONGoodArray.getJSONObject(j).getString("iCount") + " шт"
                        tvCount.gravity = Gravity.CENTER
                        val flPrice = FrameLayout(this@fOrderGetList)
                        flPrice.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.4f)
                        val tvPrice = TextView(this@fOrderGetList)
                        tvPrice.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                        if (iPriceIndex >= 0) {
                            tvPrice.text = String.format("%.2f", aJSONAttrArray.getJSONObject(iPriceIndex).getDouble("fValue"))
                        }
                        tvPrice.gravity = Gravity.CENTER
                        if (bStop){
                            bStop = false
                            return@async
                        }
                        tlInfo.addView(llGood, iViewPos)
                        llGood.addView(flGood)
                        llGood.addView(flPrice)
                        llGood.addView(flCount)
                        flPrice.addView(tvPrice)
                        flGood.addView(tvGood)
                        flCount.addView(tvCount)
                        iViewPos ++
                    }
                    aOrders[i][1] = 1
                    bLoading = false
                }
            }
            else { // Заказ развернут, сворачиваем
                if (bStop){
                    bStop = false
                    return@setOnClickListener
                }
                bLoading = true
                for(j in 0 until aOrders[i][2]){
                    tlInfo.removeViewAt(tlInfo.indexOfChild(view)+1)
                }
                aOrders[i][1] = 0
                aOrders[i][2] = 0
                bLoading = false
            }
        }
        if (bStop){
            bStop = false
            return
        }
        tlInfo.addView(tvOrder)
    }
    bLoading = false
}