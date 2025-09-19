package com.example.mnotepad.helpers

import android.widget.EditText
import java.util.Stack

class HistoryManager {
    private val undoStack = Stack<String>()
    private val redoStack = Stack<String>()
    private var lastSaved = ""

    fun save(text: String) {
        if (text != lastSaved) {
            undoStack.push(text)
            lastSaved = text
        }
    }

    fun undo(): String = if (undoStack.size > 1) {
        redoStack.push(undoStack.pop())
        lastSaved = undoStack.peek()
        undoStack.peek()
    } else if (undoStack.isNotEmpty()) {
        redoStack.push(undoStack.pop())
        lastSaved = ""
        ""
    } else {
        ""
    }

    fun redo(): String = if (redoStack.isNotEmpty()) {
        val redoText = redoStack.pop()
        undoStack.push(redoText)
        lastSaved = redoText
        redoText
    } else lastSaved
}

fun EditText.applyHistory(text: String) {
    setText(text)
    setSelection(text.length)
}
