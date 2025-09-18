package com.example.mnotepad.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import com.example.mnotepad.R
import com.example.mnotepad.databinding.ActivityNoteDetailBinding
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.exportToTxtFile
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.CategoryViewModel
import com.example.mnotepad.viewmodels.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Stack


class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var optionsMenu: Menu
    private val noteViewModel: NoteViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private var listCategories: List<Category> = emptyList()
    private var isEditedAction = false
    private var curNoteItem: Note? = null
    private lateinit var checkedItems: BooleanArray
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

        categoryViewModel.categories.observe(this) { categories ->
            listCategories = categories
        }
    }

    private fun handleCategorize() {
        if (listCategories.isEmpty()) {
            showToast("Please add at least 1 category first", this)
            return
        }
        checkedItems = BooleanArray(listCategories.size)
        val array = arrayOfNulls<String>(listCategories.size)
        var index = 0
        for (value in listCategories) {
            array[index] = value.name
            index++
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Categories")
            .setMultiChoiceItems(array, checkedItems) { dialog, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Confirm") { dialog, which ->
                noteViewModel.updateNote(
                    Note(
                        id = curNoteItem!!.id,
                        title = curNoteItem!!.title,
                        content = curNoteItem!!.content
                    )
                )
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
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


    // Request code for creating a PDF document.

    private fun handleImportFile() {
        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.setType("text/plain")
        chooseFile = Intent.createChooser(chooseFile, "Choose a file")
        startActivityForResult(chooseFile, 50)
    }

    private fun handleExportFile() {
        val noteTitle = binding.edtTitle.text.toString()
        val noteContent = binding.edtContent.text.toString()

        if (noteTitle.isEmpty() && noteContent.isEmpty()) {
            showToast("Type Something", this)
            return
        }

        exportToTxtFile(applicationContext, noteTitle, noteContent)
    }

    private fun switchToReadOnly() {
        optionsMenu.findItem(R.id.navSave).isVisible = false
        optionsMenu.findItem(R.id.navUndo).isVisible = false
        optionsMenu.findItem(R.id.navEdit).isVisible = true
        binding.edtTitle.inputType = InputType.TYPE_NULL
        binding.edtContent.inputType = InputType.TYPE_NULL
    }

    private fun switchToAllowEdit() {
        switchToAllowEdit()
        optionsMenu.findItem(R.id.navSave).isVisible = true
        optionsMenu.findItem(R.id.navUndo).isVisible = true
        optionsMenu.findItem(R.id.navEdit).isVisible = false
        binding.edtTitle.inputType = InputType.TYPE_CLASS_TEXT
        binding.edtContent.inputType = InputType.TYPE_CLASS_TEXT
    }

    private fun highlightText(s: String) {
        val spannableString = SpannableString(binding.edtContent.getText())
//        val backgroundColorSpan =
//            spannableString.getSpans(
//                0,
//                spannableString.length,
//                BackgroundColorSpan::class.java
//            )
//        for (bgSpan in backgroundColorSpan) {
//            spannableString.removeSpan(bgSpan)
//        }
        var indexOfKeyWord = spannableString.toString().indexOf(s)
        while (indexOfKeyWord > 0) {
            spannableString.setSpan(
                BackgroundColorSpan(Color.RED), indexOfKeyWord,
                indexOfKeyWord + s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            indexOfKeyWord = spannableString.toString().indexOf(s, indexOfKeyWord + s.length)
        }
        binding.edtContent.setText(spannableString)
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_detail_menu, menu)
        if (menu != null) {
            optionsMenu = menu
            menu.findItem(R.id.navEdit).isVisible = false
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

            R.id.navCategorize -> {
                handleCategorize()
                true
            }

            R.id.navImport -> {
                handleImportFile()
                true
            }

            R.id.navExport -> {
                handleExportFile()
                true
            }

            R.id.navReadOnly -> {
                switchToReadOnly()
                true
            }

            R.id.navEdit -> {
                switchToAllowEdit()
                true
            }

            R.id.navShare -> {
                val intent= Intent()
                intent.action=Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT,binding.edtContent.text.toString())
                intent.type="text/plain"
                startActivity(Intent.createChooser(intent,"Share To:"))
                true
            }

            R.id.navSearchDetail -> {
                val edtSearch = binding.edtSearch
                edtSearch.visibility = View.VISIBLE;
                edtSearch.requestFocus();

                edtSearch.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        highlightText(s.toString());
                    }

                })
                true
            }

            R.id.navColorize -> {
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

