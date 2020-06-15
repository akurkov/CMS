// Настройка товаров

package com.product.cms

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import io.ktor.client.call.call
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import kotlinx.android.synthetic.main.activity_prefs.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray

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
    }
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
            llGood.setOnClickListener(onLLClickListener())
            llGood.setOnLongClickListener(onLLLongClickListener())
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
fun Prefs.onLLClickListener(): View.OnClickListener {
    return View.OnClickListener { v->
        fCollapseCategory()
        svPerMenu.visibility = View.GONE
        llEdit.visibility = View.GONE
        val svGoodCart = ScrollView(this)
        svGoodCart.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        clPrefs.addView(svGoodCart)
        val llGoodCart = LinearLayout(this)
        llGoodCart.orientation = LinearLayout.VERTICAL
        llGoodCart.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        llGoodCart.setBackgroundColor(Color.parseColor("#FFFFFF"))
        svGoodCart.addView(llGoodCart)
        GlobalScope.async(Dispatchers.Main) {
            val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
                method = HttpMethod.Post
                body = MultiPartFormDataContent(formData {
                    append("exec", "getgoodattrs")
                    append("id", v.tag.toString())
                })
            }
            val sRequest = call.response.readText()
            val jaGoodAttrs = JSONArray(sRequest)
            for (i in 0 until jaGoodAttrs.length()){
                val llGood = LinearLayout(this@onLLClickListener)
                llGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                (llGood.layoutParams as LinearLayout.LayoutParams).setMargins(10,0,10,0)
                llGood.orientation = LinearLayout.HORIZONTAL
                llGood.tag = mapOf(
                    "iIDAttr" to jaGoodAttrs.getJSONObject(i).getString("iIDAttr"),
                    "iIDType" to jaGoodAttrs.getJSONObject(i).getString("iIDType"),
                    "bRequire" to jaGoodAttrs.getJSONObject(i).getString("bRequire"),
                    "sType" to jaGoodAttrs.getJSONObject(i).getString("sType")
                )
                llGoodCart.addView(llGood)
                val flAttrName = FrameLayout(this@onLLClickListener)
                flAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.35f)
                (flAttrName.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,5,0)
                llGood.addView(flAttrName)
                val tvAttrName = TextView(this@onLLClickListener)
                tvAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                tvAttrName.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                tvAttrName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                tvAttrName.setTextColor(Color.parseColor("#000000"))
                tvAttrName.text = jaGoodAttrs.getJSONObject(i).getString("sName")
                flAttrName.addView(tvAttrName)
            }
        }
    }
}

// Долгий клик на строчку с товаром - покажем контекстное меню
fun Prefs.onLLLongClickListener(): View.OnLongClickListener {
    return View.OnLongClickListener { v->
        fCollapseCategory()
        svPerMenu.visibility = View.GONE
        llEdit.visibility = View.VISIBLE
        llEdit.y = v.y + llEdit.height + v.height / 2

        // Редактирование товара
        ivEdit.setOnClickListener {
            llEdit.visibility = View.GONE
            Toast.makeText(this, "Редактирование товара", Toast.LENGTH_SHORT).show()
        }

        // Применяемость к категории
        ivCategory.setOnClickListener {
            llEdit.visibility = View.GONE
            Toast.makeText(this, "Применяемость к категории", Toast.LENGTH_SHORT).show()
        }

        // Акционные товары
        ivActions.setOnClickListener {
            llEdit.visibility = View.GONE
            Toast.makeText(this, "Акционные товары", Toast.LENGTH_SHORT).show()
        }
        true
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
                val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
                    method = HttpMethod.Post
                    body = MultiPartFormDataContent(formData {
                        append("exec", "savegood")
                        append("name", sGoodName)
                    })
                }
                val iRequest = call.response.readText().toInt()
                if (iRequest > 0) {
                    val tvGood = TextView(this@onOkClickListener)
                    view.tag = iRequest
                    tvGood.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    tvGood.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                    tvGood.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    tvGood.text = sGoodName
                    tvGood.setTextColor(Color.parseColor("#000000"))
                    aViews[0] = tvGood
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