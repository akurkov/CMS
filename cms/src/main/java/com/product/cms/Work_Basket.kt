// Функции класса work по работе с корзиной
package com.product.cms

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
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
fun work.fBasketInit() {
    swBC.visibility = View.GONE
    bLoading = false;
}

// Получение состава корзины
suspend fun work.fBasketGetList(){
    bLoading = true
    tlInfo.removeAllViews()
    etSearch.clearFocus()
    // Получаем состав корзины
    val client = HttpClient() {
        defaultRequest {
            header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
        }
    }
    val call = client.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
        method = HttpMethod.Post
        body = MultiPartFormDataContent(
            formData {
                append("exec", "getbasket")
                append("login", sLogin)
            }
        )
    }
    val sRequest = call.response.readText()
    val JSONArray = JSONArray(sRequest)
    // Заголовок
    val tvTitle = TextView(this)
    tvTitle.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
    tvTitle.setText("Корзина")
    tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    (tvTitle.layoutParams as LinearLayout.LayoutParams).setMargins(0,20,0,20)
    tvTitle.gravity = Gravity.CENTER
    tvTitle.setTextColor(Color.parseColor("#000000"))
    if (bStop and (sMode != "Basket")){
        bStop = false
        bLoading = false
        return
    }
    tlInfo.addView(tvTitle)
    // Кнопка очистки корзины
    val tvClear = TextView(this)
    tvClear.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
    tvClear.setText("Очистить")
    tvClear.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    (tvClear.layoutParams as LinearLayout.LayoutParams).setMargins(0,20,30,20)
    tvClear.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.cancel,0)
    tvClear.setTextColor(Color.parseColor("#696969"))
    tvClear.gravity = Gravity.RIGHT
    tvClear.setOnClickListener { view ->
        GlobalScope.async(Dispatchers.Main){
            val call = client.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
                method = HttpMethod.Post
                body = MultiPartFormDataContent(
                    formData {
                        append("exec", "delgood")
                        append("login", sLogin)
                    }
                )
            }
            val sRequest = call.response.readText()
            if (sRequest.toInt() == 0){
                fBasketInit()
                fBasketGetList()
            }
        }
    }
    if (bStop and (sMode != "Basket")){
        bStop = false
        bLoading = false
        return
    }
    tlInfo.addView(tvClear)
    // Товары
    for (i in 0..(JSONArray.length()-1)){
        // Атрибуты товаров
        val call = client.call(getString(R.string.server_uri)+getString(R.string.server_good)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(
                formData {
                    append("exec", "getgoodattr")
                    append("id", JSONArray.getJSONObject(i).getInt("iIDGood"))
                    append("brief", "0")
                }
            )
        }
        val sRequest = call.response.readText()
        val JSONAttrArray = JSONArray(sRequest)
        var iPriceIndex = -1
        for (j in 0..(JSONAttrArray.length()-1)){
            if (JSONAttrArray.getJSONObject(j).getString("sAttrName") == "Цена"){
                iPriceIndex = j
            }
        }
        val llGood = LinearLayout(this)
        llGood.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
        llGood.orientation = LinearLayout.HORIZONTAL
        val flGood = FrameLayout(this)
        flGood.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,0.2f))
        val tvGood = TextView(this)
        tvGood.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
        tvGood.setText(JSONArray.getJSONObject(i).getString("sName"))
        tvGood.gravity = Gravity.CENTER
        val flPrice = FrameLayout(this)
        flPrice.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,0.8f))
        val tvPrice = TextView(this)
        tvPrice.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT))
        if (iPriceIndex >= 0) {
            tvPrice.setText(String.format("%.2f",JSONAttrArray.getJSONObject(iPriceIndex).getDouble("fValue")))
        }
        tvPrice.gravity = Gravity.CENTER
        if (bStop and (sMode != "Basket")){
            bStop = false
            bLoading = false
            return
        }
        tlInfo.addView(llGood)
        llGood.addView(flGood)
        llGood.addView(flPrice)
        flPrice.addView(tvPrice)
        flGood.addView(tvGood)
        // Кнопки
        val llShop = LinearLayout(this)
        llShop.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
        val iwMinus = ImageView(this)
        iwMinus.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f))
        iwMinus.setImageResource(R.drawable.minus)
        iwMinus.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iwMinus.setOnClickListener{ view ->
            var sCountText = (((iwMinus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString()
            if (sCountText != "0"){
                sCountText = (sCountText.toInt() - 1).toString()
                GlobalScope.async(Dispatchers.Main) {
                    val call = client.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
                        method = HttpMethod.Post
                        body = MultiPartFormDataContent(
                            formData {
                                append("exec", "recount")
                                append("id", JSONArray.getJSONObject(i).getInt("iID"))
                                append("count", sCountText)
                            }
                        )
                    }
                    val sRequest = call.response.readText()
                    if (sRequest.toInt() == 0) {
                        (((iwMinus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(
                            0
                        ) as TextView).setText(sCountText)
                    }
                }
            }
        }
        llShop.addView(iwMinus)
        val flCount = FrameLayout(this)
        val tvCount = TextView(this)
        tvCount.setText(JSONArray.getJSONObject(i).getString("iCount"))
        tvCount.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT))
        tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f)
        (tvCount.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.CENTER
        flCount.addView(tvCount)
        llShop.addView(flCount)
        val iwPlus = ImageView(this)
        iwPlus.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f))
        iwPlus.setImageResource(R.drawable.plus)
        iwPlus.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iwPlus.setOnClickListener{ view ->
            var sCountText = (((iwMinus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString()
            sCountText = (sCountText.toInt() + 1).toString()
            GlobalScope.async(Dispatchers.Main) {
                val call = client.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
                    method = HttpMethod.Post
                    body = MultiPartFormDataContent(
                        formData {
                            append("exec", "recount")
                            append("id", JSONArray.getJSONObject(i).getInt("iID"))
                            append("count", sCountText)
                        }
                    )
                }
                val sRequest = call.response.readText()
                if (sRequest.toInt() == 0) {
                    (((iwMinus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).setText(sCountText)
                }
            }
        }
        llShop.addView(iwPlus)
        var iwShop = ImageView(this)
        iwShop.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f))
        iwShop.setImageResource(R.drawable.delete)
        iwShop.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iwShop.setOnClickListener{ view ->
            val iID = JSONArray.getJSONObject(i).getInt("iID")
            GlobalScope.async(Dispatchers.Main) {
                val call = client.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
                    method = HttpMethod.Post
                    body = MultiPartFormDataContent(
                        formData {
                            append("exec", "delgood")
                            append("id", iID)
                        }
                    )
                }
                val sRequest = call.response.readText()
                if (sRequest.toInt() == 0){
                    val iIndexView = tlInfo.indexOfChild(iwShop.parent as View)
                    tlInfo.removeViewAt(iIndexView)
                    tlInfo.removeViewAt(iIndexView - 1)
                }
            }
        }
        llShop.addView(iwShop)
        (llShop.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,0,20)
        if (bStop){
            bStop = false
            bLoading = false
            return
        }
        tlInfo.addView(llShop)
    }
    // Кнопка Оформления заказа
    val tvOrder = TextView(this)
    tvOrder.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
    tvOrder.setText("Оформить заказ")
    tvOrder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    (tvOrder.layoutParams as LinearLayout.LayoutParams).setMargins(0,60,30,0)
    tvOrder.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.shopping_basket,0)
    tvOrder.setTextColor(Color.parseColor("#696969"))
    tvOrder.gravity = Gravity.RIGHT
    tvOrder.setOnClickListener { view ->
        if (JSONArray.length() > 0){
            val iIDBasket = JSONArray.getJSONObject(0).getInt("iIDBasket")
            GlobalScope.async(Dispatchers.Main) {
                val call = client.call(getString(R.string.server_uri)+getString(R.string.server_order)){
                    method = HttpMethod.Post
                    body = MultiPartFormDataContent(
                        formData {
                            append("exec", "addorder")
                            append("id", iIDBasket)
                        }
                    )
                }
                val sRequest = call.response.readText()
                if (sRequest.toInt() == 0) {
                    Toast.makeText(this@fBasketGetList, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show()
                    fBasketInit()
                    fBasketGetList()
                }
            }
        }
    }
    if (bStop and (sMode != "Basket")){
        bStop = false
        bLoading = false
        return
    }
    tlInfo.addView(tvOrder)
    bLoading = false
}