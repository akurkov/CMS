// Вывод настроек
package com.product.cms

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import kotlinx.android.synthetic.main.activity_prefs.*
import kotlinx.coroutines.*
import org.json.JSONArray

class Prefs : AppCompatActivity() {

    private var sLogin = "" // Имя пользователя
    private var sRole = "" // Роль рользователя
    private var sMode = "" // Режим отображения настроек
    private var sColor = "" // Цвет заголовка окна роли
    var sPerfWindow = "" // Текущее окно настроек
    private var sPassword = "" // Пароль
    var bExpanded = false // Признак развернутых полей
    var aViews = arrayOfNulls<View>(2) // Массив видов для сохранения развернутых элементов
    var client: HttpClient? = null // Объект для вызова HTTP-методов
    private var iIDUser = 0 // Идентификатор пользователя
    var aAttrValues = mutableMapOf<String,String>() // Массив значений атрибутов
    var tAsync: Deferred<Unit> = GlobalScope.async(start = CoroutineStart.LAZY) {  } // Экземпляр второго потока
    var bStop = false // Признак отановки второго потока
    var jaSaveData = JSONArray() // Данные для сохранения

    // Создание лейаута
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prefs)
        sLogin = intent.getStringExtra("login")!!
        sMode = intent.getStringExtra("mode")!!
        sColor = intent.getStringExtra("color")!!
        sRole = intent.getStringExtra("role")!!
        client = HttpClient {
            defaultRequest {
                headers.append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
            }
        }

        // Установим цвет ActionBar в зависимости от роли
        if (sColor.length > 6) {
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor(sColor)))
        }
        supportActionBar!!.title = sMode

        if (sMode == "Профиль"){
            GlobalScope.async(Dispatchers.Main) {
                fsetProfile()
            }
        }
        else{
            fsetPerfs()
        }
    }

    override fun onBackPressed(){
        var iGoodCardIndex = -1
        for (i in 0 until clPrefs.childCount){
            if (clPrefs.getChildAt(i) is ScrollView){
                if (clPrefs.getChildAt(i).tag == "GoodCard"){
                    iGoodCardIndex = i
                }
            }
        }
        if (iGoodCardIndex >= 0) {
            llMenu.visibility = View.VISIBLE
            clPrefs.removeViewAt(iGoodCardIndex)
            return
        }
        if (svCategory.visibility == View.VISIBLE){
            svCategory.visibility = View.GONE
            return
        }
        super.onBackPressed()
    }

    // Сохранение настроек текущего окна
    fun onTvOKClick(view: View){
        svPerMenu.visibility = View.GONE
        llEdit.visibility = View.GONE
        ivPaste.visibility = View.GONE
        when (sMode){
            "Профиль" -> {
                GlobalScope.async(Dispatchers.Main) {
                    fCollapsePass()
                    val iResult = fSaveProfile()
                    if (iResult == 0){
                        Toast.makeText(this@Prefs, "Настройки успешно сохранены", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> when (sPerfWindow){
                "Атрибуты" -> {
                    GlobalScope.async(Dispatchers.Main) {
                        fCollapseAttr()
                        val iResult = fSaveAttr()
                        if (iResult == 0){
                            Toast.makeText(this@Prefs, "Настройки успешно сохранены", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                "ОбъектыАтрибуты" -> {
                    GlobalScope.async(Dispatchers.Main) {
                        val iResult = fSaveObjectsAttrs()
                        if (iResult == 0){
                            Toast.makeText(this@Prefs, "Настройки успешно сохранены", Toast.LENGTH_SHORT).show()
                        }
                        fAttrObject(tvPerfAttr.tag.toString())
                    }
                }
                "Категории" -> {
                    GlobalScope.async(Dispatchers.Main) {
                        val iResult = fSaveCategory()
                        if (iResult == 0){
                            Toast.makeText(this@Prefs, "Настройки успешно сохранены", Toast.LENGTH_SHORT).show()
                        }
                        fPrefCategory(tvPerfCategory.tag.toString())
                    }
                }
            }
        }
        view.visibility = View.INVISIBLE
    }

    // Отображение настроек пользователя
    private suspend fun fsetProfile(){
        sPerfWindow = "Профиль"
        val call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_user)){
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData {
                append("exec", "getprofile")
                append("login", sLogin)
            })
        }
        val sRequest = call.response.readText()
        val aJSONArray = JSONArray(sRequest)
        aAttrValues.clear()
        sPassword = aJSONArray.getJSONObject(0).getString("sPass").encrypt(getString(R.string.key_id))
        iIDUser = aJSONArray.getJSONObject(0).getInt("iID")
        faddPerfView("Логин", sLogin, "String","")
        faddPerfView("Пароль", sPassword, "Password","")
        for (i in 0 until aJSONArray.length()){
            var sValue = when (aJSONArray.getJSONObject(i).getString("sType")) {
                "Число" -> aJSONArray.getJSONObject(i).getString("fValue")
                "Дата" -> aJSONArray.getJSONObject(i).getString("dValue").convertDate()
                "Время" -> aJSONArray.getJSONObject(i).getString("dValue")
                else -> aJSONArray.getJSONObject(i).getString("sValue")
            }
            sValue = if (sValue == "null") "" else sValue
            aAttrValues.put(aJSONArray.getJSONObject(i).getString("sName"), sValue)
            aAttrValues.put(aJSONArray.getJSONObject(i).getString("sName"), sValue)
            faddPerfView(aJSONArray.getJSONObject(i).getString("sName"), sValue, aJSONArray.getJSONObject(i).getString("sType"), aJSONArray.getJSONObject(i).getString("sValues"))
        }
        val spSpace = Space(this)
        spSpace.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200)
        llInfo.addView(spSpace)
    }

    // Добавление строки настройки в окно
    private fun faddPerfView(sName: String, sValue: String, sType: String, sValues: String){
        val llPerf = LinearLayout(this)
        llPerf.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        llPerf.orientation = LinearLayout.HORIZONTAL
        val flPerfName = FrameLayout(this)
        flPerfName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.6f)
        (flPerfName.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,5,0)
        val tvPerfName = TextView(this)
        tvPerfName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        tvPerfName.gravity = Gravity.START + Gravity.CENTER_VERTICAL
        tvPerfName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        tvPerfName.text = sName
        val flPerfValue = FrameLayout(this)
        flPerfValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.4f)
        (flPerfValue.layoutParams as LinearLayout.LayoutParams).setMargins(5,0,0,0)
        val tvPerfValue: View
        when (sType){
            "Password" -> {
                tvPerfValue = Button(this)
                tvPerfValue.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
                tvPerfValue.gravity = Gravity.CENTER
                tvPerfValue.text = "Изменить"
                llPerf.tag = "Password"
                val iPassIndex = llInfo.childCount
                tvPerfValue.setOnClickListener {
                    bExpanded = true
                    aViews[0] = (llInfo.getChildAt(iPassIndex) as LinearLayout).getChildAt(0)
                    aViews[1] = (llInfo.getChildAt(iPassIndex) as LinearLayout).getChildAt(1)
                    // Старый пароль
                    (llInfo.getChildAt(iPassIndex) as LinearLayout).removeAllViews()
                    val flOldPassName = FrameLayout(this)
                    flOldPassName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.6f)
                    (flOldPassName.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,5,0)
                    val tvOldPassName = TextView(this)
                    tvOldPassName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                    tvOldPassName.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                    tvOldPassName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    tvOldPassName.text = "Текущий пароль"
                    val flOldPassValue = FrameLayout(this)
                    flOldPassValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.4f)
                    (flOldPassValue.layoutParams as LinearLayout.LayoutParams).setMargins(5,0,0,0)
                    val tvOldPassValue = EditText(this)
                    tvOldPassValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                    tvOldPassValue.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                    tvOldPassValue.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    tvOldPassValue.transformationMethod = PasswordTransformationMethod()
                    flOldPassName.addView(tvOldPassName)
                    flOldPassValue.addView(tvOldPassValue)
                    (llInfo.getChildAt(iPassIndex) as LinearLayout).addView(flOldPassName)
                    (llInfo.getChildAt(iPassIndex) as LinearLayout).addView(flOldPassValue)
                    tvOldPassValue.requestFocus()
                    // Новый пароль
                    val llNewPass = LinearLayout(this)
                    llNewPass.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    llNewPass.orientation = LinearLayout.HORIZONTAL
                    val flNewPassName = FrameLayout(this)
                    flNewPassName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.6f)
                    (flNewPassName.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,5,0)
                    val tvNewPassName = TextView(this)
                    tvNewPassName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                    tvNewPassName.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                    tvNewPassName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    tvNewPassName.text = "Новый пароль"
                    val flNewPassValue = FrameLayout(this)
                    flNewPassValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.4f)
                    (flNewPassValue.layoutParams as LinearLayout.LayoutParams).setMargins(5,0,0,0)
                    val tvNewPassValue = EditText(this)
                    tvNewPassValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                    tvNewPassValue.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                    tvNewPassValue.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    tvNewPassValue.transformationMethod = PasswordTransformationMethod()
                    flNewPassName.addView(tvNewPassName)
                    flNewPassValue.addView(tvNewPassValue)
                    llNewPass.addView(flNewPassName)
                    llNewPass.addView(flNewPassValue)
                    llInfo.addView(llNewPass,iPassIndex + 1)
                    // Новый пароль еще раз
                    val llAgnPass = LinearLayout(this)
                    llAgnPass.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    llAgnPass.orientation = LinearLayout.HORIZONTAL
                    val flAgnPassName = FrameLayout(this)
                    flAgnPassName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.6f)
                    (flAgnPassName.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,5,0)
                    val tvAgnPassName = TextView(this)
                    tvAgnPassName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                    tvAgnPassName.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                    tvAgnPassName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    tvAgnPassName.text = "Повторите пароль"
                    val flAgnPassValue = FrameLayout(this)
                    flAgnPassValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.4f)
                    (flAgnPassValue.layoutParams as LinearLayout.LayoutParams).setMargins(5,0,0,0)
                    val tvAgnPassValue = EditText(this)
                    tvAgnPassValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                    tvAgnPassValue.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                    tvAgnPassValue.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    tvAgnPassValue.transformationMethod = PasswordTransformationMethod()
                    flAgnPassName.addView(tvAgnPassName)
                    flAgnPassValue.addView(tvAgnPassValue)
                    llAgnPass.addView(flAgnPassName)
                    llAgnPass.addView(flAgnPassValue)
                    llInfo.addView(llAgnPass,iPassIndex + 2)
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
                        fCollapsePass()
                    }
                    iwOK.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    iwOK.setImageResource(R.drawable.done)
                    iwOK.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    iwOK.setOnClickListener {
                        val sOldPass = (((llInfo.getChildAt(iPassIndex) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as EditText).text.toString()
                        val sNewPass = (((llInfo.getChildAt(iPassIndex + 1) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as EditText).text.toString()
                        val sAgnPass = (((llInfo.getChildAt(iPassIndex + 2) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as EditText).text.toString()
                        if (sOldPass != sValue){
                            Toast.makeText(this, "Неверно введен текущий пароль", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        if (sNewPass != sAgnPass){
                            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        if (sNewPass.isEmpty()){
                            Toast.makeText(this, "Пароль не может быть пустым", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        sPassword = sNewPass
                        fCollapsePass()
                        tvOK.visibility = View.VISIBLE
                    }
                    llBut.addView(iwCancel)
                    llBut.addView(iwOK)
                    llInfo.addView(llBut,iPassIndex + 3)
                }
            }
            "Перечисление" ->{
                tvPerfValue = RadioGroup(this)
                tvPerfValue.orientation = RadioGroup.VERTICAL
                tvPerfValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                tvPerfValue.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                var sList = sValues
                while (sList.isNotEmpty()){
                    val rbList = RadioButton(this)
                    rbList.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    val sNext = sList.substringBefore('#',sList)
                    sList = sList.substringAfter('#',"")
                    rbList.text = sNext
                    rbList.setOnCheckedChangeListener { _, _ ->
                        fCollapsePass()
                        tvOK.visibility = View.VISIBLE
                    }
                    tvPerfValue.addView(rbList)
                    if (sValue == sNext){
                        tvPerfValue.check(rbList.id)
                    }
                }
            }
            else -> {
                tvPerfValue = EditText(this)
                tvPerfValue.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                tvPerfValue.gravity = Gravity.START + Gravity.CENTER_VERTICAL
                tvPerfValue.setText(sValue)
                tvPerfValue.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus){
                        fCollapsePass()
                    }
                }
                tvPerfValue.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.toString().trim().isNotEmpty()){
                            tvOK.visibility = View.VISIBLE
                        }
                    }
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    }
                    override fun afterTextChanged(s: Editable) {
                    }
                })
                when (sType){ // Время пока не трогаю, нет атрибутов. При наличии сделат подобно дате
                    "Дата" -> {
                        tvPerfValue.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL + InputType.TYPE_CLASS_NUMBER
                        DateInputMask(tvPerfValue).listen()
                    }
                    "Число" -> {
                        tvPerfValue.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL + InputType.TYPE_CLASS_NUMBER
                    }
                }
            }
        }
        flPerfName.addView(tvPerfName)
        flPerfValue.addView(tvPerfValue)
        llPerf.addView(flPerfName)
        llPerf.addView(flPerfValue)
        llInfo.addView(llPerf)
    }

    // Свернуть поля ввода пароля
    private fun fCollapsePass(){
        if (bExpanded) {
            bExpanded = false
            var iPassIndex = 0
            for (i in 0 until llInfo.childCount) {
                if (llInfo.getChildAt(i).tag == "Password") {
                    iPassIndex = i
                }
            }
            llInfo.removeViewAt(iPassIndex + 1)
            llInfo.removeViewAt(iPassIndex + 1)
            llInfo.removeViewAt(iPassIndex + 1)
            (llInfo.getChildAt(iPassIndex) as LinearLayout).removeAllViews()
            (llInfo.getChildAt(iPassIndex) as LinearLayout).addView(aViews[0])
            (llInfo.getChildAt(iPassIndex) as LinearLayout).addView(aViews[1])
        }
    }

    // Сохранение профиля
    private suspend fun fSaveProfile(): Int{
        val call = client!!.call(getString(R.string.server_uri) + getString(R.string.server_user)) {
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData {
                append("exec", "setprofile")
                append("id", iIDUser)
                append("login", (((llInfo.getChildAt(0) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as EditText).text.toString())
                append("pass", sPassword.encrypt(getString(R.string.key_id)))
                for (i in 2 until (llInfo.childCount - 1)) {
                    val sKey = (((llInfo.getChildAt(i) as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as TextView).text.toString()
                    val view = ((llInfo.getChildAt(i) as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0)
                    var sValue = ""
                    if (view is EditText) {
                        sValue = view.text.toString()
                    } else {
                        if ((view as RadioGroup).checkedRadioButtonId >= 0) {
                            for (j in 0 until view.childCount) {
                                if (view.getChildAt(j).id == view.checkedRadioButtonId) {
                                    sValue = (view.getChildAt(j) as RadioButton).text.toString()
                                }
                            }
                        }
                    }
                    aAttrValues.get(sKey)
                    if (sValue.isNotEmpty() and (aAttrValues.get(sKey) != sValue)) {
                        append(sKey.replace(' ', '_'), sValue)
                        aAttrValues.put(sKey.replace(' ', '_'), sValue)
                    }
                }
            })
        }
        val sRes = call.response.readText()
        return sRes.toInt()
    }

    // Инициализация окна настроек
    private fun fsetPerfs() {
        tvSettings.visibility = View.VISIBLE
        svPerMenu.visibility = View.VISIBLE

        // Скроем пункты меню, к которым нет доступа
        GlobalScope.async(Dispatchers.Main) {
            val call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_perf)){
                method = HttpMethod.Post
                body = MultiPartFormDataContent(formData {
                    append("exec", "getrolerights")
                    append("role", sRole)
                })
            }
            val sRequest = call.response.readText()
            val aJSONArray = JSONArray(sRequest)
            if (aJSONArray.length() > 0){
                if (aJSONArray.getJSONObject(0).getString("Attr") == "N"){
                    tvPerfAttr.visibility = View.GONE
                }
                if (aJSONArray.getJSONObject(0).getString("Category") == "N"){
                    tvPerfCategory.visibility = View.GONE
                }
                if (aJSONArray.getJSONObject(0).getString("Good") == "N"){
                    tvPerfGood.visibility = View.GONE
                }
                if (aJSONArray.getJSONObject(0).getString("Basket") == "N"){
                    tvPerfBasket.visibility = View.GONE
                }
                if (aJSONArray.getJSONObject(0).getString("Orders") == "N"){
                    tvPerfOrder.visibility = View.GONE
                }
                if (aJSONArray.getJSONObject(0).getString("State") == "N"){
                    tvPerfState.visibility = View.GONE
                }
                if (aJSONArray.getJSONObject(0).getString("Actions") == "N"){
                    tvPerfActions.visibility = View.GONE
                }
                if (aJSONArray.getJSONObject(0).getString("User") == "N"){
                    tvPerfUser.visibility = View.GONE
                }
                if (aJSONArray.getJSONObject(0).getString("Role") == "N"){
                    tvPerfRole.visibility = View.GONE
                }
                tvPerfAttr.tag = aJSONArray.getJSONObject(0).getString("Attr")
                tvPerfCategory.tag = aJSONArray.getJSONObject(0).getString("Category")
                tvPerfGood.tag = aJSONArray.getJSONObject(0).getString("Good")
                tvPerfBasket.tag = aJSONArray.getJSONObject(0).getString("Basket")
                tvPerfOrder.tag = aJSONArray.getJSONObject(0).getString("Orders")
                tvPerfState.tag = aJSONArray.getJSONObject(0).getString("State")
                tvPerfActions.tag = aJSONArray.getJSONObject(0).getString("Actions")
                tvPerfUser.tag = aJSONArray.getJSONObject(0).getString("User")
                tvPerfRole.tag = aJSONArray.getJSONObject(0).getString("Role")
            }
            else {
                svPerMenu.visibility = View.GONE
            }
        }
    }

    // Показать меню настроек
    fun fontvSettingsClick(view: View){
        svPerMenu.visibility = View.VISIBLE
        llEdit.visibility = View.GONE
        svCategory.visibility = View.GONE
    }

    // Реакция на выбор пункта из меню настроек
    fun fontvPerfClick(view: View){
        svPerMenu.visibility = View.GONE
        when (view.id) {
            tvPerfAttr.id ->{
                if (sPerfWindow != getString(R.string.PerfAttr)){
                    sPerfWindow = getString(R.string.PerfAttr)
                    ivPaste.visibility = View.INVISIBLE
                    llMenu.visibility = View.GONE
                    fPrefAttr(view.tag.toString())
                }
            }
            tvPerfCategory.id ->{
                if (sPerfWindow != getString(R.string.PerfCategory)){
                    sPerfWindow = getString(R.string.PerfCategory)
                    ivPaste.visibility = View.INVISIBLE
                    ivCategory.visibility = View.GONE
                    ivListoff.visibility = View.GONE
                    ivCopy.visibility = View.VISIBLE
                    ivCut.visibility = View.VISIBLE
                    llMenu.visibility = View.GONE
                    fPrefCategory(view.tag.toString())
                }
            }
            tvPerfGood.id ->{
                if (sPerfWindow != "Товары"){
                    sPerfWindow = "Товары"
                    ivPaste.visibility = View.INVISIBLE
                    ivCategory.visibility = View.VISIBLE
                    ivListoff.visibility = View.VISIBLE
                    ivCopy.visibility = View.GONE
                    ivCut.visibility = View.GONE
                    llMenu.visibility = View.VISIBLE
                    fPrefGood(view.tag.toString())
                }
            }
        }
    }
}