package com.product.cms

import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

// Функция кодирования строки по XOR-алгоритму и паролю
fun String.encrypt(sPass: String): String {
    val baXOR = StringBuilder()
    for (i in 0 until this.length){
        baXOR.append((this[i].toInt() xor sPass[i % sPass.length].toInt()).toChar())
    }
    return baXOR.toString()
}

// Переводит DP в Int
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

// Конвертация даты в читаемый формат
fun String.convertDate(): String {
    if (this.length > 9) {
        return this[8].toString() + this[9].toString() + "." + this[5].toString() + this[6].toString() + "." + this[0].toString() + this[1].toString() + this[2].toString() + this[3].toString()
    }
    else{
        return this
    }
}

// Маска для ввода даты
class DateInputMask(val etInput : EditText) {

    fun listen() {
        etInput.addTextChangedListener(mDateEntryWatcher)
    }

    private val mDateEntryWatcher = object : TextWatcher {

        var bEdited = false
        val sDividerCharacter = "."

        override fun onTextChanged(s: CharSequence, iStart: Int, iBefore: Int, iCount: Int) {
            if (bEdited) {
                bEdited = false
                return
            }

            var sWorking = getEditText()

            sWorking = manageDateDivider(sWorking, 2, iStart, iBefore)
            sWorking = manageDateDivider(sWorking, 5, iStart, iBefore)

            bEdited = true
            etInput.setText(sWorking)
            etInput.setSelection(etInput.text.length)
        }

        private fun manageDateDivider(sWorking: String, iPosition : Int, iStart: Int, iBefore: Int) : String{
            if (sWorking.length == iPosition) {
                return if (iBefore <= iPosition && iStart < iPosition)
                    sWorking + sDividerCharacter
                else
                    sWorking.dropLast(1)
            }
            return sWorking
        }

        private fun getEditText() : String {
            return if (etInput.text.length >= 10)
                etInput.text.toString().substring(0,10)
            else
                etInput.text.toString()
        }

        override fun afterTextChanged(s: Editable) {}
        override fun beforeTextChanged(s: CharSequence, iStart: Int, iCount: Int, iAfter: Int) {}
    }
}