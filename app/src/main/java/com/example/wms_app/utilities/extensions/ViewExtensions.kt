package com.example.wms_app.utilities.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


fun EditText.isPositionEditTextValid(errorString: String, charNumber: Int): Boolean {
    this.onChange { this.error = null }
    val textToCheck = this.text.toString().trim()
    return if (textToCheck.isBlank() || textToCheck.length != charNumber) {
        this.error = errorString
        false
    } else {
        true
    }
}

fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}