package com.example.basicviews

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получение настроек из файла настроек
        val spref = getPreferences(Context.MODE_PRIVATE)
        rbJSON.isChecked = spref.getBoolean("rbJSON",false)
        rbHTML.isChecked = spref.getBoolean("rbHTML",true)
        tFind.setText(spref.getString("Find",""))
    }

    override fun onDestroy() {
        super.onDestroy()

        // Сохранение настроек в файл настроек
        val spref = getPreferences(Context.MODE_PRIVATE)
        val ed = spref.edit()
        ed.putBoolean("rbJSON",rbJSON.isChecked)
        ed.putBoolean("rbHTML",rbHTML.isChecked)
        ed.putString("Find",tFind.text.toString())
        ed.commit()
    }

    // Обработка нажатия кнопки поиска
    fun onbFindClick(view: View){
        if (rbHTML.isChecked) {
            // Для формата HTML отображается страница sql.php
            val intent = Intent(this@MainActivity, browser::class.java)
            intent.putExtra("uri","http://l90268zm.beget.tech/sql.php?query="+tFind.text.toString())
            intent.putExtra("texttype","HTML")
            startActivity(intent)
        }
        else{
            // Для формата JSON идет обращение к странице json.php
            val intent = Intent(this@MainActivity, browser::class.java)
            intent.putExtra("uri","http://l90268zm.beget.tech/json.php?query="+tFind.text.toString())
            intent.putExtra("texttype","JSON")
            startActivity(intent)
        }
    }
}

