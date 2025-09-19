package com.example.mnotepad.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import com.example.mnotepad.R
import com.example.mnotepad.databinding.ActivityNoteDetailBinding
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.FileHelper
import com.example.mnotepad.helpers.HistoryManager
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.applyHistory
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.CategoryViewModel
import com.example.mnotepad.viewmodels.NoteViewModel


class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var optionsMenu: Menu
    private val noteViewModel: NoteViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private var listCategories: List<Category> = emptyList()
    private var curNoteItem: Note? = null
    private lateinit var importLauncher: ActivityResultLauncher<Intent>
    private val history = HistoryManager()
    private val handler = Handler(Looper.getMainLooper())
    private val saveRunnable = object : Runnable {
        override fun run() {
            history.save(binding.edtContent.text.toString())
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
        initObservers()
        getNoteDataIfUpdate()
        initImportLauncher()

        handler.post(saveRunnable)
    }

    private fun initImportLauncher() {
        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    val content = FileHelper.readTextFromUri(this, uri)
                    val lines = content.lines()
                    val title = lines.firstOrNull() ?: ""
                    val body = lines.drop(1).joinToString("\n")

                    binding.edtTitle.setText(title)
                    binding.edtContent.setText(body)
                }
            }
        }
    }

    private fun initObservers() {
        categoryViewModel.categories.observe(this) {
            listCategories = it
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_detail_menu, menu)
        if (menu != null) {
            optionsMenu = menu
            menu.findItem(R.id.navEdit).isVisible = false
            handleContentChange()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.navSave -> {
            upsertNote(); true
        }

        R.id.navUndo -> {
            val text = history.undo()
            binding.edtContent.applyHistory(text); true
        }

        R.id.navRedo -> {
            binding.edtContent.applyHistory(history.redo()); true
        }

        R.id.navDelete -> {
            curNoteItem?.let { noteViewModel.deleteNote(it.id) }; finish(); true
        }

        R.id.navCategorize -> {
            handleCategorize(); true
        }

        R.id.navImport -> {
            handleImportFile(); true
        }

        R.id.navExport -> {
            handleExportFile(); true
        }

        R.id.navReadOnly -> {
            toggleEditMode(false); true
        }

        R.id.navEdit -> {
            toggleEditMode(true); true
        }

        R.id.navShare -> {
            shareNote(); true
        }

        R.id.navSearchDetail -> {
            startSearchMode(); true
        }

        else -> super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getNoteDataIfUpdate() {
        if (intent.getBooleanExtra(IS_EDITED_ACTION, false)) {
            curNoteItem = intent.getParcelableExtra(NOTE_DETAIL_OBJECT, Note::class.java)
            curNoteItem?.let {
                binding.edtTitle.setText(it.title)
                binding.edtContent.setText(it.content)
            }
        }
    }


    private fun handleContentChange() {
        binding.edtContent.addTextChangedListener { text ->
            optionsMenu.findItem(R.id.navUndo)?.isEnabled = !text.isNullOrEmpty()
        }
    }


    private fun handleCategorize() {
//        if (listCategories.isEmpty()) {
//            showToast("Please add at least 1 category first", this)
//            return
//        }
//
//        val names = listCategories.map { it.name }.toTypedArray()
//        val checkedItems = BooleanArray(listCategories.size)
//
//        MaterialAlertDialogBuilder(this)
//            .setTitle("Select Categories")
//            .setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
//                checkedItems[which] = isChecked
//            }
//            .setPositiveButton("Confirm") { _, _ ->
//                curNoteItem?.let {
//                    noteViewModel.updateNote(it.copy(updatedAt = System.currentTimeMillis()))
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
    }

    private fun upsertNote() {
        val title = binding.edtTitle.text.toString()
        val content = binding.edtContent.text.toString()

        if (title.isEmpty() && content.isEmpty()) {
            showToast("Type Something", this)
            return
        }

        val updatedNote = curNoteItem?.copy(
            title = title,
            content = content,
            updatedAt = System.currentTimeMillis()
        ) ?: Note(title = title, content = content)

        noteViewModel.upsertNote(updatedNote)
        showToast("$title Saved", this)
        finish()
    }

    private fun handleImportFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/plain"
        }
        importLauncher.launch(Intent.createChooser(intent, "Choose a TXT file"))
    }

    private fun handleExportFile() {
        val title = binding.edtTitle.text.toString()
        val content = binding.edtContent.text.toString()
        if (title.isEmpty() && content.isEmpty()) {
            showToast("Type something before export", this)
            return
        }

        FileHelper.exportToTxtFile(this, title, "$title\n$content")
        showToast("Exported to download dir", this)
    }

    private fun shareNote() {
        val title = binding.edtTitle.text.toString()
        val content = binding.edtContent.text.toString()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$content")
        }
        startActivity(Intent.createChooser(shareIntent, "Share note via"))
    }

    private fun startSearchMode() {
        val btnSave = findViewById<View>(R.id.navSave)
        val btnUndo = findViewById<View>(R.id.navUndo)

        btnSave.visibility = View.GONE
        btnUndo.visibility = View.GONE

        val btnClear = binding.btnClearSearch
        val edtSearch = binding.edtSearch
        edtSearch.visibility = View.VISIBLE;
        btnClear.visibility = View.VISIBLE
        edtSearch.requestFocus();

        edtSearch.doOnTextChanged { query, _, _, _ ->
            val keyword = query.toString()
            highlightSearchKeyword(binding.edtContent, keyword)
        }

        btnClear.setOnClickListener {
            edtSearch.text.clear()
            edtSearch.clearFocus()
            edtSearch.visibility = View.GONE
            btnClear.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
            btnUndo.visibility = View.VISIBLE
        }
    }

    private fun toggleEditMode(enable: Boolean) {
        optionsMenu.findItem(R.id.navSave).isVisible = enable
        optionsMenu.findItem(R.id.navUndo).isVisible = enable
        optionsMenu.findItem(R.id.navEdit).isVisible = !enable

        binding.edtTitle.inputType = if (enable) InputType.TYPE_CLASS_TEXT else InputType.TYPE_NULL
        binding.edtContent.inputType =
            if (enable) InputType.TYPE_CLASS_TEXT else InputType.TYPE_NULL
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(saveRunnable) // cleanup
    }

    private fun highlightSearchKeyword(editText: EditText, keyword: String) {
        val text = editText.text.toString()
        val spannable = SpannableString(text)

        // clear old highlight
        spannable.removeSpan(BackgroundColorSpan(Color.YELLOW))

        if (keyword.isNotBlank()) {
            var index = text.indexOf(keyword, 0, ignoreCase = true)
            while (index >= 0) {
                spannable.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    index,
                    index + keyword.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                index = text.indexOf(keyword, index + keyword.length, ignoreCase = true)
            }
        }

        editText.setText(spannable, TextView.BufferType.SPANNABLE)
    }
}

