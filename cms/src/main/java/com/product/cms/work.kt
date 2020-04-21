package com.product.cms

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_work.*

class work : AppCompatActivity() {

    var sLogin = "" // Имя пользователя
    var sRole = "" // Роль пользователя
    var sColor = "" // Цвет заголовка окна роли

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)
        sLogin = intent.getStringExtra("login")
        sRole = intent.getStringExtra("role")
        sColor = "#"+intent.getStringExtra("color")
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor(sColor)))
        textView.setText(sLogin + " - " + sRole)
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
}
