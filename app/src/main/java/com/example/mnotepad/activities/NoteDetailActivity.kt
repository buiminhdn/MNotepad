package com.example.mnotepad.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import com.example.mnotepad.R
import com.example.mnotepad.databinding.ActivityNoteDetailBinding
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.NoteViewModel
import java.util.Stack

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var optionsMenu: Menu
    private val noteViewModel: NoteViewModel by viewModels()
    private var isEditedAction = false
    private var curNoteItem: Note? = null

    private val undoStack = Stack<String>()
    private val redoStack = Stack<String>()
    private var lastSavedText = ""
    private val handler = Handler(Looper.getMainLooper())
    private val saveRunnable = object : Runnable {
        override fun run() {
            val currentText = binding.edtContent.text.toString()
            if (currentText != lastSavedText) {
                undoStack.push(currentText)
                lastSavedText = currentText
            }
            handler.postDelayed(this, 1000)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initToolbar()
        getNoteDataIfUpdate()
        handleContentChange()

        handler.post(saveRunnable)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getNoteDataIfUpdate() {
        isEditedAction = intent.getBooleanExtra(IS_EDITED_ACTION, false)
        if (isEditedAction) getNoteDataBundle()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getNoteDataBundle() {
        curNoteItem = intent.getParcelableExtra(NOTE_DETAIL_OBJECT, Note::class.java)!!
        binding.edtTitle.setText(curNoteItem?.title)
        binding.edtContent.setText(curNoteItem?.content)
    }


    private fun handleContentChange() {
        binding.edtContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                optionsMenu[1].isEnabled = s.toString().isNotEmpty()
            }
        })
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun upsertNote() {
        val noteTitle = binding.edtTitle.text.toString()
        val noteContent = binding.edtContent.text.toString()

        if (noteTitle.isEmpty() && noteContent.isEmpty()) {
            showToast("Type Something", this)
            return
        }

        // Update & Add
        if (curNoteItem != null) {
            noteViewModel.updateNote(
                Note(
                    id = curNoteItem!!.id,
                    title = noteTitle,
                    content = noteContent
                )
            )
            showToast("$noteTitle Updated", this)
        } else {
            noteViewModel.addNote(Note(id = 0, title = noteTitle, content = noteContent))
            showToast("$noteTitle Added", this)
        }

        finish()
    }

    private fun undoNote() {
        if (undoStack.size > 1) {
            redoStack.push(undoStack.pop())
            val prev = undoStack.peek()
            prev?.let {
                binding.edtContent.setText(it)
                binding.edtContent.setSelection(it.length)
                lastSavedText = it
            }
        } else {
            redoStack.push(undoStack.pop())
            binding.edtContent.setText("")
            lastSavedText = ""
        }
    }

    private fun redoNote() {
        if (redoStack.isNotEmpty()) {
            val redoText = redoStack.pop()
            undoStack.push(redoText)
            redoText?.let {
                binding.edtContent.setText(it)
                binding.edtContent.setSelection(it.length)
                lastSavedText = it
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_detail_menu, menu)
        if (menu != null) {
            optionsMenu = menu
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navSave -> {
                upsertNote()
                true
            }

            R.id.navUndo -> {
                undoNote()
                true
            }

            R.id.navRedo -> {
                redoNote()
                true
            }

            R.id.navDelete -> {
                if (curNoteItem != null) {
                    noteViewModel.deleteNote(curNoteItem!!.id)
                }
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(saveRunnable) // cleanup
    }
}

