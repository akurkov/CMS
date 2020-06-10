package com.product.cms

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
import java.util.*

open class Work : AppCompatActivity() {

    var sLogin = "" // Имя пользователя
    private var sRole = "" // Роль пользователя
    private var sColor = "" // Цвет заголовка окна роли
    private var bExpanded = true // Признак развернутого бокового меню
    var iIDCategory = 0 // Идентификатор текущей категории
    var sMode = "" // Режим отображения
    var bLoading = false // Признак загрузки данных
    var iPage = 0 // Номер текущей загрузки данных
    var bEOD = false // Признак достигнутого конца данных
    var bStop = false // Признак завершения параллельного потока
    var sSearchString = "" // Поисковая строка
    var aOrders: MutableList<MutableList<Int>> = ArrayList() // Связанные с заказами объекты: идентификатор заказа, признак раскрытия заказа, количество элементов в заказе
    var client: HttpClient? = null // Объект для вызова HTTP-методов

    // Создание лейаута
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)
        sLogin = intent.getStringExtra("login")!!
        sRole = intent.getStringExtra("role")!!
        sColor = "#" + intent.getStringExtra("color")!!
        client = HttpClient {
            defaultRequest {
                header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0")
            }
        }

        // Установим цвет ActionBar в зависимости от роли
        if (sColor.length > 6) {
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor(sColor)))
        }

        // Восстановим позицию бокового меню
        val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
        bExpanded = spref.getBoolean("Expanded",true)
        fCheckExpand()

        //При потере фокуса уберем строку поиска
        etSearch.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                flSearch.visibility = View.GONE
                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        // Реакция на окончание редактирования строки поиска
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE ) {
                // Выбран поиск
                etSearch.clearFocus()
                if (etSearch.text.toString() != ""){
                    sMode="Search"
                    sSearchString = etSearch.text.toString()
                    fGoodsSearch()
                }
                true
            }
            else{
                false
            }
        }

        // Подготовим подгрузку при скроллинге
        swInfo.viewTreeObserver.addOnScrollChangedListener {
            etSearch.clearFocus()
            val view = swInfo.getChildAt(swInfo.childCount - 1) as View
            val iDiff: Int = view.bottom - (swInfo.height + swInfo.scrollY)
            if (iDiff == 0) {
                fAddGoods()
            }
        }

        // Отобразим акции
        sMode = "Actions"
        fGoodActions()
    }

    // При остановке лейаута сохраним позицию бокового меню
    override fun onPause() {
        super.onPause()
        val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
        val ed = spref.edit()
        ed.putBoolean("Expanded", bExpanded)
        ed.apply()
    }

    // Изменение иконки меню в зависимости от первой буквы логина
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        val sIcon = "letter_" +  sLogin[0].toString().decapitalize()
        val iIDIcon = resources.getIdentifier(sIcon,"drawable",packageName)
        if (iIDIcon === 0) {
            menu.getItem(1).setIcon(R.drawable.letter_a)
        }
        else {
            menu.getItem(1).icon = getDrawable(iIDIcon)
        }

        // Скроем меню Настройки для покупателя
        GlobalScope.async(Dispatchers.Main) {
            val call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_perf)){
                method = HttpMethod.Post
                body = MultiPartFormDataContent(
                    formData {
                        append("exec", "getrolerights")
                        append("role", sRole)
                    }
                )
            }
            val sRequest = call.response.readText()
            val aJSONArray = JSONArray(sRequest)
            if (aJSONArray.length() > 0){
                menu.getItem(1).subMenu.getItem(1).isVisible = true
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    // При нажатии кнопки Назад свернем приложение
    override fun onBackPressed(){
        // Если отображается фото товара или карточка товара, просто закроем их
        if (flPhoto.visibility == View.VISIBLE){
            flPhoto.visibility = View.GONE
            return
        }
        if (flGoodCard.visibility == View.VISIBLE){
            flGoodCard.visibility = View.GONE
            return
        }

        // В режиме Товары возвращаемся по хлебным крошкам
        if (sMode == "Goods"){
            if (llBC.childCount > 2){
                llBC.removeViewAt(llBC.childCount - 1)
                llBC.removeViewAt(llBC.childCount - 1)
                val tvChild = llBC.getChildAt(llBC.childCount - 1) as TextView
                iIDCategory = tvChild.tag.toString().toInt()
                fGoodsGetList(tvChild.text.toString())
                return
            }
        }
        finishAffinity()
    }

    // Реакции на выбор пунктов меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        etSearch.clearFocus()
        when (item.itemId){
            R.id.imExit -> { // Нажата кнопка Выход, переходим к логину
                val intent = Intent(this@Work, Login::class.java)
                startActivity(intent)
            }
            R.id.mSearch ->{ // Нажата кнопка поиск
                flSearch.visibility = View.VISIBLE
                etSearch.setText("")
                etSearch.requestFocus()
            }
            R.id.imProfile -> { // Нажата кнопка Профиль, переходим к настройкам
                val intent = Intent(this@Work, Prefs::class.java)
                intent.putExtra("login", sLogin)
                intent.putExtra("color", sColor)
                intent.putExtra("role", sRole)
                intent.putExtra("mode", "Профиль")
                startActivity(intent)
            }
            R.id.imSettings -> {
                // Нажата кнопка Настройки, переходим к настройкам
                val intent = Intent(this@Work, Prefs::class.java)
                intent.putExtra("login", sLogin)
                intent.putExtra("color", sColor)
                intent.putExtra("role", sRole)
                intent.putExtra("mode", "Настройки")
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Действие пунктов бокового меню
    fun onllMenuClick(view: View){
        when (view.id){
            R.id.tvExpand -> { // Нажата кнопка раскрытия/сворачивания бокового меню
                bExpanded = !bExpanded
                fCheckExpand()
            }
            R.id.tvGoods -> { // Нажата кнопка Товары
                sMode = "Goods"
                fGoodsInit()
                fGoodsGetList("")
            }
            R.id.tvBasket -> { // Нажата кнопка Корзина
                sMode = "Basket"
                fBasketInit()
                GlobalScope.async(Dispatchers.Main) {
                    fBasketGetList()
                }
            }
            R.id.tvOrders -> { // Нажата кнопка Заказы
                sMode = "Orders"
                fOrderInit()
                GlobalScope.async(Dispatchers.Main) {
                    fOrderGetList()
                }
            }
            R.id.tvActions -> { // нажата кнопка Акции
                sMode = "Actions"
                fGoodActions()
            }
        }
    }

    // Отображение меню в зависимости от признака развертывания
    private fun fCheckExpand(){
        if (bExpanded){
            llMenu.layoutParams.width = 120.toPx()
            llMenu.requestLayout()
            tvActions.text = getString(R.string.flMenu_Actions)
            tvBasket.text = getString(R.string.flMenu_Basket)
            tvGoods.text = getString(R.string.flMenu_Goods)
            tvOrders.text = getString(R.string.flMenu_Orders)
        }
        else {
            llMenu.layoutParams.width = 30.toPx()
            llMenu.requestLayout()
            tvActions.text = ""
            tvBasket.text = ""
            tvGoods.text = ""
            tvOrders.text = ""
        }
    }

    // Обработчик кнопок в карточке товаров
    fun fOnCardClick(view: View){
        when (view.id){
            R.id.iwCardMinus ->{ // Уменьшить количество товара
                var sCountText = (((view.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString()
                if (sCountText != "0"){
                    sCountText = (sCountText.toInt() - 1).toString()
                    (((view.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text = sCountText
                }
            }
            R.id.iwCardPlus ->{ // Увеличить количество товара
                var sCountText = (((view.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString()
                sCountText = (sCountText.toInt() + 1).toString()
                (((view.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text = sCountText
            }
            R.id.iwCardShop ->{ // Положить в корзину
                val iCount = (((view.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).text.toString().toInt()
                val iIDGood = (((view.parent as LinearLayout).getChildAt(1) as FrameLayout).getChildAt(0) as TextView).tag.toString().toInt()
                if (iCount > 0){
                    GlobalScope.async(Dispatchers.Main) {
                        val call = client!!.call(getString(R.string.server_uri)+getString(R.string.server_basket)){
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
                            Toast.makeText(this@Work,"Товар добавлен в корзину",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

