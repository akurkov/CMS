// Функции класса work по работе с корзиной
package com.product.cms

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
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
fun Work.fBasketInit() {
    if (bLoading){
        bStop = true
    }
    swBC.visibility = View.GONE
}

// Получение состава корзины
suspend fun Work.fBasketGetList(){
    bLoading = true
    tlInfo.removeAllViews()
    etSearch.clearFocus()
    // Получаем состав корзины
    var call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
        method = HttpMethod.Post
        body = MultiPartFormDataContent(
            formData {
                append("exec", "getbasket")
                append("login", sLogin)
            }
        )
    }
    var sRequest = call.response.readText()
    val aJSONArray = JSONArray(sRequest)
    // Заголовок
    val tvTitle = TextView(this)
    tvTitle.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
    tvTitle.text = "Корзина"
    tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    (tvTitle.layoutParams as LinearLayout.LayoutParams).setMargins(0,20,0,20)
    tvTitle.gravity = Gravity.CENTER
    tvTitle.setTextColor(Color.parseColor("#000000"))
    if (bStop){
        bStop = false
        return
    }
    tlInfo.addView(tvTitle)
    // Кнопка очистки корзины
    val tvClear = TextView(this)
    tvClear.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
    tvClear.text = "Очистить"
    tvClear.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    (tvClear.layoutParams as LinearLayout.LayoutParams).setMargins(0,20,30,20)
    tvClear.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.cancel,0)
    tvClear.setTextColor(Color.parseColor("#696969"))
    tvClear.gravity = Gravity.END
    tvClear.setOnClickListener {
        GlobalScope.async(Dispatchers.Main){
            if (bLoading){
                return@async
            }
            bLoading = true
            call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
                method = HttpMethod.Post
                body = MultiPartFormDataContent(
                    formData {
                        append("exec", "delgood")
                        append("login", sLogin)
                    }
                )
            }
            sRequest = call.response.readText()
            if (sRequest.toInt() == 0){
                fBasketGetList()
            }
            bLoading = false
        }
    }
    if (bStop){
        bStop = false
        return
    }
    tlInfo.addView(tvClear)
    // Товары
    for (i in 0 until aJSONArray.length()){
        // Атрибуты товаров
        call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_good)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(
                formData {
                    append("exec", "getgoodattr")
                    append("id", aJSONArray.getJSONObject(i).getInt("iIDGood"))
                    append("brief", "0")
                }
            )
        }
        sRequest = call.response.readText()
        val aJSONAttrArray = JSONArray(sRequest)
        var iPriceIndex = -1
        for (j in 0 until aJSONAttrArray.length()){
            if (aJSONAttrArray.getJSONObject(j).getString("sAttrName") == "Цена"){
                iPriceIndex = j
            }
        }
        val llGood = LinearLayout(this)
        llGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        llGood.orientation = LinearLayout.HORIZONTAL
        val flGood = FrameLayout(this)
        flGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,0.2f)
        val tvGood = TextView(this)
        tvGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        tvGood.text = aJSONArray.getJSONObject(i).getString("sName")
        tvGood.gravity = Gravity.CENTER
        val flPrice = FrameLayout(this)
        flPrice.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,0.8f)
        val tvPrice = TextView(this)
        tvPrice.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
        if (iPriceIndex >= 0) {
            tvPrice.text = String.format("%.2f",aJSONAttrArray.getJSONObject(iPriceIndex).getDouble("fValue"))
        }
        tvPrice.gravity = Gravity.CENTER
        if (bStop){
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
        llShop.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        val iwMinus = ImageView(this)
        iwMinus.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f)
        iwMinus.setImageResource(R.drawable.minus)
        iwMinus.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iwMinus.setOnClickListener{
            var sCountText = (((iwMinus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString()
            if (sCountText != "0"){
                sCountText = (sCountText.toInt() - 1).toString()
                GlobalScope.async(Dispatchers.Main) {
                    call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
                        method = HttpMethod.Post
                        body = MultiPartFormDataContent(
                            formData {
                                append("exec", "recount")
                                append("id", aJSONArray.getJSONObject(i).getInt("iID"))
                                append("count", sCountText)
                            }
                        )
                    }
                    sRequest = call.response.readText()
                    if (sRequest.toInt() == 0) {
                        (((iwMinus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text = sCountText
                    }
                }
            }
        }
        llShop.addView(iwMinus)
        val flCount = FrameLayout(this)
        val tvCount = TextView(this)
        tvCount.text = aJSONArray.getJSONObject(i).getString("iCount")
        tvCount.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT)
        tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f)
        (tvCount.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.CENTER
        flCount.addView(tvCount)
        llShop.addView(flCount)
        val iwPlus = ImageView(this)
        iwPlus.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f)
        iwPlus.setImageResource(R.drawable.plus)
        iwPlus.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iwPlus.setOnClickListener{
            var sCountText = (((iwMinus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString()
            sCountText = (sCountText.toInt() + 1).toString()
            GlobalScope.async(Dispatchers.Main) {
                call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
                    method = HttpMethod.Post
                    body = MultiPartFormDataContent(
                        formData {
                            append("exec", "recount")
                            append("id", aJSONArray.getJSONObject(i).getInt("iID"))
                            append("count", sCountText)
                        }
                    )
                }
                sRequest = call.response.readText()
                if (sRequest.toInt() == 0) {
                    (((iwMinus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text = sCountText
                }
            }
        }
        llShop.addView(iwPlus)
        val iwShop = ImageView(this)
        iwShop.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f)
        iwShop.setImageResource(R.drawable.delete)
        iwShop.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iwShop.setOnClickListener{
            val iID = aJSONArray.getJSONObject(i).getInt("iID")
            GlobalScope.async(Dispatchers.Main) {
                if (bLoading){
                    return@async
                }
                bLoading = true
                call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
                    method = HttpMethod.Post
                    body = MultiPartFormDataContent(
                        formData {
                            append("exec", "delgood")
                            append("id", iID)
                        }
                    )
                }
                sRequest = call.response.readText()
                if (sRequest.toInt() == 0){
                    if (bStop){
                        bStop = false
                        return@async
                    }
                    val iIndexView = tlInfo.indexOfChild(iwShop.parent as View)
                    tlInfo.removeViewAt(iIndexView)
                    tlInfo.removeViewAt(iIndexView - 1)
                }
                bLoading = false
            }
        }
        llShop.addView(iwShop)
        (llShop.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,0,20)
        if (bStop){
            bStop = false
            return
        }
        tlInfo.addView(llShop)
    }
    // Кнопка Оформления заказа
    val tvOrder = TextView(this)
    tvOrder.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
    tvOrder.text = "Оформить заказ"
    tvOrder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    (tvOrder.layoutParams as LinearLayout.LayoutParams).setMargins(0,60,30,0)
    tvOrder.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.shopping_basket,0)
    tvOrder.setTextColor(Color.parseColor("#696969"))
    tvOrder.gravity = Gravity.END
    tvOrder.setOnClickListener {
        if (aJSONArray.length() > 0){
            val iIDBasket = aJSONArray.getJSONObject(0).getInt("iIDBasket")
            GlobalScope.async(Dispatchers.Main) {
                if (bLoading){
                    return@async
                }
                bLoading = true
                call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_order)){
                    method = HttpMethod.Post
                    body = MultiPartFormDataContent(
                        formData {
                            append("exec", "addorder")
                            append("id", iIDBasket)
                        }
                    )
                }
                sRequest = call.response.readText()
                if (sRequest.toInt() == 0) {
                    Toast.makeText(this@fBasketGetList, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show()
                    fBasketInit()
                    fBasketGetList()
                }
                bLoading = false
            }
        }
    }
    if (bStop){
        bStop = false
        return
    }
    tlInfo.addView(tvOrder)
    bLoading = false
}