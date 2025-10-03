package com.example.mnotepad.helpers

import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText
import androidx.core.graphics.toColorInt

object TextEditorHelper {
    var isBold = false
    var isItalic = false
    var isUnderline = false
    var activeColor: Int? = null
    lateinit var textWatcher: TextWatcher
    private fun applySpan(editText: EditText, span: Any) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start < end) {
            val spannable: Editable = editText.text
            spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun toggleBold(editText: EditText): Boolean {
        isBold = !isBold
        if (editText.hasSelection()) {
            applySpan(editText, StyleSpan(BOLD))
        }
        return isBold
    }

    fun toggleItalic(editText: EditText): Boolean {
        isItalic = !isItalic
        if (editText.hasSelection()) {
            applySpan(editText, StyleSpan(ITALIC))
        }
        return isItalic
    }

    fun toggleUnderline(editText: EditText): Boolean {
        isUnderline = !isUnderline
        if (editText.hasSelection()) {
            applySpan(editText, UnderlineSpan())
        }
        return isUnderline
    }

    fun toggleColor(editText: EditText, colorHex: String): Boolean {
        val colorInt = colorHex.toColorInt()

        activeColor = if (colorInt != activeColor) colorInt else null

        if (editText.hasSelection() && activeColor != null) {
            applySpan(editText, ForegroundColorSpan(colorInt))
        }

        return activeColor != null
    }

    fun reset() {
        isBold = false
        isItalic = false
        isUnderline = false
        activeColor = null
    }

    fun attachTo(
        editText: EditText
    ) {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Nếu count > 0 nghĩa là đang thêm text, không phải xóa
                if (count > 0 && s is Spannable) {
                    val end = start + count
                    val spannable = s

                    if (isBold) {
                        spannable.setSpan(
                            StyleSpan(BOLD),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    if (isItalic) {
                        spannable.setSpan(
                            StyleSpan(ITALIC),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    if (isUnderline) {
                        spannable.setSpan(
                            UnderlineSpan(),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    activeColor?.let {
                        spannable.setSpan(
                            ForegroundColorSpan(it),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        editText.addTextChangedListener(textWatcher)
    }

    fun detachTextWatcher(editText: EditText) {
        editText.removeTextChangedListener(textWatcher)
    }
}
