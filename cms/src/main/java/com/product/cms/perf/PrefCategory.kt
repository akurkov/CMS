// Настройка категорий товаров

package com.product.cms

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
import org.json.JSONObject
import java.util.logging.Logger
import kotlin.math.max

// Иниициализация отображения категорий
fun Prefs.fPrefCategory(sRights: String) {
    if (tAsync.isActive) {
        bStop = true
    }
    tAsync = GlobalScope.async(Dispatchers.Main) {
        // Получим перечень категорий
        val call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_perf)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData {
                append("exec", "getcategory")
            })
        }
        val sRequest = call.response.readText()
        jaSaveData = JSONArray(sRequest)
        fOutputCategory("0", "", sRights)
    }
}

// Вывод содержимого категории
fun Prefs.fOutputCategory(sID: String, sParent: String, sRights: String){
    llInfo.removeAllViews()
    val iID = sID.substringAfterLast(',',"0")

    // Заголовок
    val tvTitle = TextView(this)
    tvTitle.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    tvTitle.text = "Категории"
    tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    (tvTitle.layoutParams as LinearLayout.LayoutParams).setMargins(0,10,0,20)
    tvTitle.gravity = Gravity.CENTER
    tvTitle.setTextColor(Color.parseColor("#000000"))
    if (bStop){
        bStop = false
        return
    }
    llInfo.addView(tvTitle)

    // Кнопка возврата
    if (iID != "0") {
        val tvBack = TextView(this)
        tvBack.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tvBack.gravity = Gravity.START
        tvBack.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        tvBack.text = "[  . .  ]"
        tvBack.setOnClickListener {
            svPerMenu.visibility = View.GONE
            llEdit.visibility = View.GONE
            var iParentIndex = 0
            for (i in 0 until jaSaveData.length()){
                if (jaSaveData.getJSONObject(i).getString("iIDParent") == iID){
                    iParentIndex = i
                }
            }
            fOutputCategory(sID.substringBeforeLast(',', sID), jaSaveData.getJSONObject(iParentIndex).getString("iIDParent"), sRights)
        }
        if (bStop) {
            bStop = false
            return
        }
        llInfo.addView(tvBack)
    }

    // Перечень категорий
    for (i in 0 until jaSaveData.length()){
        if (jaSaveData.getJSONObject(i).getString("iIDParent") != iID){
            continue
        }
        val llCat = LinearLayout(this)
        llCat.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        llCat.orientation = LinearLayout.HORIZONTAL
        llCat.tag = jaSaveData.getJSONObject(i).getString("iIDChild")
        val flCat = FrameLayout(this)
        flCat.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        (flCat.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,0,0)
        val tvCat = TextView(this)
        tvCat.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tvCat.gravity = Gravity.START
        tvCat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        tvCat.setTextColor(Color.parseColor("#000000"))
        tvCat.text = jaSaveData.getJSONObject(i).getString("sChild")
        // При клике перейдем к категории
        llCat.setOnClickListener {
            svPerMenu.visibility = View.GONE
            llEdit.visibility = View.GONE
            fOutputCategory(sID + ',' + jaSaveData.getJSONObject(i).getString("iIDChild"), jaSaveData.getJSONObject(i).getString("sParent"), sRights)
        }

        // При долгом клике вызовем контекстное меню
        if (sRights == "R"){
            llCat.setOnLongClickListener {
                fCollapseCategory()
                Toast.makeText(this, "Отсутствует доступ для редактирования", Toast.LENGTH_SHORT).show()
                true
            }
        }
        else {
            llCat.setOnLongClickListener {
                fCollapseCategory()
                svPerMenu.visibility = View.GONE
                llEdit.visibility = View.VISIBLE
                llEdit.y = it.y + llEdit.height + it.height / 2
                // Редактирование категории
                ivEdit.setOnClickListener {
                    llEdit.visibility = View.GONE
                    fCategoryExpand(llCat, jaSaveData.getJSONObject(i).getInt("isusing"))
                }

                // Копирование категории
                ivCopy.setOnClickListener {
                    llEdit.visibility = View.GONE
                    ivPaste.visibility = View.VISIBLE
                    val joPaste = JSONObject()
                    joPaste.put("iIDParent", iID)
                    joPaste.put("sParent", jaSaveData.getJSONObject(i).getString("sParent"))
                    joPaste.put("iIDChild", jaSaveData.getJSONObject(i).getString("iIDChild"))
                    joPaste.put("sChild", jaSaveData.getJSONObject(i).getString("sChild"))
                    joPaste.put("isusing", jaSaveData.getJSONObject(i).getString("isusing"))
                    ivPaste.tag =joPaste
                }

                // Вырезание категории
                ivCut.setOnClickListener {
                    llEdit.visibility = View.GONE
                    ivPaste.visibility = View.VISIBLE
                    val joPaste = JSONObject()
                    joPaste.put("iIDParent", iID)
                    joPaste.put("sParent", jaSaveData.getJSONObject(i).getString("sParent"))
                    joPaste.put("iIDChild", jaSaveData.getJSONObject(i).getString("iIDChild"))
                    joPaste.put("sChild", jaSaveData.getJSONObject(i).getString("sChild"))
                    joPaste.put("isusing", jaSaveData.getJSONObject(i).getString("isusing"))
                    ivPaste.tag =joPaste
                    jaSaveData.remove(i)
                    llInfo.removeView(llCat)
                    tvOK.visibility = View.VISIBLE
                }
                true
            }
        }
        flCat.addView(tvCat)
        llCat.addView(flCat)
        if (bStop){
            bStop = false
            return
        }
        llInfo.addView(llCat)
    }

    // Кнопка добавления категории
    if (sRights == "W") {
        val tvNewCategory = TextView(this)
        tvNewCategory.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tvNewCategory.text = "+"
        tvNewCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        tvNewCategory.gravity = Gravity.CENTER
        tvNewCategory.setTextColor(Color.parseColor("#6200ee"))
        tvNewCategory.background = getDrawable(android.R.drawable.editbox_background)
        tvNewCategory.setOnClickListener {
            fCollapseCategory()
            svPerMenu.visibility = View.GONE
            llEdit.visibility = View.GONE
            aViews[0] = null

            val llCat = LinearLayout(this)
            llCat.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            llCat.orientation = LinearLayout.HORIZONTAL
            llCat.setOnClickListener {
                svPerMenu.visibility = View.GONE
                llEdit.visibility = View.GONE
                fOutputCategory(sID + ',' + llCat.tag.toString(), (((it as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as TextView).text.toString(), sRights)
            }

            // При долгом клике вызовем контекстное меню
            llCat.setOnLongClickListener {
                fCollapseCategory()
                svPerMenu.visibility = View.GONE
                llEdit.visibility = View.VISIBLE
                llEdit.y = it.y + llEdit.height + it.height / 2
                // Редактирование категории
                ivEdit.setOnClickListener {
                    llEdit.visibility = View.GONE
                    fCategoryExpand(llCat, 0)
                }

                // Копирование категории
                ivCopy.setOnClickListener {
                    llEdit.visibility = View.GONE
                    ivPaste.visibility = View.VISIBLE
                    val joPaste = JSONObject()
                    joPaste.put("iIDParent", iID)
                    joPaste.put("sParent", sParent)
                    joPaste.put("iIDChild", llCat.tag.toString())
                    joPaste.put("sChild", ((llCat.getChildAt(0) as FrameLayout).getChildAt(0) as TextView).text.toString())
                    joPaste.put("isusing", 0)
                    ivPaste.tag =joPaste
                }

                // Вырезание категории
                ivCut.setOnClickListener {
                    llEdit.visibility = View.GONE
                    ivPaste.visibility = View.VISIBLE
                    val joPaste = JSONObject()
                    joPaste.put("iIDParent", iID)
                    joPaste.put("sParent", sParent)
                    joPaste.put("iIDChild", llCat.tag.toString())
                    joPaste.put("sChild", ((llCat.getChildAt(0) as FrameLayout).getChildAt(0) as TextView).text.toString())
                    joPaste.put("isusing", 0)
                    ivPaste.tag =joPaste
                    var iDelIndex = 0
                    for (i in 0 until jaSaveData.length()){
                        if ((jaSaveData.getJSONObject(i).getString("iIDParent") == iID) &&
                            (jaSaveData.getJSONObject(i).getString("iIDChild") == llCat.tag.toString())){
                            iDelIndex = i
                        }
                    }
                    jaSaveData.remove(iDelIndex)
                    llInfo.removeView(llCat)
                    tvOK.visibility = View.VISIBLE
                }
                true
            }
            val flCat = FrameLayout(this)
            flCat.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            (flCat.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,0,0)

            if (ivPaste.visibility == View.VISIBLE){
                // Вставка из буфера
                if (iID == (ivPaste.tag as JSONObject).getString("iIDChild")){
                    Toast.makeText(this, "Категорию нельзя вставить саму в себя", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                ivPaste.visibility = View.INVISIBLE
                llCat.tag = (ivPaste.tag as JSONObject).getString("iIDChild")
                val tvCat = TextView(this)
                tvCat.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                tvCat.gravity = Gravity.START
                tvCat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                tvCat.setTextColor(Color.parseColor("#000000"))
                tvCat.text = (ivPaste.tag as JSONObject).getString("sChild")
                flCat.addView(tvCat)
                llCat.addView(flCat)
                llInfo.addView(llCat, llInfo.indexOfChild(it))
                val joSaveData = JSONObject((ivPaste.tag as JSONObject).toString())
                joSaveData.put("iIDParent", iID)
                joSaveData.put("sParent", sParent)
                joSaveData.remove("Oper")
                jaSaveData.put(joSaveData)
                tvOK.visibility = View.VISIBLE
            }
            else{
                // Вставка нового элемента
                bExpanded = true
                var iMaxNewIndex = 0
                // Найдем новый идентификатор для нового атрибута
                for (i in 0 until jaSaveData.length()){
                    if (jaSaveData.getJSONObject(i).getString("iIDParent")[0] == '_'){
                        iMaxNewIndex = max((jaSaveData.getJSONObject(i).getString("iIDParent").substringAfter('_').toInt() + 1), iMaxNewIndex)
                    }
                    if (jaSaveData.getJSONObject(i).getString("iIDChild")[0] == '_'){
                        iMaxNewIndex = max((jaSaveData.getJSONObject(i).getString("iIDChild").substringAfter('_').toInt() + 1), iMaxNewIndex)
                    }
                }
                llCat.tag = "_" + iMaxNewIndex.toString()
                val etCat = EditText(this)
                etCat.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                etCat.requestFocus()
                flCat.addView(etCat)
                llCat.addView(flCat)
                llInfo.addView(llCat, llInfo.indexOfChild(it))

                // Кнопки
                val llBut = LinearLayout(this)
                llBut.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                llBut.orientation = LinearLayout.HORIZONTAL
                val iwCancel = ImageView(this)
                iwCancel.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                iwCancel.setImageResource(R.drawable.cancel)
                iwCancel.scaleType = ImageView.ScaleType.CENTER_INSIDE
                val iwOK = ImageView(this)
                iwCancel.setOnClickListener {
                    svPerMenu.visibility = View.GONE
                    llEdit.visibility = View.GONE
                    fCollapseCategory()
                }
                iwOK.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                iwOK.setImageResource(R.drawable.done)
                iwOK.scaleType = ImageView.ScaleType.CENTER_INSIDE
                iwOK.setOnClickListener {
                    svPerMenu.visibility = View.GONE
                    llEdit.visibility = View.GONE
                    if (((llCat.getChildAt(0) as FrameLayout).getChildAt(0) as EditText).text.isNotEmpty()) {
                        // При создании нового атрибута создадим для него новые виды
                        tvOK.visibility = View.VISIBLE
                        val tvCategory = TextView(this)
                        tvCategory.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                        tvCategory.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                        tvCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        tvCategory.text = ((llCat.getChildAt(0) as FrameLayout).getChildAt(0) as EditText).text.toString()
                        tvCategory.setTextColor(Color.parseColor("#000000"))
                        aViews[0] = tvCategory
                        val joSaveData = JSONObject()
                        joSaveData.put("iIDParent", iID)
                        joSaveData.put("sParent", sParent)
                        joSaveData.put("iIDChild", llCat.tag.toString())
                        joSaveData.put("sChild", tvCategory.text.toString())
                        joSaveData.put("isusing", 0)
                        jaSaveData.put(joSaveData)
                        fCollapseCategory()
                    } else {
                        Toast.makeText(this, "Пустое имя категории не допускается", Toast.LENGTH_SHORT).show()
                    }
                }
                llBut.addView(iwCancel)
                llBut.addView(iwOK)
                llInfo.addView(llBut, llInfo.indexOfChild(it))
            }
        }
        if (bStop) {
            bStop = false
            return
        }
        llInfo.addView(tvNewCategory)

    }
    val spSpace = Space(this)
    spSpace.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200)
    llInfo.addView(spSpace)
}

// Разворачивание категории для редактирования
fun Prefs.fCategoryExpand(view: View, isusing: Int){
    if (((view as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) is EditText){
        return
    }
    bExpanded = true
    aViews[0] = (view.getChildAt(0) as FrameLayout).getChildAt(0)
    (view.getChildAt(0) as FrameLayout).removeAllViews()
    val etCategoryName = EditText(this)
    etCategoryName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    etCategoryName.setText((aViews[0] as TextView).text)
    (view.getChildAt(0) as FrameLayout).addView(etCategoryName)
    etCategoryName.requestFocus()
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
        fCollapseCategory()
    }
    val iwOK = ImageView(this)
    iwOK.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    iwOK.setImageResource(R.drawable.done)
    iwOK.scaleType = ImageView.ScaleType.CENTER_INSIDE
    iwOK.setOnClickListener {
        svPerMenu.visibility = View.GONE
        tvOK.visibility = View.VISIBLE
        (aViews[0] as TextView).text = ((view.getChildAt(0) as FrameLayout).getChildAt(0) as EditText).text.toString()
        for (i in 0 until jaSaveData.length()){
            if (jaSaveData.getJSONObject(i).getString("iIDParent") == view.tag.toString()){
                jaSaveData.getJSONObject(i).put("sParent",(aViews[0] as TextView).text.toString())
            }
            if (jaSaveData.getJSONObject(i).getString("iIDChild") == view.tag.toString()){
                jaSaveData.getJSONObject(i).put("sChild",(aViews[0] as TextView).text.toString())
            }
        }
        fCollapseCategory()
    }
    llBut.addView(iwCancel)
    llBut.addView(iwOK)
    if (isusing == 0) {
        val iwDel = ImageView(this)
        iwDel.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        iwDel.setImageResource(R.drawable.delete)
        iwDel.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iwDel.setOnClickListener {
            svPerMenu.visibility = View.GONE
            tvOK.visibility = View.VISIBLE
            val dlAttrDel = AlertDialog.Builder(this)
            dlAttrDel.setTitle("Предупреждение")
            dlAttrDel.setMessage("Удалить категорию?")
            dlAttrDel.setPositiveButton("Да") { _, _ ->
                tvOK.visibility = View.VISIBLE
                var iDelIndex = 0
                for (i in 0 until jaSaveData.length()){
                    if (jaSaveData.getJSONObject(i).getString("iIDChild") == view.tag.toString()){
                        iDelIndex = i
                    }
                }
                jaSaveData.remove(iDelIndex)
                aViews[0] = null
                fCollapseCategory()
            }
            dlAttrDel.setNegativeButton("Нет") { _, _ ->
            }
            dlAttrDel.show()
        }
        llBut.addView(iwDel)
    }
    llInfo.addView(llBut,(view.parent as LinearLayout).indexOfChild(view) + 1)
}

// Сворачивание категории
fun Prefs.fCollapseCategory(){
    if (!bExpanded){
        return
    }
    bExpanded = false
    for (i in 0 until llInfo.childCount){
        if (llInfo.getChildAt(i) is LinearLayout){
            if (((llInfo.getChildAt(i) as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) is EditText) {
                llInfo.removeViewAt(i + 1)
                if (aViews[0] != null) {
                    ((llInfo.getChildAt(i) as LinearLayout).getChildAt(0) as FrameLayout).removeAllViews()
                    ((llInfo.getChildAt(i) as LinearLayout).getChildAt(0) as FrameLayout).addView(aViews[0])
                }
                else{
                    llInfo.removeViewAt(i)
                }
            }
        }
    }
}

// Сохранение категорий
suspend fun Prefs.fSaveCategory():Int {
    val log = Logger.getAnonymousLogger()
    val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
        method = HttpMethod.Post
        body = MultiPartFormDataContent(formData {
            append("exec", "savecategory")
            append("jsonattrs",jaSaveData.toString())
        })
    }
    val sRes = call.response.readText()
    log.warning(sRes)
    return 0 //sRes.toInt()
}