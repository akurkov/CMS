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
        val spref = getPreferences(Context.MODE_PRIVATE)
        rbJSON.isChecked = spref.getBoolean("rbJSON",false)
        rbHTML.isChecked = spref.getBoolean("rbHTML",true)
    }

    override fun onDestroy() {
        super.onDestroy()
        val spref = getPreferences(Context.MODE_PRIVATE)
        val ed = spref.edit()
        ed.putBoolean("rbJSON",rbJSON.isChecked)
        ed.putBoolean("rbHTML",rbHTML.isChecked)
        ed.commit()
    }

    fun onbFindClick(view: View){
        if (rbHTML.isChecked) {
            val intent = Intent(this@MainActivity, browser::class.java)
            intent.putExtra("uri","http://l90268zm.beget.tech/sql.php?query="+tFind.text.toString())
            startActivity(intent)
        }
        else{
            val intent = Intent(this@MainActivity, JSON::class.java)
            intent.putExtra("uri","http://l90268zm.beget.tech/json.php?query="+tFind.text.toString())
            startActivity(intent)
        }
    }
}

