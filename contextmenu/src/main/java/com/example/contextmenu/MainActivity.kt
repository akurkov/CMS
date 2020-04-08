package com.example.contextmenu

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_color -> {
            if (tvColor.currentTextColor == Color.BLUE) {
                tvColor.setTextColor(Color.RED)
            }
            else{
                tvColor.setTextColor(Color.BLUE)
            }
            true
        }
        R.id.action_size -> {
            if (tvSize.getTextSize().compareTo(40f) == 0) {
                tvSize.setTextSize(TypedValue.COMPLEX_UNIT_PX,26f)
            }
            else{
                tvSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
