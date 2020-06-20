// Настройка товаров

package com.product.cms

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import io.ktor.client.call.call
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import kotlinx.android.synthetic.main.activity_prefs.*
import kotlinx.android.synthetic.main.dialog_attrvalues.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray
import org.json.JSONObject
import java.util.logging.Logger

// Инициализация окна настроек товаров
fun Prefs.fPrefGood(sRights: String) {
    if (tAsync.isActive) {
        bStop = true
    }
    GlobalScope.async(Dispatchers.Main) {
        // Получим перечень категорий
        val call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_perf)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData {
                append("exec", "getcategory")
            })
        }
        val sRequest = call.response.readText()
        jaSaveData = JSONArray(sRequest)
        llCategory.removeAllViews()
        val llBut = LinearLayout(this@fPrefGood)
        llBut.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        llBut.orientation = LinearLayout.HORIZONTAL
        (llBut.layoutParams as LinearLayout.LayoutParams).setMargins(0,5,0,5)
        val iwCancel = ImageView(this@fPrefGood)
        iwCancel.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        iwCancel.setImageResource(R.drawable.cancel)
        iwCancel.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iwCancel.setOnClickListener {
            svCategory.visibility = View.GONE
        }
        val iwOK = ImageView(this@fPrefGood)
        iwOK.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        iwOK.setImageResource(R.drawable.done)
        iwOK.scaleType = ImageView.ScaleType.CENTER_INSIDE
        llBut.addView(iwCancel)
        llBut.addView(iwOK)
        llCategory.addView(llBut)
        faddCategory(0,0)
    }
    val iVMargin = (Resources.getSystem().displayMetrics.heightPixels * 0.01f).toInt()
    val iLMargin = (Resources.getSystem().displayMetrics.widthPixels * 0.3f).toInt()
    val iRMargin = (Resources.getSystem().displayMetrics.widthPixels * 0.01f).toInt()
    val set = ConstraintSet()
    set.clone(clPrefs)
    set.connect(R.id.svCategory, ConstraintSet.TOP, R.id.clPrefs, ConstraintSet.TOP, iVMargin)
    set.connect(R.id.svCategory, ConstraintSet.LEFT, R.id.clPrefs, ConstraintSet.LEFT, iLMargin)
    set.connect(R.id.svCategory, ConstraintSet.BOTTOM, R.id.clPrefs, ConstraintSet.BOTTOM, iVMargin)
    set.connect(R.id.svCategory, ConstraintSet.RIGHT, R.id.clPrefs, ConstraintSet.RIGHT, iRMargin)
    set.applyTo(clPrefs)
    llInfo.removeAllViews()

    // Заголовок
    val tvTitle = TextView(this)
    tvTitle.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    tvTitle.text = "Товары"
    tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    (tvTitle.layoutParams as LinearLayout.LayoutParams).setMargins(0,10,0,20)
    tvTitle.gravity = Gravity.CENTER
    tvTitle.setTextColor(Color.parseColor("#000000"))
    llInfo.addView(tvTitle)

    // Кнопка с вставкой нового элемента
    if (sRights == "W") {
        val tvNewGood = TextView(this)
        tvNewGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tvNewGood.text = "+"
        tvNewGood.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        tvNewGood.gravity = Gravity.CENTER
        tvNewGood.setTextColor(Color.parseColor("#6200ee"))
        tvNewGood.background = getDrawable(android.R.drawable.editbox_background)
        tvNewGood.setOnClickListener {
            fCollapseCategory()
            svPerMenu.visibility = View.GONE
            llEdit.visibility = View.GONE
            aViews[0] = null
            val llGood = LinearLayout(this)
            llGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            llGood.orientation = LinearLayout.HORIZONTAL
            llGood.setOnClickListener(onLLClickListener(sRights))
            llGood.setOnLongClickListener(onLLLongClickListener(sRights))
            val flGood = FrameLayout(this)
            flGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            (flGood.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,0,0)
            // Вставка нового элемента
            bExpanded = true
            val etGood = EditText(this)
            etGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            etGood.requestFocus()
            flGood.addView(etGood)
            llGood.addView(flGood)
            llInfo.addView(llGood, llInfo.indexOfChild(it) + 1)

            // Кнопки
            val llBut = LinearLayout(this)
            llBut.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            llBut.orientation = LinearLayout.HORIZONTAL
            val iwCancel = ImageView(this)
            iwCancel.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            iwCancel.setImageResource(R.drawable.cancel)
            iwCancel.scaleType = ImageView.ScaleType.CENTER_INSIDE
            iwCancel.setOnClickListener {
                svPerMenu.visibility = View.GONE
                llEdit.visibility = View.GONE
                svCategory.visibility = View.GONE
                fCollapseCategory()
            }
            val iwOK = ImageView(this)
            iwOK.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            iwOK.setImageResource(R.drawable.done)
            iwOK.scaleType = ImageView.ScaleType.CENTER_INSIDE
            iwOK.setOnClickListener(onOkClickListener(llGood))
            llBut.addView(iwCancel)
            llBut.addView(iwOK)
            llInfo.addView(llBut, llInfo.indexOfChild(it) + 2)
        }
        llInfo.addView(tvNewGood)
    }
    val spSpace = Space(this)
    spSpace.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200)
    llInfo.addView(spSpace)
}

// Клик на строчку с товаром - покажем карточку
fun Prefs.onLLClickListener(sRights: String): View.OnClickListener {
    return View.OnClickListener { v->
        fCollapseCategory()
        svPerMenu.visibility = View.GONE
        llEdit.visibility = View.GONE
        llMenu.visibility = View.GONE
        svCategory.visibility = View.GONE
        val svGoodCart = ScrollView(this)
        svGoodCart.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        svGoodCart.tag = "GoodCard"
        clPrefs.addView(svGoodCart)
        val llGoodCart = LinearLayout(this)
        llGoodCart.orientation = LinearLayout.VERTICAL
        llGoodCart.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        llGoodCart.setBackgroundColor(Color.parseColor("#FFFFFF"))
        svGoodCart.addView(llGoodCart)
        GlobalScope.async(Dispatchers.Main) {
            // Получим перечень атрибутов
            val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
                method = HttpMethod.Post
                body = MultiPartFormDataContent(formData {
                    append("exec", "getgoodattrs")
                    append("id", v.tag.toString())
                })
            }
            val sRequest = call.response.readText()
            val jaGoodAttrs = JSONArray(sRequest)

            // Выведем атрибуты
            for (i in 0 until jaGoodAttrs.length()){
                val llGood = LinearLayout(this@onLLClickListener)
                llGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                (llGood.layoutParams as LinearLayout.LayoutParams).setMargins(10,0,10,0)
                llGood.orientation = LinearLayout.HORIZONTAL
                llGoodCart.addView(llGood)
                val flAttrName = FrameLayout(this@onLLClickListener)
                flAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.65f)
                (flAttrName.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,5,0)
                llGood.addView(flAttrName)
                val tvAttrName = TextView(this@onLLClickListener)
                tvAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                tvAttrName.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                tvAttrName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                tvAttrName.setTextColor(Color.parseColor("#000000"))
                tvAttrName.text = jaGoodAttrs.getJSONObject(i).getString("sName")
                flAttrName.addView(tvAttrName)
                val flAttrValue = FrameLayout(this@onLLClickListener)
                flAttrValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.35f)
                (flAttrValue.layoutParams as LinearLayout.LayoutParams).setMargins(5,0,0,0)
                llGood.addView(flAttrValue)
                var sValue = when (jaGoodAttrs.getJSONObject(i).getString("sType")) {
                    "Число" -> jaGoodAttrs.getJSONObject(i).getString("fValue")
                    "Дата" -> jaGoodAttrs.getJSONObject(i).getString("dValue").convertDate()
                    "Время" -> jaGoodAttrs.getJSONObject(i).getString("dValue")
                    else -> jaGoodAttrs.getJSONObject(i).getString("sValue")
                }
                sValue = if (sValue == "null") "" else sValue
                var etAttrValue: View?
                if (jaGoodAttrs.getJSONObject(i).getString("sType") == "Перечисление"){
                    val aAttrValues = mutableListOf<String>()
                    var sList = jaGoodAttrs.getJSONObject(i).getString("sValues")
                    while (sList.isNotEmpty()){
                        val sNext = sList.substringBefore('#',sList)
                        sList = sList.substringAfter('#',"")
                        aAttrValues.add(sNext)
                    }
                    etAttrValue = Spinner(this@onLLClickListener)
                    val adAttrValue = ArrayAdapter(this@onLLClickListener, android.R.layout.simple_spinner_item, aAttrValues)
                    adAttrValue.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    etAttrValue.adapter = adAttrValue
                    etAttrValue.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                    etAttrValue.setSelection(aAttrValues.indexOf(sValue))
                }
                else{
                    etAttrValue = EditText(this@onLLClickListener)
                    etAttrValue.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                    etAttrValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    etAttrValue.setTextColor(Color.parseColor("#000000"))
                    etAttrValue.setText(sValue)
                    when (jaGoodAttrs.getJSONObject(i).getString("sType")){ // Время пока не трогаю, нет атрибутов. При наличии сделат подобно дате
                        "Дата" -> {
                            etAttrValue.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL + InputType.TYPE_CLASS_NUMBER
                            DateInputMask(etAttrValue).listen()
                        }
                        "Число" -> {
                            etAttrValue.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL + InputType.TYPE_CLASS_NUMBER
                        }
                    }
                }
                etAttrValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                flAttrValue.addView(etAttrValue)
            }

            // Выведем кнопки
            val llBut = LinearLayout(this@onLLClickListener)
            llBut.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            (llBut.layoutParams as LinearLayout.LayoutParams).setMargins(0,20,0,0)
            llBut.orientation = LinearLayout.HORIZONTAL
            val iwCancel = ImageView(this@onLLClickListener)
            iwCancel.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            iwCancel.setImageResource(R.drawable.cancel)
            iwCancel.scaleType = ImageView.ScaleType.CENTER_INSIDE
            iwCancel.setOnClickListener {
                llMenu.visibility = View.VISIBLE
                clPrefs.removeView(svGoodCart)
            }
            llBut.addView(iwCancel)
            if (sRights == "W") {
                val iwOK = ImageView(this@onLLClickListener)
                iwOK.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                iwOK.setImageResource(R.drawable.done)
                iwOK.scaleType = ImageView.ScaleType.CENTER_INSIDE
                iwOK.setOnClickListener {
                    var bSaving = true
                    for (i in 0 until jaGoodAttrs.length()) {
                        val view = ((llGoodCart.getChildAt(i) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0)
                        if ((view is EditText) && (jaGoodAttrs.getJSONObject(i).getInt("bRequire") == 1)) {
                            if (view.text.toString().isEmpty()) {
                                bSaving = false
                                Toast.makeText(this@onLLClickListener, "Атрибут " + jaGoodAttrs.getJSONObject(i).getString("sName") + " не может быть пустым", Toast.LENGTH_SHORT).show()
                            }
                        }
                        when (jaGoodAttrs.getJSONObject(i).getString("sType")) {
                            "Число" -> jaGoodAttrs.getJSONObject(i).put("fValue", (view as EditText).text.toString())
                            "Дата", "Время" -> jaGoodAttrs.getJSONObject(i).put("dValue", (view as EditText).text.toString())
                            "Перечисление" -> jaGoodAttrs.getJSONObject(i).put("sValue", (view as Spinner).selectedItem.toString())
                            else -> jaGoodAttrs.getJSONObject(i).put("sValue", (view as EditText).text.toString())
                        }
                        jaGoodAttrs.getJSONObject(i).put("iIDObject", v.tag.toString())
                    }
                    if (bSaving) {
                        GlobalScope.async(Dispatchers.Main) {
                            val log = Logger.getAnonymousLogger()
                            log.warning(jaGoodAttrs.toString())
                            val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
                                method = HttpMethod.Post
                                body = MultiPartFormDataContent(formData {
                                    append("exec", "savegoodattrs")
                                    append("jsonattrs",jaGoodAttrs.toString())
                                })
                            }
                            val iRes = call.response.readText().toInt()
                            if (iRes == 0){
                                Toast.makeText(this@onLLClickListener, "Атрибуты успешно сохранены", Toast.LENGTH_SHORT).show()
                            }
                        }
                        llMenu.visibility = View.VISIBLE
                        clPrefs.removeView(svGoodCart)
                    }
                }
                llBut.addView(iwOK)
            }
            llGoodCart.addView(llBut)
            val spSpace = Space(this@onLLClickListener)
            spSpace.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200)
            llGoodCart.addView(spSpace)
        }
    }
}

// Долгий клик на строчку с товаром - покажем контекстное меню
fun Prefs.onLLLongClickListener(sRights: String): View.OnLongClickListener {
    return View.OnLongClickListener { v->
        fCollapseCategory()
        svPerMenu.visibility = View.GONE
        llEdit.visibility = View.VISIBLE
        svCategory.visibility = View.GONE
        llEdit.y = v.y + llEdit.height + v.height / 2

        // Редактирование товара
        ivEdit.setOnClickListener {
            llEdit.visibility = View.GONE
            if (sRights == "R") {
                Toast.makeText(this, "Нет прав для редактирования товара", Toast.LENGTH_SHORT).show()
            }
            else{
                if (((v as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) is EditText){
                    return@setOnClickListener
                }
                bExpanded = true
                aViews[0] = (v.getChildAt(0) as FrameLayout).getChildAt(0)
                (v.getChildAt(0) as FrameLayout).removeAllViews()
                val etGood = EditText(this)
                etGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                etGood.setText((aViews[0] as TextView).text)
                (v.getChildAt(0) as FrameLayout).addView(etGood)
                etGood.requestFocus()
                // Добавим кнопки
                val llBut = LinearLayout(this)
                llBut.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                llBut.orientation = LinearLayout.HORIZONTAL
                val iwCancel = ImageView(this)
                iwCancel.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                iwCancel.setImageResource(R.drawable.cancel)
                iwCancel.scaleType = ImageView.ScaleType.CENTER_INSIDE
                iwCancel.setOnClickListener {
                    svPerMenu.visibility = View.GONE
                    llEdit.visibility = View.GONE
                    fCollapseCategory()
                }
                val iwOK = ImageView(this)
                iwOK.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                iwOK.setImageResource(R.drawable.done)
                iwOK.scaleType = ImageView.ScaleType.CENTER_INSIDE
                iwOK.setOnClickListener(onOkClickListener(v))
                llBut.addView(iwCancel)
                llBut.addView(iwOK)
                val iwDel = ImageView(this)
                iwDel.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                iwDel.setImageResource(R.drawable.delete)
                iwDel.scaleType = ImageView.ScaleType.CENTER_INSIDE
                iwDel.setOnClickListener {
                    svPerMenu.visibility = View.GONE
                    val dlAttrDel = AlertDialog.Builder(this)
                    dlAttrDel.setTitle("Предупреждение")
                    dlAttrDel.setMessage("Удалить товар?")
                    dlAttrDel.setPositiveButton("Да") { _, _ ->
                        GlobalScope.async(Dispatchers.Main) {
                            val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
                                method = HttpMethod.Post
                                body = MultiPartFormDataContent(formData {
                                    append("exec", "savegood")
                                    append("name", "")
                                    append("id", v.tag.toString())
                                    append("del", 1)
                                })
                            }
                            val iRequest = call.response.readText().toInt()
                        }
                        aViews[0] = null
                        fCollapseCategory()
                    }
                    dlAttrDel.setNegativeButton("Нет") { _, _ ->
                    }
                    dlAttrDel.show()
                }
                llBut.addView(iwDel)
            llInfo.addView(llBut,(v.parent as LinearLayout).indexOfChild(v) + 1)
            }
        }

        // Применяемость к категории
        ivCategory.setOnClickListener {
            llEdit.visibility = View.GONE
            svCategory.visibility = View.VISIBLE
            (llCategory.getChildAt(0) as LinearLayout).getChildAt(1).setOnClickListener {
                Toast.makeText(this, "Сохраняю привязку товаров", Toast.LENGTH_SHORT).show()
                svCategory.visibility = View.GONE
            }
            // Проставлю привязку к категориям
            GlobalScope.async(Dispatchers.Main) {
                
            }
        }

        // Акционные товары
        ivActions.setOnClickListener {
            llEdit.visibility = View.GONE
            Toast.makeText(this, "Акционные товары", Toast.LENGTH_SHORT).show()
        }
        true
    }
}

// Вывод категорий
fun Prefs.faddCategory(iIDParent: Int, iLevel: Int){
    for (i in 0 until jaSaveData.length()){
        if (iIDParent == jaSaveData.getJSONObject(i).getInt("iIDParent")) {
            val cbCategory = CheckBox(this)
            cbCategory.setTextColor(Color.parseColor("#000000"))
            cbCategory.text = "  ".repeat(iLevel) + jaSaveData.getJSONObject(i).getString("sChild")
            cbCategory.tag = jaSaveData.getJSONObject(i).getInt("iIDChild")
            llCategory.addView(cbCategory)
            faddCategory(jaSaveData.getJSONObject(i).getInt("iIDChild"), iLevel + 1)
        }
    }
}

// Сохранение товара
fun Prefs.onOkClickListener(view: View): View.OnClickListener {
    return View.OnClickListener {
        val sGoodName = (((view as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as EditText).text.toString()
        if (sGoodName.isEmpty()){
            Toast.makeText(this@onOkClickListener, "Пустое наименование товара не допускается", Toast.LENGTH_SHORT).show()
        }
        else {
            GlobalScope.async(Dispatchers.Main) {
                // Сохраним товар
                var iID = 0
                if (view.tag != null){
                    iID = view.tag.toString().toInt()
                }
                val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
                    method = HttpMethod.Post
                    body = MultiPartFormDataContent(formData {
                        append("exec", "savegood")
                        append("name", sGoodName)
                        append("id", iID)
                        append("del", 0)
                    })
                }
                val iRequest = call.response.readText().toInt()
                if (iRequest > 0) {
                    if (iID == 0) {
                        val tvGood = TextView(this@onOkClickListener)
                        view.tag = iRequest
                        tvGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                        tvGood.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                        tvGood.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        tvGood.text = sGoodName
                        tvGood.setTextColor(Color.parseColor("#000000"))
                        aViews[0] = tvGood
                    }
                    else{
                        (aViews[0] as TextView).text = sGoodName
                    }
                    fCollapseCategory()
                }
                if (iRequest < 0) {
                    Toast.makeText(this@onOkClickListener, "Такой товар уже существует", Toast.LENGTH_SHORT).show()
                }
                if (iRequest == 0) {
                    Toast.makeText(this@onOkClickListener, "Не удалось сохранить товар", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}