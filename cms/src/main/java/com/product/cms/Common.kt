package com.product.cms

import android.content.res.Resources

// Функция кодирования строки по XOR-алгоритму и паролю
fun String.encrypt(sPass: String): String {
    var baXOR = StringBuilder()
    for (i in 0..(this.length-1)){
        baXOR.append((this[i].toInt() xor sPass[i % sPass.length].toInt()).toChar())
    }
    return baXOR.toString()
}

// Переводит DP в Int
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

// Конвертация даты в читаемый формат
fun String.convertDate(): String {
    var sDate = this[8].toString() + this[9].toString() + "." + this[5].toString() + this[6].toString() + "." + this[0].toString() + this[1].toString() + this[2].toString() + this[3].toString()
    return sDate
}
