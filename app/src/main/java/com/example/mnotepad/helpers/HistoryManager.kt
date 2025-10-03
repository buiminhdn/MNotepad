package com.example.mnotepad.helpers

import android.text.Spannable
import android.widget.EditText
import java.util.Stack

class HistoryManager {
//    private val undoStack = Stack<String>()
//    private val redoStack = Stack<String>()
//    private var lastSaved = ""
//    private var firstSave = false
//
//    fun save(text: String) {
//        if (!firstSave) {
//            // lần đầu thì chỉ gán lastSaved thôi, không push vào undoStack
//            lastSaved = text
//            firstSave = true
//            return
//        }
//
//        if (text != lastSaved) {
//            undoStack.push(lastSaved)
//            lastSaved = text
//            // Mỗi lần gõ thì không cho redo nữa
//            redoStack.clear()
//        }
//    }
//
//    fun undo(): String {
//        return if (undoStack.isNotEmpty()) {
//            val prev = undoStack.pop()
//            redoStack.push(lastSaved) // lưu trạng thái hiện tại vào redo
//            lastSaved = prev
//            prev
//        } else {
//            // không cho undo xuống dưới text ban đầu
//            lastSaved
//        }
//    }
//
//
//    fun redo(): String {
//        return if (redoStack.isNotEmpty()) {
//            val next = redoStack.pop()
//            undoStack.push(lastSaved) // lưu trạng thái hiện tại vào undo
//            lastSaved = next
//            next
//        } else {
//            lastSaved
//        }
//    }

    private val undoStack = Stack<Spannable>()
    private val redoStack = Stack<Spannable>()
    private lateinit var lastSaved: Spannable
    private var firstSave = false

    fun isUndoEmpty(): Boolean {
        return undoStack.isEmpty()
    }

    fun isRedoEmpty(): Boolean {
        return redoStack.isEmpty()
    }

    fun save(text: Spannable) {
        if (!firstSave) {
            // lần đầu thì chỉ gán lastSaved thôi, không push vào undoStack
            lastSaved = text
            firstSave = true
            return
        }

        if (text.toString() != lastSaved.toString()) {
            undoStack.push(lastSaved)
            lastSaved = text
            // Mỗi lần gõ thì không cho redo nữa
            redoStack.clear()
        }
    }

    fun undo(): Spannable {
        return if (undoStack.isNotEmpty()) {
            val prev = undoStack.pop()
            redoStack.push(lastSaved) // lưu trạng thái hiện tại vào redo
            lastSaved = prev
            prev
        } else {
            // không cho undo xuống dưới text ban đầu
            lastSaved
        }
    }


    fun redo(): Spannable {
        return if (redoStack.isNotEmpty()) {
            val next = redoStack.pop()
            undoStack.push(lastSaved) // lưu trạng thái hiện tại vào undo
            lastSaved = next
            next
        } else {
            lastSaved
        }
    }
}

fun EditText.applyHistory(text: Spannable) {
    setText(text)
    setSelection(text.length)
}
//fun EditText.applyHistory(text: String) {
//    setText(text)
//    setSelection(text.length)
//}
