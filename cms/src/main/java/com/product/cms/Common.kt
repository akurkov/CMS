package com.product.cms

import android.content.res.Resources

// Функция кодирования строки по XOR-алгоритму и паролю
fun String.encrypt(stPass: String): String {
    var baXOR = StringBuilder()
    for (i in 0..(this.length-1)){
        baXOR.append((this[i].toInt() xor stPass[i % stPass.length].toInt()).toChar())
    }
    return baXOR.toString()
}

// Переводит DP в Int
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()