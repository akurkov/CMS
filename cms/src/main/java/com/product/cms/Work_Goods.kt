// Функции класса work по работе с товарами
package com.product.cms

import android.graphics.Color
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
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
fun work.fGoodsInit() {
    llBC.removeAllViews()
    swBC.visibility = View.VISIBLE
    iIDCategory = 0
    bLoading = false;
    iPage = 0
    bEOD = false
}

// Получение списка товаров и вывод в виде таблицы
suspend fun work.fGoodsGetList(sCategory: String){
    etSearch.clearFocus()
    bLoading = true
    iPage = 0
    bEOD = false
    tlInfo.removeAllViews()

    // Очистка хлебных крошек
    var bDel = false
    val iBCChilds = llBC.childCount
    var iIndex = 0
    for (i in 0..(iBCChilds-1)) {
        if ((llBC.getChildAt(i) as TextView).tag.toString().toInt() == iIDCategory){
            bDel = true
            iIndex = i
        }
    }
    if (bDel){
        for (i in ((iIndex)..(iBCChilds-2))){
            llBC.removeViewAt(iIndex+1)
        }
    }

    // Выводим хлебные крошки
    if (llBC.childCount>0){
        if ((llBC.getChildAt(llBC.childCount-1) as TextView).tag.toString().toInt() == iIDCategory){
            // Получение списка
            fGetGoods()
            bLoading = false
            return
        }
    }
    val lp = ViewGroup.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT)
    if (iIDCategory != 0) {
        val tvSep = TextView(this)
        tvSep.setText(" > ")
        tvSep.setLayoutParams(lp)
        tvSep.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        tvSep.tag = (-1).toString()
        llBC.addView(tvSep)
    }
    val tvBC = TextView(this)
    tvBC.setText(sCategory)
    tvBC.setLayoutParams(lp)
    tvBC.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    tvBC.tag = iIDCategory.toString()
    if (iIDCategory == 0) {
        tvBC.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.home, 0, 0, 0)
    }
    tvBC.setOnClickListener{ view ->
        if (bLoading){
            bStop = true
        }
        iIDCategory = (view as TextView).tag.toString().toInt()
        GlobalScope.async(Dispatchers.Main) {
            fGoodsGetList(sCategory)
        }
    }
    llBC.addView(tvBC)
    // Получение списка
    fGetGoods()
    bLoading = false
}

// Подгрузка товаров
suspend fun work.fAddGoods(){
    if (bLoading or bEOD){
        return
    }
    when (sMode){
        "Goods","Search","Actions" ->{
            bLoading = true
            // Получение списка
            iPage = iPage + 1
            fGetGoods()
            bLoading = false
        }
    }
}

// Добавление товаров к View
suspend fun work.fGetGoods(){
    val client = HttpClient() {
        defaultRequest {
            header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
        }
    }
    var call: HttpClientCall? = null
    when (sMode) {
        "Goods" -> {
            call = client.call(getString(R.string.server_uri)+getString(R.string.server_good)){
                method = HttpMethod.Post
                body = MultiPartFormDataContent(
                    formData {
                        append("exec", "getgoods")
                        append("id", iIDCategory)
                        append("page", iPage)
                    }
                )
            }
        }
        "Search" -> {
            call = client.call(getString(R.string.server_uri)+getString(R.string.server_good)){
                method = HttpMethod.Post
                body = MultiPartFormDataContent(
                    formData {
                        append("exec", "goodsearch")
                        append("query", sSearchString)
                        append("page", iPage)
                    }
                )
            }
        }
        "Actions" -> {
            call = client.call(getString(R.string.server_uri)+getString(R.string.server_good)){
                method = HttpMethod.Post
                body = MultiPartFormDataContent(
                    formData {
                        append("exec", "getgroup")
                        append("query", "Акции")
                        append("page", iPage)
                    }
                )
            }
        }
    }
    val sRequest = call!!.response.readText()
    val JSONArray = JSONArray(sRequest)

    // Выводим список товаров/категорий
    if (JSONArray.length()<30){
        bEOD = true
    }
    for (i in 0..(JSONArray.length()-1)){
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(10,10,10,10)
        val tvNewGood = TextView(this)
        tvNewGood.setText(JSONArray.getJSONObject(i).getString("sName"))
        tvNewGood.setLayoutParams(lp)
        tvNewGood.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
        tvNewGood.tag = JSONArray.getJSONObject(i).getString("iID")
        if (JSONArray.getJSONObject(i).getString("bCategory") == "1"){ // Для категории отображаем значок папки, на клик назначаем получение содержимого категории
            tvNewGood.setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
            tvNewGood.setTextColor(Color.parseColor("#696969"))
            tvNewGood.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.folder,0,0,0)
            tvNewGood.setOnClickListener{ view ->
                if (bLoading){
                    bStop = true
                }
                iIDCategory = (view as TextView).tag.toString().toInt()
                GlobalScope.async(Dispatchers.Main) {
                    fGoodsGetList(JSONArray.getJSONObject(i).getString("sName"))
                }
            }
        }
        else{ // Для товара на клик назначаем отображение карточки
            tvNewGood.setTypeface(null,Typeface.BOLD)
            tvNewGood.setOnClickListener { v ->
                flGoodCard.visibility = View.VISIBLE
                GlobalScope.async(Dispatchers.Main) {
                    fShowGoodCard(JSONArray.getJSONObject(i).getInt("iID"),JSONArray.getJSONObject(i).getString("sName"))
                }
            }
        }
        if (bStop){
            bStop = false
            bLoading = false
            return
        }
        tlInfo.addView(tvNewGood)

        // Добавим описание к товару
        if (JSONArray.getJSONObject(i).getString("bCategory") == "0"){
            val displaymetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displaymetrics)
            val wp = (displaymetrics.widthPixels - llMenu.width) / 3
            val call = client.call(getString(R.string.server_uri)+getString(R.string.server_good)){
                method = HttpMethod.Post
                body = MultiPartFormDataContent(
                    formData {
                        append("exec", "getgoodattr")
                        append("id", JSONArray.getJSONObject(i).getInt("iID"))
                        append("brief", "1")
                    }
                )
            }
            val sRequest = call.response.readText()
            val JSONAttrArray = JSONArray(sRequest)
            val trNewAttrGood = LinearLayout(this)
            trNewAttrGood.orientation = LinearLayout.HORIZONTAL
            trNewAttrGood.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
            // Добавим объект таблицы
            val tlGoodAttrs = LinearLayout(this)
            tlGoodAttrs.orientation = LinearLayout.VERTICAL
            tlGoodAttrs.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
            tlGoodAttrs.setOnClickListener { v ->
                flGoodCard.visibility = View.VISIBLE
                GlobalScope.async(Dispatchers.Main) {
                    fShowGoodCard(JSONArray.getJSONObject(i).getInt("iID"),JSONArray.getJSONObject(i).getString("sName"))
                }
            }
            // Выводим прочие атрибуты
            var iPhoto = -1
            for (j in 0..(JSONAttrArray.length()-1)){
                if (JSONAttrArray.getJSONObject(j).getString("sAttrType") == "Изображение"){
                    iPhoto = j
                }
                else {
                    val tvAttr = TextView(this)
                    var sAttrText = JSONAttrArray.getJSONObject(j).getString("sAttrName")
                    when (JSONAttrArray.getJSONObject(j).getString("sAttrType")) {
                        "Число" -> {
                            sAttrText = sAttrText + ": " + JSONAttrArray.getJSONObject(j)
                                .getString("fValue")
                        }
                        "Дата" -> {
                            sAttrText = sAttrText + ": " + JSONAttrArray.getJSONObject(j)
                                .getString("dValue")
                        }
                        "Время" -> {
                            sAttrText = sAttrText + ": " + JSONAttrArray.getJSONObject(j)
                                .getString("dValue")
                        }
                        else -> {
                            sAttrText = sAttrText + ": " + JSONAttrArray.getJSONObject(j)
                                .getString("sValue")
                        }
                    }
                    tvAttr.setText(sAttrText)
                    tvAttr.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
                    tvAttr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    (tvAttr.layoutParams as LinearLayout.LayoutParams).setMargins(10,0,10,0)
                    tlGoodAttrs.addView(tvAttr)
                }
            }
            // Добавим объект фото
            if (iPhoto<0) {
                val iwGoodPhoto = ImageView(this)
                iwGoodPhoto.setLayoutParams(LinearLayout.LayoutParams(wp,LinearLayout.LayoutParams.MATCH_PARENT))
                iwGoodPhoto.setImageResource(R.drawable.nofoto)
                iwGoodPhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
                trNewAttrGood.addView(iwGoodPhoto)
            }
            else{
                val wvPhoto = WebView(this)
                wvPhoto.setLayoutParams(LinearLayout.LayoutParams(wp,LinearLayout.LayoutParams.MATCH_PARENT))
                wvPhoto.loadUrl(JSONAttrArray.getJSONObject(iPhoto).getString("sValue"))
                wvPhoto.settings.useWideViewPort = true
                wvPhoto.settings.loadWithOverviewMode = true
                wvPhoto.webViewClient = WebViewClient()
                // При клике на фото увеличение фото
                wvPhoto.setOnTouchListener { v, event ->
                    wvFullPhoto.loadUrl(JSONAttrArray.getJSONObject(iPhoto).getString("sValue"))
                    wvFullPhoto.settings.useWideViewPort = true
                    wvFullPhoto.settings.loadWithOverviewMode = true
                    flPhoto.visibility = View.VISIBLE
                    true
                }
                trNewAttrGood.addView(wvPhoto)
            }
            trNewAttrGood.addView(tlGoodAttrs)
            if (bStop){
                bStop = false
                bLoading = false
                return
            }
            tlInfo.addView(trNewAttrGood)
            // Добавим кнопки покупки
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
                    (((iwMinus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).setText(sCountText)
                }
            }
            llShop.addView(iwMinus)
            val flCount = FrameLayout(this)
            val tvCount = TextView(this)
            tvCount.setText("0")
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
                var sCountText = (((iwPlus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString()
                sCountText = (sCountText.toInt() + 1).toString()
                (((iwPlus.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).setText(sCountText)
            }
            llShop.addView(iwPlus)
            val iwShop = ImageView(this)
            iwShop.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,2f))
            iwShop.setImageResource(R.drawable.add_shopping_cart)
            iwShop.scaleType = ImageView.ScaleType.CENTER_INSIDE
            iwShop.setOnClickListener{ view ->
                val iCount = (((iwShop.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString().toInt()
                val iIDGood = JSONArray.getJSONObject(i).getInt("iID")
                if (iCount > 0){
                    GlobalScope.async(Dispatchers.Main) {
                        val call = client.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
                            method = HttpMethod.Post
                            body = MultiPartFormDataContent(
                                formData {
                                    append("exec", "addgood")
                                    append("login", sLogin)
                                    append("count", iCount)
                                    append("id", iIDGood)
                                }
                            )
                        }
                        val sRequest = call.response.readText()
                        if (sRequest.toInt() == 0){
                            Toast.makeText(this@fGetGoods,"Товар добавлен в корзину",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            llShop.addView(iwShop)
            if (bStop){
                bStop = false
                bLoading = false
                return
            }
            tlInfo.addView(llShop)
        }
    }
}

// Поиск товаров и вывод результата
suspend fun work.fGoodsSearch(){
    llBC.removeAllViews()
    swBC.visibility = View.GONE
    bLoading = true;
    iPage = 0
    bEOD = false
    tlInfo.removeAllViews()
    fGetGoods()
    bLoading = false;
}

// Отображение акционных товаров
suspend fun work.fGoodActions(){
    llBC.removeAllViews()
    swBC.visibility = View.GONE
    bLoading = true;
    iPage = 0
    bEOD = false
    tlInfo.removeAllViews()
    fGetGoods()
    bLoading = false;
}

// Отображение карточки товара
suspend fun work.fShowGoodCard(iIDGood: Int, sName: String){
    // Заголовок карточки - наименование товара
    llCard.removeAllViews()
    tvCardCount.tag = iIDGood.toString()
    val lpm = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
    val tvName = TextView(this)
    tvName.setLayoutParams(lpm)
    (tvName.layoutParams as LinearLayout.LayoutParams).setMargins(0,20,0,0)
    tvName.setTypeface(null,Typeface.BOLD)
    tvName.gravity = Gravity.CENTER
    tvName.setText(sName)
    tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
    llCard.addView(tvName)
    // Получение всех атрибутов
    val client = HttpClient() {
        defaultRequest {
            header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
        }
    }
    val call = client.call(getString(R.string.server_uri)+getString(R.string.server_good)){
        method = HttpMethod.Post
        body = MultiPartFormDataContent(
            formData {
                append("exec", "getgoodattr")
                append("id", iIDGood)
                append("brief", "0")
            }
        )
    }
    val sRequest = call.response.readText()
    val JSONAttrArray = JSONArray(sRequest)
    val displaymetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displaymetrics)
    val hp = ((displaymetrics.heightPixels - tvName.height-200) / 2).toInt()
    val wp = ((displaymetrics.widthPixels - 30) / 2.8f).toInt()
    var iPhotoIndex = -1
    // Найти изображение
    for (i in (0..(JSONAttrArray.length()-1))){
        if (JSONAttrArray.getJSONObject(i).getString("sAttrType") == "Изображение"){
            if (JSONAttrArray.getJSONObject(i).getString("sValue") != ""){
                iPhotoIndex = i
            }
        }
    }
    // Вывести изображение
    if (iPhotoIndex < 0){
        val iwGoodPhoto = ImageView(this)
        iwGoodPhoto.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, hp))
        iwGoodPhoto.setImageResource(R.drawable.nofoto)
        iwGoodPhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
        llCard.addView(iwGoodPhoto)
    }
    else{
        val wvPhoto = WebView(this)
        wvPhoto.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, hp))
        wvPhoto.loadUrl(JSONAttrArray.getJSONObject(iPhotoIndex).getString("sValue"))
        wvPhoto.settings.useWideViewPort = true
        wvPhoto.settings.loadWithOverviewMode = true
        wvPhoto.webViewClient = WebViewClient()
        llCard.addView(wvPhoto)
    }
    // По всем атрибутам
    for (i in (0..(JSONAttrArray.length()-1))){
        if (JSONAttrArray.getJSONObject(i).getString("sAttrType") != "Изображение"){
            val llAttr = LinearLayout(this)
            llAttr.setLayoutParams(lpm)
            llAttr.orientation = LinearLayout.HORIZONTAL
            // Наименование атрибута
            val tvAttr = TextView(this)
            tvAttr.setLayoutParams(LinearLayout.LayoutParams(wp,LinearLayout.LayoutParams.WRAP_CONTENT))
            (tvAttr.layoutParams as LinearLayout.LayoutParams).setMargins(10,0,0,0)
            tvAttr.setTypeface(null,Typeface.BOLD)
            tvAttr.setText(JSONAttrArray.getJSONObject(i).getString("sAttrName"))
            tvAttr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            llAttr.addView(tvAttr)
            var sAttrText = ""
            // Получим тип атрибута
            when (JSONAttrArray.getJSONObject(i).getString("sAttrType")) {
                "Число" -> {
                    sAttrText = JSONAttrArray.getJSONObject(i).getString("fValue")
                }
                "Дата" -> {
                    sAttrText = JSONAttrArray.getJSONObject(i).getString("dValue")
                }
                "Время" -> {
                    sAttrText = JSONAttrArray.getJSONObject(i).getString("dValue")
                }
                else -> {
                    sAttrText = JSONAttrArray.getJSONObject(i).getString("sValue")
                }
            }
            val tvValue = TextView(this)
            tvValue.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT))
            (tvValue.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,10,0)
            tvValue.setText(sAttrText)
            tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            llAttr.addView(tvValue)
            llCard.addView(llAttr)
        }
    }
    // Добавим пустое пространство снизу
    val spSpace = Space(this)
    spSpace.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,120))
    llCard.addView(spSpace)
}