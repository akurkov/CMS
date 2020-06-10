// Настройка атрибутов

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
import kotlinx.android.synthetic.main.dialog_attrvalues.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray
import org.json.JSONObject

// Отображение атрибутов
fun Prefs.fPrefAttr(sRights: String){
    if (tAsync.isActive){
        bStop = true
    }
    jaSaveData = JSONArray()
    tAsync = GlobalScope.async(Dispatchers.Main) {
        llInfo.removeAllViews()

        // Заголовок
        val tvTitle = TextView(this@fPrefAttr)
        tvTitle.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tvTitle.text = "Атрибуты"
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        (tvTitle.layoutParams as LinearLayout.LayoutParams).setMargins(0,10,0,20)
        tvTitle.gravity = Gravity.CENTER
        tvTitle.setTextColor(Color.parseColor("#000000"))
        if (bStop){
            bStop = false
            return@async
        }
        llInfo.addView(tvTitle)

        // Кнопка перехода к настройкам привязки атрибутов к объектам
        val tvAttrObject = TextView(this@fPrefAttr)
        tvAttrObject.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tvAttrObject.text = " Привязка к объектам "
        tvAttrObject.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        (tvAttrObject.layoutParams as LinearLayout.LayoutParams).setMargins(30,0,0,0)
        tvAttrObject.gravity = Gravity.START
        tvAttrObject.setTextColor(Color.parseColor("#6200ee"))
        tvAttrObject.background = getDrawable(android.R.drawable.editbox_background)
        tvAttrObject.setOnClickListener {
            svPerMenu.visibility = View.GONE
            fAttrObject(sRights)
        }
        if (bStop){
            bStop = false
            return@async
        }
        llInfo.addView(tvAttrObject)

        // Получим перечень типов атрибутов
        var call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_perf)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData {
                append("exec", "getattrtypes")
            })
        }
        var sRequest = call.response.readText()
        var aJSONArray = JSONArray(sRequest)
        val aAttrTypes = mutableListOf<String>()
        for (i in 0 until aJSONArray.length()){
            aAttrTypes.add(aJSONArray.getJSONObject(i).getString("sName"))
        }

        // Получим перечень атрибутов
        call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_perf)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData {
                append("exec", "getattrs")
            })
        }
        sRequest = call.response.readText()
        aJSONArray = JSONArray(sRequest)
        for (i in 0 until aJSONArray.length()){
            val llAttr = LinearLayout(this@fPrefAttr)
            llAttr.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            llAttr.orientation = LinearLayout.HORIZONTAL
            llAttr.tag = aJSONArray.getJSONObject(i).getString("iID")
            val flAttrName = FrameLayout(this@fPrefAttr)
            flAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.35f)
            (flAttrName.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,5,0)
            val tvAttrName = TextView(this@fPrefAttr)
            tvAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            tvAttrName.gravity = Gravity.START + Gravity.CENTER_VERTICAL
            tvAttrName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tvAttrName.text = aJSONArray.getJSONObject(i).getString("sName")
            val flAttrType = FrameLayout(this@fPrefAttr)
            flAttrType.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.65f)
            (flAttrType.layoutParams as LinearLayout.LayoutParams).setMargins(5,0,0,0)
            val tvAttrType = TextView(this@fPrefAttr)
            tvAttrType.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            tvAttrType.gravity = Gravity.START + Gravity.CENTER_VERTICAL
            tvAttrType.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tvAttrType.text = aJSONArray.getJSONObject(i).getString("sType")

            // При долгом клике вызовем окно редактироавния значений перечисления
            llAttr.setOnLongClickListener {
                svPerMenu.visibility = View.GONE
                fsetAttrValues(it, aJSONArray.getJSONObject(i).getString("sValues"), sRights)
                true
            }

            // Проверим можно ли редактировать атрибут
            if ((sRights == "R") || (aJSONArray.getJSONObject(i).getInt("System") == 1)){
                // Редактировать нельзя, предупредим
                tvAttrName.setTextColor(Color.parseColor("#909090"))
                tvAttrType.setTextColor(Color.parseColor("#909090"))
                val sAttrEditMessage = if (sRights == "R") "Отсутствует доступ для редактирования" else "Системный атрибут недоступен для редактирования"
                llAttr.setOnClickListener {
                    svPerMenu.visibility = View.GONE
                    Toast.makeText(this@fPrefAttr, sAttrEditMessage, Toast.LENGTH_SHORT).show()
                }
            }
            else {
                // Редактировать можно
                tvAttrName.setTextColor(Color.parseColor("#000000"))
                tvAttrType.setTextColor(Color.parseColor("#000000"))
                llAttr.setOnClickListener {
                    svPerMenu.visibility = View.GONE
                    fAttrExpand(it, aJSONArray.getJSONObject(i).getInt("isusing"), aAttrTypes, aJSONArray.getJSONObject(i).getString("sValues"))
                }
            }
            flAttrName.addView(tvAttrName)
            flAttrType.addView(tvAttrType)
            llAttr.addView(flAttrName)
            llAttr.addView(flAttrType)
            if (bStop){
                bStop = false
                return@async
            }
            llInfo.addView(llAttr)
        }

        // Кнопка добавления новго атрибута
        if (sRights == "W") {
            val tvNewAttr = TextView(this@fPrefAttr)
            tvNewAttr.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            tvNewAttr.text = "+"
            tvNewAttr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            tvNewAttr.gravity = Gravity.CENTER
            tvNewAttr.setTextColor(Color.parseColor("#6200ee"))
            tvNewAttr.background = getDrawable(android.R.drawable.editbox_background)
            tvNewAttr.setOnClickListener {
                svPerMenu.visibility = View.GONE
                if (bExpanded) {
                    fCollapseAttr()
                }
                // Перед разворачиванием сохраним разворачиваемые элементы
                bExpanded = true
                aViews[0] = null
                aViews[1] = null
                val llAttr = LinearLayout(this@fPrefAttr)
                llAttr.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                llAttr.orientation = LinearLayout.HORIZONTAL
                llAttr.setOnClickListener {
                    svPerMenu.visibility = View.GONE
                    fAttrExpand(it, 0, aAttrTypes,"")
                }
                llAttr.setOnLongClickListener {
                    svPerMenu.visibility = View.GONE
                    fsetAttrValues(it, "", sRights)
                    true
                }
                llInfo.addView(llAttr, llInfo.indexOfChild(it))
                val flAttrName = FrameLayout(this@fPrefAttr)
                flAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.35f)
                (flAttrName.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 5, 0)
                llAttr.addView(flAttrName)
                val flAttrType = FrameLayout(this@fPrefAttr)
                flAttrType.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.65f)
                (flAttrType.layoutParams as LinearLayout.LayoutParams).setMargins(5, 0, 0, 0)
                llAttr.addView(flAttrType)
                val etAttrName = EditText(this@fPrefAttr)
                etAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                flAttrName.addView(etAttrName)
                etAttrName.requestFocus()
                val spAttrType = Spinner(this@fPrefAttr)
                spAttrType.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                val adAttrType = ArrayAdapter(this@fPrefAttr, android.R.layout.simple_spinner_item, aAttrTypes)
                adAttrType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spAttrType.adapter = adAttrType
                flAttrType.addView(spAttrType)
                val llBut = LinearLayout(this@fPrefAttr)
                llBut.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                llBut.orientation = LinearLayout.HORIZONTAL
                val iwCancel = ImageView(this@fPrefAttr)
                iwCancel.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                iwCancel.setImageResource(R.drawable.cancel)
                iwCancel.scaleType = ImageView.ScaleType.CENTER_INSIDE
                val iwOK = ImageView(this@fPrefAttr)
                iwCancel.setOnClickListener {
                    svPerMenu.visibility = View.GONE
                    fCollapseAttr()
                }
                iwOK.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                iwOK.setImageResource(R.drawable.done)
                iwOK.scaleType = ImageView.ScaleType.CENTER_INSIDE
                iwOK.setOnClickListener {
                    svPerMenu.visibility = View.GONE
                    if ((((llInfo.getChildAt(llInfo.indexOfChild(it.parent as LinearLayout) - 1) as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as TextView).text.isNotEmpty()) {
                        // При создании нового атрибута создадим для него новые виды
                        tvOK.visibility = View.VISIBLE
                        val tvAttrName = TextView(this@fPrefAttr)
                        tvAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                        tvAttrName.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                        tvAttrName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        tvAttrName.text = (((llInfo.getChildAt(llInfo.indexOfChild(it.parent as LinearLayout) - 1) as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as TextView).text.toString()
                        tvAttrName.setTextColor(Color.parseColor("#000000"))
                        aViews[0] = tvAttrName
                        val tvAttrType = TextView(this@fPrefAttr)
                        tvAttrType.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                        tvAttrType.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                        tvAttrType.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        tvAttrType.text = (((llInfo.getChildAt(llInfo.indexOfChild(it.parent as LinearLayout) - 1) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as Spinner).selectedItem.toString()
                        tvAttrType.setTextColor(Color.parseColor("#000000"))
                        aViews[1] = tvAttrType
                        // Найдем новый идентификатор для нового атрибута
                        var iMaxNewIndex = 0
                        for (j in 0 until jaSaveData.length()){
                            if (jaSaveData.getJSONObject(j).getString("iID")[0] == '_'){
                                iMaxNewIndex = jaSaveData.getJSONObject(j).getString("iID").substringAfter('_').toInt() + 1
                            }
                        }
                        val joSaveData = JSONObject()
                        joSaveData.put("iID","_" + iMaxNewIndex.toString())
                        joSaveData.put("AttrName", tvAttrName.text.toString())
                        joSaveData.put("AttrType", tvAttrType.text.toString())
                        joSaveData.put("sValues","")
                        joSaveData.put("Del",false)
                        llAttr.tag = "_" + iMaxNewIndex.toString()
                        jaSaveData.put(joSaveData)
                        fCollapseAttr()
                    } else {
                        Toast.makeText(this@fPrefAttr, "Пустое имя атрибута не допускается", Toast.LENGTH_SHORT).show()
                    }
                }
                llBut.addView(iwCancel)
                llBut.addView(iwOK)
                llInfo.addView(llBut, llInfo.indexOfChild(it))
            }
            if (bStop) {
                bStop = false
                return@async
            }
            llInfo.addView(tvNewAttr)
        }
        val spSpace = Space(this@fPrefAttr)
        spSpace.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200)
        llInfo.addView(spSpace)
    }
}

// Вход в режим редактирования атрибута
fun Prefs.fAttrExpand(llAttr: View, isusing: Int, aAttrTypes: MutableList<String>, sValues: String){
    if (((llAttr as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) is EditText){
        return
    }
    if (bExpanded){
        fCollapseAttr()
    }
    // Развернем
    bExpanded = true
    aViews[0] = (llAttr.getChildAt(0) as FrameLayout).getChildAt(0)
    aViews[1] = (llAttr.getChildAt(1) as FrameLayout).getChildAt(0)
    (llAttr.getChildAt(0) as FrameLayout).removeAllViews()
    // Наименование атрибута сделаем редактируемым
    val etAttrName = EditText(this)
    etAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    etAttrName.setText((aViews[0] as TextView).text)
    (llAttr.getChildAt(0) as FrameLayout).addView(etAttrName)
    etAttrName.requestFocus()
    // Тип атрибута сделаем редактируемым, если он не используется
    if (isusing == 0) {
        (llAttr.getChildAt(1) as FrameLayout).removeAllViews()
        val spAttrType = Spinner(this)
        spAttrType.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        val adAttrType = ArrayAdapter(this, android.R.layout.simple_spinner_item, aAttrTypes)
        adAttrType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAttrType.adapter = adAttrType
        spAttrType.setSelection(aAttrTypes.indexOf((aViews[1] as TextView).text.toString()))
        (llAttr.getChildAt(1) as FrameLayout).addView(spAttrType)
    }
    else {
        (llAttr.getChildAt(1) as FrameLayout).getChildAt(0).setOnClickListener {
            svPerMenu.visibility = View.GONE
            Toast.makeText(this, "Невозможно сменить тип у уже использующегося атрибута", Toast.LENGTH_SHORT).show()
        }
    }
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
        fCollapseAttr()
    }
    val iwOK = ImageView(this)
    iwOK.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    iwOK.setImageResource(R.drawable.done)
    iwOK.scaleType = ImageView.ScaleType.CENTER_INSIDE
    iwOK.setOnClickListener {
        svPerMenu.visibility = View.GONE
        tvOK.visibility = View.VISIBLE
        var iAttrID = -1
        for (i in 0 until jaSaveData.length()){
            if (jaSaveData.getJSONObject(i).getString("iID") == llAttr.tag.toString()){
                iAttrID = i
            }
        }
        (aViews[0] as TextView).text = (((llInfo.getChildAt(llInfo.indexOfChild(it.parent as LinearLayout) - 1) as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as TextView).text.toString()
        if (((llInfo.getChildAt(llInfo.indexOfChild(it.parent as LinearLayout) - 1 ) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) is Spinner) {
            (aViews[1] as TextView).text = (((llInfo.getChildAt(llInfo.indexOfChild(it.parent as LinearLayout) - 1) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as Spinner).selectedItem.toString()
        }
        // Сохраним изменения
        val joSaveData = JSONObject()
        joSaveData.put("iID", llAttr.tag.toString())
        joSaveData.put("AttrName", (aViews[0] as TextView).text.toString())
        joSaveData.put("AttrType", (aViews[1] as TextView).text.toString())
        joSaveData.put("sValues",sValues)
        joSaveData.put("Del",false)
        if (iAttrID < 0) {
            jaSaveData.put(joSaveData)
        }
        else{
            jaSaveData.put(iAttrID, joSaveData)
        }
        fCollapseAttr()
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
            dlAttrDel.setMessage("Удалить атрибут?")
            dlAttrDel.setPositiveButton("Да") { _, _ ->
                tvOK.visibility = View.VISIBLE
                var iAttrID = -1
                for (i in 0 until jaSaveData.length()){
                    if (jaSaveData.getJSONObject(i).getString("iID") == llAttr.tag.toString()){
                        iAttrID = i
                    }
                }
                if (iAttrID < 0){
                    val joSaveData = JSONObject()
                    joSaveData.put("iID", llAttr.tag.toString())
                    joSaveData.put("AttrName","")
                    joSaveData.put("AttrType","")
                    joSaveData.put("sValues","")
                    joSaveData.put("Del",true)
                    jaSaveData.put(joSaveData)
                }
                else{
                    if (llAttr.tag.toString()[0] == '_'){
                        jaSaveData.remove(iAttrID)
                    }
                    else{
                        jaSaveData.getJSONObject(iAttrID).put("Del",true)
                    }

                }
                aViews[0] = null
                aViews[1] = null
                fCollapseAttr()
            }
            dlAttrDel.setNegativeButton("Нет") { _, _ ->
            }
            dlAttrDel.show()
        }
        llBut.addView(iwDel)
    }
    llInfo.addView(llBut,(llAttr.parent as LinearLayout).indexOfChild(llAttr) + 1)
}

// Выход из режима редактирования
fun Prefs.fCollapseAttr(){
    bExpanded = false
    for (i in 0 until llInfo.childCount){
        if (llInfo.getChildAt(i) is LinearLayout){
            if (((llInfo.getChildAt(i) as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) is EditText) {
                llInfo.removeViewAt(i + 1)
                if (aViews[0] != null) {
                    ((llInfo.getChildAt(i) as LinearLayout).getChildAt(0) as FrameLayout).removeAllViews()
                    ((llInfo.getChildAt(i) as LinearLayout).getChildAt(0) as FrameLayout).addView(aViews[0])
                    ((llInfo.getChildAt(i) as LinearLayout).getChildAt(1) as FrameLayout).removeAllViews()
                    ((llInfo.getChildAt(i) as LinearLayout).getChildAt(1) as FrameLayout).addView(aViews[1])
                    ((llInfo.getChildAt(i) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0).setOnClickListener{ llInfo.getChildAt(i).performClick() }
                }
                else{
                    llInfo.removeViewAt(i)
                }
            }
        }
    }
}

// Редактирование перечня значений атрибутов
fun Prefs.fsetAttrValues(llAttr: View, sValues: String, sRights: String){
    if ((((llAttr as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString() == "Перечисление") {
        if (sRights == "R") {
            Toast.makeText(this, "Отсутствует доступ для редактирования", Toast.LENGTH_SHORT).show()
        }
        else {
            var sList = sValues
            var iAttrID = -1
            for (i in 0 until jaSaveData.length()){
                if (jaSaveData.getJSONObject(i).getString("iID") == llAttr.tag.toString()){
                    iAttrID = i
                }
            }
            if (iAttrID >= 0){
                sList = jaSaveData.getJSONObject(iAttrID).getString("sValues")
            }
            val dlAttrValues = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.dialog_attrvalues, null)
            dialogLayout.etAttrValues.setText(sList.replace('#','\n'))
            dialogLayout.etAttrValues.visibility = View.VISIBLE
            dlAttrValues.setView(dialogLayout)
            dlAttrValues.setPositiveButton("Сохранить") { _, _ ->
                tvOK.visibility = View.VISIBLE
                dialogLayout.etAttrValues.visibility = View.GONE
                val joSaveData = JSONObject()
                joSaveData.put("iID", llAttr.tag.toString())
                joSaveData.put("AttrName",((llAttr.getChildAt(0) as FrameLayout).getChildAt(0) as TextView).text.toString())
                joSaveData.put("AttrType","Перечисление")
                joSaveData.put("sValues",dialogLayout.etAttrValues.text.toString().replace('\n','#'))
                joSaveData.put("Del",false)
                if (iAttrID >= 0){
                    jaSaveData.put(iAttrID, joSaveData)
                }
                else{
                    jaSaveData.put(joSaveData)
                }

            }
            dlAttrValues.setNegativeButton("Отмена") { _, _ ->
            }
            dlAttrValues.show()
        }
    }
}

// Сохранение атрибутов на сервере
suspend fun Prefs.fSaveAttr(): Int {
    val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
        method = HttpMethod.Post
        body = MultiPartFormDataContent(formData {
            append("exec", "saveattrs")
            append("jsonattrs",jaSaveData.toString())
        })
    }
    val sRes = call.response.readText()
    val aJSONArray = JSONArray(sRes)
    // Установим идентификаторы элементам с атрибуатми
    for (i in 0 until llInfo.childCount){
        if (llInfo.getChildAt(i) is LinearLayout){
            val sViewText = (((llInfo.getChildAt(i) as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as TextView).text.toString()
            for (j in 0 until aJSONArray.length()){
                if (aJSONArray.getJSONObject(j).getString("sName") == sViewText){
                    llInfo.getChildAt(i).tag = aJSONArray.getJSONObject(j).getString("iID")
                }
            }
        }
    }
    jaSaveData = JSONArray()
    return 0
}

// Отображение привязки атрибутов к объектам
fun Prefs.fAttrObject(sRights: String){
    sPerfWindow = "ОбъектыАтрибуты"
    if (tAsync.isActive){
        bStop = true
    }
    jaSaveData = JSONArray()
    tAsync = GlobalScope.async(Dispatchers.Main) {
        // Получим с сервера атрибуты, объекты и свзи атрибутов с объектами
        var call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData {
                append("exec", "getattrs")
            })
        }
        var sRes = call.response.readText()
        val aJSONAttrs = JSONArray(sRes)
        call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData {
                append("exec", "getobjectsattrs")
            })
        }
        sRes = call.response.readText()
        val aJSONObjectsAttrs = JSONArray(sRes)
        call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData {
                append("exec", "getobjects")
            })
        }
        sRes = call.response.readText()
        val aJSONObjects = JSONArray(sRes)

        llInfo.removeAllViews()

        // Заголовок
        val tvTitle = TextView(this@fAttrObject)
        tvTitle.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tvTitle.text = "Привязка к объектам"
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        (tvTitle.layoutParams as LinearLayout.LayoutParams).setMargins(0,10,0,20)
        tvTitle.gravity = Gravity.CENTER
        tvTitle.setTextColor(Color.parseColor("#000000"))
        if (bStop){
            bStop = false
            return@async
        }
        llInfo.addView(tvTitle)

        // Перечень объектов
        val svObjects = HorizontalScrollView(this@fAttrObject)
        svObjects.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        (svObjects.layoutParams as LinearLayout.LayoutParams).setMargins(10,0,10,10)
        svObjects.isHorizontalScrollBarEnabled = false
        val llObjects = LinearLayout(this@fAttrObject)
        llObjects.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        for (i in 0 until aJSONObjects.length()){
            val tvObject = TextView(this@fAttrObject)
            tvObject.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            tvObject.text = aJSONObjects.getJSONObject(i).getString("sName")
            tvObject.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            (tvObject.layoutParams as LinearLayout.LayoutParams).setMargins(5,0,5,0)
            tvObject.setTextColor(Color.parseColor("#808080"))
            // Выбор объекта
            tvObject.setOnClickListener {
                svPerMenu.visibility = View.GONE
                svObjects.tag = (it as TextView).text.toString()
                // Выделим объект цветом
                for (j in 0 until llObjects.childCount){
                    if (llObjects.getChildAt(j) == it){
                        it.setTextColor(Color.parseColor("#000000"))
                    }
                    else{
                        (llObjects.getChildAt(j) as TextView).setTextColor(Color.parseColor("#808080"))
                    }
                }

                // Отобразим привязки
                for (j in 2 until llInfo.childCount){
                    if (llInfo.getChildAt(j) is LinearLayout){
                        val llAttr = llInfo.getChildAt(j) as LinearLayout
                        ((llAttr.getChildAt(0) as FrameLayout).getChildAt(0) as CheckBox).setOnCheckedChangeListener(null)
                        var iAttrIndex = -1
                        // Поищем в изначальных установках
                        for (k in 0 until aJSONObjectsAttrs.length()){
                            if ((aJSONObjectsAttrs.getJSONObject(k).getString("sObjectName") == it.text.toString()) &&
                                (aJSONObjectsAttrs.getJSONObject(k).getString("sAttrName") == ((llAttr.getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString())){
                                ((llAttr.getChildAt(0) as FrameLayout).getChildAt(0) as CheckBox).isChecked = true
                                if (aJSONObjectsAttrs.getJSONObject(k).getInt("bRequire") == 1){
                                    ((llAttr.getChildAt(2) as FrameLayout).getChildAt(0) as ImageView).clearColorFilter()
                                }
                                else{
                                    ((llAttr.getChildAt(2) as FrameLayout).getChildAt(0) as ImageView).setColorFilter(Color.parseColor("#D0D0D0"))
                                }
                                iAttrIndex = k
                            }
                        }
                        var bROCheck = false
                        if (iAttrIndex < 0){
                            ((llAttr.getChildAt(0) as FrameLayout).getChildAt(0) as CheckBox).isChecked = false
                            ((llAttr.getChildAt(2) as FrameLayout).getChildAt(0) as ImageView).setColorFilter(Color.parseColor("#D0D0D0"))
                        }
                        else{
                            if (aJSONObjectsAttrs.getJSONObject(iAttrIndex).getInt("isusing") > 0){
                                bROCheck = true
                            }
                        }

                        // Поищем в сделанных изменениях
                        iAttrIndex = -1
                        for (k in 0 until jaSaveData.length()){
                            if ((jaSaveData.getJSONObject(k).getString("sObjectName") == it.text.toString()) &&
                                (jaSaveData.getJSONObject(k).getString("sAttrName") == ((llAttr.getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString())){
                                iAttrIndex = k
                                ((llAttr.getChildAt(0) as FrameLayout).getChildAt(0) as CheckBox).isChecked = jaSaveData.getJSONObject(k).getBoolean("bChecked")
                                if (jaSaveData.getJSONObject(k).getBoolean("bRequire")){
                                    ((llAttr.getChildAt(2) as FrameLayout).getChildAt(0) as ImageView).clearColorFilter()
                                }
                                else{
                                    ((llAttr.getChildAt(2) as FrameLayout).getChildAt(0) as ImageView).setColorFilter(Color.parseColor("#D0D0D0"))
                                }
                            }
                        }

                        // Поставим слушателей на клики. Checkbox
                        ((llAttr.getChildAt(0) as FrameLayout).getChildAt(0) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked ->
                            svPerMenu.visibility = View.GONE
                            if (sRights == "R"){
                                Toast.makeText(this@fAttrObject, "Недостаточно прав для изменения привязок",Toast.LENGTH_SHORT).show()
                                buttonView.isChecked = !isChecked
                                return@setOnCheckedChangeListener
                            }
                            if (bROCheck && (!isChecked)){
                                Toast.makeText(this@fAttrObject, "Атрибут используется, нельзя менять привязку",Toast.LENGTH_SHORT).show()
                                buttonView.isChecked = true
                                return@setOnCheckedChangeListener
                            }
                            tvOK.visibility = View.VISIBLE
                            if (!isChecked){
                                ((llAttr.getChildAt(2) as FrameLayout).getChildAt(0) as ImageView).setColorFilter(Color.parseColor("#D0D0D0"))
                            }
                            val joAttr = JSONObject()
                            joAttr.put("sObjectName", svObjects.tag.toString())
                            joAttr.put("sAttrName", ((llAttr.getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString())
                            joAttr.put("bChecked", isChecked)
                            joAttr.put("bRequire", ((llAttr.getChildAt(2) as FrameLayout).getChildAt(0) as ImageView).colorFilter == null)
                            if (iAttrIndex < 0){
                                jaSaveData.put(joAttr)
                            }
                            else{
                                jaSaveData.put(iAttrIndex, joAttr)
                            }
                        }

                        // обязательный признак
                        ((llAttr.getChildAt(2) as FrameLayout).getChildAt(0) as ImageView).setOnClickListener {
                            svPerMenu.visibility = View.GONE
                            if (sRights == "R"){
                                Toast.makeText(this@fAttrObject, "Недостаточно прав для изменения привязок",Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }
                            if (!((llAttr.getChildAt(0) as FrameLayout).getChildAt(0) as CheckBox).isChecked){
                                return@setOnClickListener
                            }
                            tvOK.visibility = View.VISIBLE
                            var bRequire = false
                            if ((it as ImageView).colorFilter == null){
                                it.setColorFilter(Color.parseColor("#D0D0D0"))
                            }
                            else{
                                it.clearColorFilter()
                                bRequire = true
                            }
                            val joAttr = JSONObject()
                            joAttr.put("sObjectName", svObjects.tag.toString())
                            joAttr.put("sAttrName", ((llAttr.getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString())
                            joAttr.put("bChecked", true)
                            joAttr.put("bRequire", bRequire)
                            if (iAttrIndex < 0){
                                jaSaveData.put(joAttr)
                            }
                            else{
                                jaSaveData.put(iAttrIndex, joAttr)
                            }
                        }
                    }
                }
            }
            llObjects.addView(tvObject)
        }
        svObjects.addView(llObjects)
        if (bStop){
            bStop = false
            return@async
        }
        llInfo.addView(svObjects)

        // Добавим атрибуты
        for (i in 0 until aJSONAttrs.length()){
            val llAttr = LinearLayout(this@fAttrObject)
            llAttr.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            llAttr.orientation = LinearLayout.HORIZONTAL
            val flCheck = FrameLayout(this@fAttrObject)
            flCheck.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
            val cbAttr = CheckBox(this@fAttrObject)
            cbAttr.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cbAttr.gravity = Gravity.CENTER
            val flAttrName = FrameLayout(this@fAttrObject)
            flAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.2f)
            val tvAttrName = TextView(this@fAttrObject)
            tvAttrName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            tvAttrName.gravity = Gravity.START + Gravity.CENTER_VERTICAL
            tvAttrName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tvAttrName.text = aJSONAttrs.getJSONObject(i).getString("sName")
            tvAttrName.setTextColor(Color.parseColor("#000000"))
            val flAttrReq = FrameLayout(this@fAttrObject)
            flAttrReq.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
            val iwAttrReq = ImageView(this@fAttrObject)
            iwAttrReq.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            iwAttrReq.scaleType = ImageView.ScaleType.CENTER_INSIDE
            iwAttrReq.setImageResource(R.drawable.attention)
            iwAttrReq.setColorFilter(Color.parseColor("#D0D0D0"))
            flCheck.addView(cbAttr)
            flAttrName.addView(tvAttrName)
            flAttrReq.addView(iwAttrReq)
            llAttr.addView(flCheck)
            llAttr.addView(flAttrName)
            llAttr.addView(flAttrReq)
            if (bStop){
                bStop = false
                return@async
            }
            llInfo.addView(llAttr)
        }
        val spSpace = Space(this@fAttrObject)
        spSpace.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200)
        llInfo.addView(spSpace)

        // Выберем первый объект
        ((llInfo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as LinearLayout).getChildAt(0).performClick()
    }
}

// Сохранение привязок атрибутов на сервере
suspend fun Prefs.fSaveObjectsAttrs(): Int {
    val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_perf)) {
        method = HttpMethod.Post
        body = MultiPartFormDataContent(formData {
            append("exec", "saveobjectsattrs")
            append("jsonattrs",jaSaveData.toString())
        })
    }
    val sRes = call.response.readText()
    jaSaveData = JSONArray()
    return sRes.toInt()
}

