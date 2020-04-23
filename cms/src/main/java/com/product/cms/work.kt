package com.product.cms

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_work.*

class work : AppCompatActivity() {

    var sLogin = "" // Имя пользователя
    var sRole = "" // Роль пользователя
    var sColor = "" // Цвет заголовка окна роли
    var bExpanded = true // Признак развернутого бокового меню

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)
        sLogin = intent.getStringExtra("login")
        sRole = intent.getStringExtra("role")
        sColor = "#"+intent.getStringExtra("color")
        if (sColor.length > 6) {
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor(sColor)))
        }
        val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
        bExpanded = spref.getBoolean("Expanded",true)
        fCheckExpand()
    }

    override fun onPause() {
        super.onPause()
        val spref = getSharedPreferences("common", Context.MODE_PRIVATE)
        val ed = spref.edit()
        ed.putBoolean("Expanded", bExpanded)
        ed.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        var sIcon = "letter_" +  sLogin[0].toString().decapitalize()
        var iIDIcon = resources.getIdentifier(sIcon,"drawable",packageName)
        if (iIDIcon === 0) {
            menu.getItem(0).setIcon(R.drawable.letter_a)
        }
        else {
            menu.getItem(0).setIcon(getDrawable(iIDIcon))
        }
        return super.onCreateOptionsMenu(menu)
    }

    // При нажатии кнопки Назад свернем приложение
    override fun onBackPressed(){
        finishAffinity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.imExit -> {
                val intent = Intent(this@work, login::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Действие пунктов бокового меню
    fun onllMenuClick(view: View){
        when (view.id){
            R.id.tvExpand -> {
                bExpanded = if (bExpanded) false else true
                fCheckExpand()
            }
        }
    }

    // Отображение меню в зависимости от признака развертывания
    fun fCheckExpand(){
        if (bExpanded){
            llMenu.layoutParams.width = 120.toPx()
            llMenu.requestLayout()
            tvActions.setText(R.string.flMenu_Actions)
            tvBasket.setText(R.string.flMenu_Basket)
            tvGoods.setText(R.string.flMenu_Goods)
            tvOrders.setText(R.string.flMenu_Orders)
        }
        else {
            llMenu.layoutParams.width = 30.toPx()
            llMenu.requestLayout()
            tvActions.setText("")
            tvBasket.setText("")
            tvGoods.setText("")
            tvOrders.setText("")
        }
    }
}
