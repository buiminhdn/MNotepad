package com.example.mnotepad.activities

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.R
import com.example.mnotepad.adapters.ColorAdapter
import com.example.mnotepad.databinding.ActivityNoteDetailBinding
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.DateTimeHelper
import com.example.mnotepad.helpers.FileHelper
import com.example.mnotepad.helpers.FileSAFHelper
import com.example.mnotepad.helpers.HistoryManager
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.applyHistory
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.CategoryViewModel
import com.example.mnotepad.viewmodels.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.adapters.CategoryAdapter
import com.example.mnotepad.adapters.CheckListAdapter
import com.example.mnotepad.assets.OptionsData.Companion.colorOptions
import com.example.mnotepad.assets.OptionsData.Companion.colorPalette
import com.example.mnotepad.callbacks.ItemMoveCallback
import com.example.mnotepad.entities.enums.NoteType
import com.example.mnotepad.entities.models.ChecklistItem
import com.example.mnotepad.helpers.ColorPickerDialogHelper
import com.example.mnotepad.helpers.TextConvertHelper.convertCheckListContentToText
import com.example.mnotepad.helpers.TextConvertHelper.convertCheckListToContent
import com.example.mnotepad.helpers.TextConvertHelper.convertCheckListToContentForSave
import com.example.mnotepad.helpers.TextConvertHelper.convertContentToCheckList
import com.example.mnotepad.helpers.ThemeManager


class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var optionsMenu: Menu
    private val noteViewModel: NoteViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var checkListAdapter: CheckListAdapter
    private var listCategories: List<Category> = emptyList()
    private var curNoteItem: Note? = null
    private lateinit var importTxtLauncher: ActivityResultLauncher<Intent>
    private lateinit var createTxtLauncher: ActivityResultLauncher<Intent>
    private lateinit var createPdfLauncher: ActivityResultLauncher<Intent>
    var noteType = NoteType.TEXT
    var touchHelper: ItemTouchHelper? = null
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
        ThemeManager.applyTheme(this)
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
        setupRecyclerView()
        initObservers()
        getNoteDataIfUpdate()
        initImportLauncher()
        initCreateTxtLauncher()
        initCreatePdfLauncher()
        handleButtonDeleteSearch()
        handleAddNewItemToCheckList()
        setupDragAndDrop()

        handler.post(saveRunnable)
    }

    private fun handleAddNewItemToCheckList() {
        binding.btnAddChecklistItem.setOnClickListener {
            checkListAdapter.addItem(ChecklistItem("", false))
        }
    }

    private fun setupRecyclerView() {
        checkListAdapter = CheckListAdapter(mutableListOf()) { viewHolder ->
            touchHelper?.startDrag(viewHolder)
        }
        binding.rvCheckListItems.apply {
            layoutManager = LinearLayoutManager(this@NoteDetailActivity)
            adapter = checkListAdapter
        }
    }

    private fun handleButtonDeleteSearch() {

    }

    private fun initImportLauncher() {
        importTxtLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        val (title, content) = FileSAFHelper.importTxt(this, uri) ?: return@let
                        binding.edtTitle.setText(title)
                        binding.edtContent.setText(content)
                    }
                }
            }
    }

    private fun initCreateTxtLauncher() {
        createTxtLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        FileSAFHelper.exportTxt(this, uri, binding.edtContent.text.toString())
                    }
                }
            }
    }

    private fun initCreatePdfLauncher() {
        createPdfLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        FileSAFHelper.exportPdf(
                            this,
                            uri,
                            binding.edtTitle.text.toString(),
                            binding.edtContent.text.toString()
                        )
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

    fun setupDragAndDrop() {
        touchHelper = ItemTouchHelper(
            ItemMoveCallback(checkListAdapter)
        )
        touchHelper?.attachToRecyclerView(binding.rvCheckListItems)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_detail_menu, menu)
        if (menu != null) {
            optionsMenu = menu
            menu.findItem(R.id.navEdit).isVisible = false
            handleContentChange()
            updateConvertMenuTitle()
        }
        return true
    }

    private fun updateConvertMenuTitle() {
        val convertItem = optionsMenu.findItem(R.id.navChangeType)
        when (noteType) {
            NoteType.TEXT -> {
                binding.checklistLayout.visibility = View.GONE
                binding.edtContent.visibility = View.VISIBLE
                convertItem.title = "Convert to Checklist"
            }

            NoteType.CHECKLIST -> {
                binding.edtContent.visibility = View.GONE
                binding.checklistLayout.visibility = View.VISIBLE
                convertItem.title = "Convert to Text"
            }
        }
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

        R.id.navColorize -> {
            showColorPickerDialog(); true
        }

        R.id.navImport -> {
            handleImportFile(); true
        }

        R.id.navExport -> {
            handleExportFile(); true
        }

        R.id.navPrint -> {
            handlePrintFile(); true
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

        R.id.navShowInfo -> {
            showInfoDialog(); true
        }

        R.id.navChangeType -> {
            handleConvertNoteType()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun handleConvertNoteType() {
        val convertItem = optionsMenu.findItem(R.id.navChangeType)

        when (noteType) {
            NoteType.TEXT -> {
                noteType = NoteType.CHECKLIST
                binding.edtContent.visibility = View.GONE
                binding.checklistLayout.visibility = View.VISIBLE
                convertItem.title = "Convert to Text"

                val items = convertContentToCheckList(binding.edtContent.text.toString())

                checkListAdapter.setCheckListItems(items)
            }

            NoteType.CHECKLIST -> {
                noteType = NoteType.TEXT
                binding.checklistLayout.visibility = View.GONE
                binding.edtContent.visibility = View.VISIBLE
                convertItem.title = "Convert to Checklist"

                val text = convertCheckListToContent(checkListAdapter.getCheckListItems())
                binding.edtContent.setText(text)
            }
        }
    }

    private fun showColorPickerDialog() {
        ColorPickerDialogHelper.show(
            this,
            colorPalette,
            onColorSelected = { selectedColor ->
                showToast("Selected: $selectedColor", applicationContext)
                curNoteItem?.let {
                    noteViewModel.upsertNote(it.copy(color = selectedColor))
                    binding.noteDetailLayout.backgroundTintList =
                        ColorStateList.valueOf(selectedColor)
                }
            },
            onReset = {
                curNoteItem?.let { noteViewModel.upsertNote(it.copy(color = null)) }
                binding.noteDetailLayout.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.secondary)
            }
        )
    }

    private fun showInfoDialog() {
        val content = binding.edtContent.text.toString()
        val numOfWords = content.split("\\s+".toRegex()).size
        val numOfWrappedLines = content.split("\n+".toRegex()).size
        val numOfCharacters = content.length
        val numOfCharactersWithoutWhitespaces = content
            .trim()
            .replace(" ", "")
            .replace("\n", "").length
        val createdAt = DateTimeHelper.getFormatedDate(curNoteItem?.createdAt)
        val updatedAt = DateTimeHelper.getFormatedDate(curNoteItem?.updatedAt)
        val contentInfo = "Words: $numOfWords \n" +
                "Wrapped lines: $numOfWrappedLines\n" +
                "Characters: $numOfCharacters\n" +
                "Characters without whitespaces: $numOfCharactersWithoutWhitespaces\n" +
                "Created at: $createdAt\n" +
                "Last saved at: $updatedAt"
//                "Last saved at: ${curNoteItem?.updatedAt}"
        MaterialAlertDialogBuilder(this)
            .setMessage(contentInfo)
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getNoteDataIfUpdate() {
        if (intent.getBooleanExtra(IS_EDITED_ACTION, false)) {
            curNoteItem = intent.getParcelableExtra(NOTE_DETAIL_OBJECT, Note::class.java)
            curNoteItem?.let {
                binding.edtTitle.setText(it.title)
                if (it.type == NoteType.TEXT && it.content.isNotEmpty()) {
                    binding.edtContent.setText(it.content)
                } else {
                    checkListAdapter.setCheckListItems(convertCheckListContentToText(it.content))
                }

                noteType = it.type

                if (it.color != null) {
                    binding.noteDetailLayout.backgroundTintList = ColorStateList.valueOf(it.color)
                } else {
                    binding.noteDetailLayout.backgroundTintList = ContextCompat.getColorStateList(
                        this,
                        R.color.secondary
                    )
                }
            }
        }
    }


    private fun handleContentChange() {
        binding.edtContent.addTextChangedListener { text ->
            optionsMenu.findItem(R.id.navUndo)?.isEnabled = !text.isNullOrEmpty()
        }
    }


    private fun handleCategorize() {
        if (listCategories.isEmpty()) {
            showToast("Please add at least 1 category first", this)
            return
        }

        val names = listCategories.map { it.name }.toTypedArray()
        val checkedCategories = curNoteItem?.categoryIds ?: emptyList()

        val checkedItems = BooleanArray(listCategories.size)

        for (i in 0..checkedItems.size - 1) {
            checkedItems[i] = checkedCategories.contains(listCategories[i].id)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Categories")
            .setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Confirm") { _, _ ->
                curNoteItem?.let {
                    val checkedId = arrayListOf<Int>()

                    for (i in 0..checkedItems.size - 1) {
                        if (checkedItems[i]) {
                            checkedId.add(listCategories[i].id)
                        }
                    }

                    noteViewModel.upsertNote(it.copy(categoryIds = checkedId))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun upsertNote() {
        val title = binding.edtTitle.text.toString()
        var content = ""

        content = if (noteType == NoteType.TEXT) {
            binding.edtContent.text.toString()
        } else {
            convertCheckListToContentForSave(checkListAdapter.getCheckListItems())
        }

        if (title.isEmpty() && content.isEmpty()) {
            showToast("Type Something", this)
            return
        }

        val updatedNote = curNoteItem?.copy(
            title = title,
            content = content,
            updatedAt = System.currentTimeMillis(),
            type = noteType
        ) ?: Note(title = title, content = content, type = noteType)

        noteViewModel.upsertNote(updatedNote)
        showToast("$title Saved", this)
        finish()
    }

    private fun handleImportFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        importTxtLauncher.launch(intent)
    }

    private fun handleExportFile() {
        val title = binding.edtTitle.text.toString()
        val content = binding.edtContent.text.toString()
        if (title.isEmpty() && content.isEmpty()) {
            showToast("Type something before export", this)
            return
        }

        val intent =
            FileSAFHelper.createFileIntent(binding.edtTitle.text.toString(), "txt", "text/plain")
        createTxtLauncher.launch(intent)
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

    private fun handlePrintFile() {
        val intent = FileSAFHelper.createFileIntent(
            binding.edtTitle.text.toString(),
            "pdf",
            "application/pdf"
        )
        createPdfLauncher.launch(intent)
    }

    private fun startSearchMode() {
        val btnSave = findViewById<View>(R.id.navSave)
        val btnUndo = findViewById<View>(R.id.navUndo)

        // Hide button Save, Undo
        btnSave.visibility = View.GONE
        btnUndo.visibility = View.GONE

        // Show Input Search, X icon
        val btnClear = binding.btnClearSearch
        val edtSearch = binding.edtSearch
        edtSearch.visibility = View.VISIBLE;
        btnClear.visibility = View.VISIBLE
        edtSearch.requestFocus();

        edtSearch.doOnTextChanged { query, _, _, _ ->
            val keyword = query.toString()
            highlightSearchKeyword(binding.edtContent, keyword)
            // Mỗi lần text change thì 2 nút này chạy lại
            // Nên set lại như vầy
            btnSave.visibility = View.GONE
            btnUndo.visibility = View.GONE
        }

        // Reset lại view ban đầu
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

