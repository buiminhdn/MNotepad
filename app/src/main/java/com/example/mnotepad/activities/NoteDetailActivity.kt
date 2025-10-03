package com.example.mnotepad.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.R
import com.example.mnotepad.adapters.CheckListAdapter
import com.example.mnotepad.assets.OptionsData.colorPalette
import com.example.mnotepad.callbacks.ItemMoveCallback
import com.example.mnotepad.databinding.ActivityNoteDetailBinding
import com.example.mnotepad.entities.enums.NoteType
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.ChecklistItem
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.CATEGORY_ID
import com.example.mnotepad.helpers.ColorPickerDialogHelper
import com.example.mnotepad.helpers.DELAY_TYPING
import com.example.mnotepad.helpers.DateTimeHelper
import com.example.mnotepad.helpers.FileHelper
import com.example.mnotepad.helpers.FileSAFHelper
import com.example.mnotepad.helpers.HistoryManager
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.PLAIN_TYPE
import com.example.mnotepad.helpers.PrintHelper
import com.example.mnotepad.helpers.TextConvertHelper.convertCheckListContentToText
import com.example.mnotepad.helpers.TextConvertHelper.convertCheckListToContent
import com.example.mnotepad.helpers.TextConvertHelper.convertCheckListToContentForSave
import com.example.mnotepad.helpers.TextConvertHelper.convertContentToCheckList
import com.example.mnotepad.helpers.TextEditorHelper
import com.example.mnotepad.helpers.ThemeManager
import com.example.mnotepad.helpers.applyHistory
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.helpers.toHexColor
import com.example.mnotepad.viewmodels.CategoryViewModel
import com.example.mnotepad.viewmodels.NoteViewModel
import com.example.mnotepad.widgets.NoteWidget
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
    var noteType = NoteType.TEXT
    var touchHelper: ItemTouchHelper? = null
    private val history = HistoryManager()
    private val handler = Handler(Looper.getMainLooper())
    private val saveRunnable = object : Runnable {
        override fun run() {
            history.save(binding.edtContent.text.toSpannable())
            handler.postDelayed(this, DELAY_TYPING)
        }
    }

    private var createdDate: Long = 0
    private var currentColor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initToolbar()
        setupRecyclerView()
        initObservers()
        createdDate = DateTimeHelper.getCurrentTime()
        getNoteDataIfUpdate()
        initImportLauncher()
        initCreateTxtLauncher()
        handleAddNewItemToCheckList()
        setupDragAndDrop()
        handleTextEditor()
        handleTitleChange()

        handler.post(saveRunnable)
    }

    private fun handleTitleChange() {
        binding.edtTitle.setOnFocusChangeListener { view, hasFocus ->
            binding.formattingBar.isEnabled = false
            binding.formattingBar.isVisible = false
        }
        binding.edtContent.setOnFocusChangeListener { view, hasFocus ->
            binding.formattingBar.isEnabled = true
            binding.formattingBar.isVisible = true
        }
    }

    private fun handleTextEditor() {
        TextEditorHelper.attachTo(binding.edtContent)

        binding.ivBold.setOnClickListener {
            val active = TextEditorHelper.toggleBold(binding.edtContent)
            binding.ivBold.isSelected = active
        }

        binding.ivItalic.setOnClickListener {
            val active = TextEditorHelper.toggleItalic(binding.edtContent)
            binding.ivItalic.isSelected = active
        }

        binding.icUnderline.setOnClickListener {
            val active = TextEditorHelper.toggleUnderline(binding.edtContent)
            binding.icUnderline.isSelected = active
        }

        binding.ivColor.setOnClickListener {
            ColorPickerDialogHelper.show(
                this,
                colorPalette,
                currentColor = TextEditorHelper.activeColor?.toHexColor(),
                onColorSelected = { selectedColor ->
                    val active = TextEditorHelper.toggleColor(binding.edtContent, selectedColor)
                    binding.ivColor.isSelected = active
                },
                onReset = {
                    TextEditorHelper.activeColor = null
                    binding.ivColor.isSelected = false
                }
            )
        }
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

    private fun initObservers() {
        categoryViewModel.categories.observe(this) {
            listCategories = it
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            upsertNote()
            finish()
        }
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
            updateConvertMenuTitle()
        }
        return true
    }

    private fun updateConvertMenuTitle() {
        val convertItem = optionsMenu.findItem(R.id.navChangeType)
        when (noteType) {
            NoteType.TEXT -> {
                binding.checklistLayout.visibility = View.GONE
                binding.noteContentLayout.visibility = View.VISIBLE
                convertItem.title = "Convert to Checklist"
            }

            NoteType.CHECKLIST -> {
                binding.noteContentLayout.visibility = View.GONE
                binding.checklistLayout.visibility = View.VISIBLE
                convertItem.title = "Convert to Text"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.navSave -> {
            upsertNote()
            true
        }

        R.id.navUndo -> {
            val text = history.undo()
            TextEditorHelper.detachTextWatcher(binding.edtContent)
            binding.edtContent.applyHistory(text)
            TextEditorHelper.attachTo(binding.edtContent)
            true
        }

        R.id.navRedo -> {
            binding.edtContent.applyHistory(history.redo())
            true
        }

        R.id.navDelete -> {
            curNoteItem?.let { noteViewModel.deleteNote(it.id) }
            finish()
            true
        }

        R.id.navCategorize -> {
            handleCategorize()
            true
        }

        R.id.navColorize -> {
            showColorPickerDialog()
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

        R.id.navPrint -> {
            handlePrintFile()
            true
        }

        R.id.navReadOnly -> {
            toggleEditMode(false)
            true
        }

        R.id.navEdit -> {
            toggleEditMode(true)
            true
        }

        R.id.navShare -> {
            shareNote()
            true
        }

        R.id.navSearchDetail -> {
            startSearchMode()
            true
        }

        R.id.navShowInfo -> {
            showInfoDialog()
            true
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
                binding.noteContentLayout.visibility = View.GONE
                binding.checklistLayout.visibility = View.VISIBLE
                convertItem.title = "Convert to Text"

                val items = convertContentToCheckList(binding.edtContent.text.toString())

                checkListAdapter.setCheckListItems(items)
            }

            NoteType.CHECKLIST -> {
                noteType = NoteType.TEXT
                binding.checklistLayout.visibility = View.GONE
                binding.noteContentLayout.visibility = View.VISIBLE
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
            currentColor = curNoteItem?.color,
            onColorSelected = { selectedColor ->
                currentColor = selectedColor
                showToast("Selected: $selectedColor", applicationContext)
                val colorInt = selectedColor.toColorInt()
                curNoteItem?.let {
                    noteViewModel.upsertNote(it.copy(color = selectedColor))
                    curNoteItem = it.copy(color = selectedColor)
                }
                binding.noteDetailLayout.backgroundTintList =
                    ColorStateList.valueOf(colorInt)
            },
            onReset = {
                currentColor = ""
                curNoteItem?.let {
                    noteViewModel.upsertNote(it.copy(color = null))
                    curNoteItem = it.copy(color = null)
                }
                binding.noteDetailLayout.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.secondary)
            }
        )
    }

    private fun showInfoDialog() {
        val content = binding.edtContent.text.toString()
        val isContentEmpty = content.isEmpty()
        val numOfWords = if (isContentEmpty) 0 else content.trim().split("\\s+".toRegex()).size
        val numOfWrappedLines = if (isContentEmpty) 0 else content.split("\n+".toRegex()).size
        val numOfCharacters = if (isContentEmpty) 0 else content.trim().length
        val numOfCharactersWithoutWhitespaces = if (isContentEmpty) {
            0
        } else {
            content
                .trim()
                .replace(" ", "")
                .replace("\n", "").length
        }
        val createdAt = DateTimeHelper.getFormatedDate(createdDate)
        val lastSavedAt = DateTimeHelper.getFormatedDate(
            curNoteItem?.updatedAt ?: DateTimeHelper.getCurrentTime()
        )
        val contentInfo = "Words: $numOfWords \n" +
                "Wrapped lines: $numOfWrappedLines\n" +
                "Characters: $numOfCharacters\n" +
                "Characters without whitespaces: $numOfCharactersWithoutWhitespaces\n" +
                "Created at: $createdAt\n" +
                "Last saved at: $lastSavedAt"
        MaterialAlertDialogBuilder(this)
            .setMessage(contentInfo)
            .setPositiveButton(getString(R.string.txt_option_ok_upper)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getNoteDataIfUpdate() {
        TextEditorHelper.reset()

        if (intent.getBooleanExtra(IS_EDITED_ACTION, false)) {
            curNoteItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NOTE_DETAIL_OBJECT, Note::class.java)
            } else {
                intent.getParcelableExtra(NOTE_DETAIL_OBJECT)
            }
            curNoteItem?.let {
                createdDate = it.createdAt
                binding.edtTitle.setText(it.title)
                if (it.type == NoteType.TEXT && it.content.isNotEmpty()) {
                    binding.edtContent.setText(
                        Html.fromHtml(it.content, Html.FROM_HTML_MODE_COMPACT)
                    )
                } else {
                    checkListAdapter.setCheckListItems(convertCheckListContentToText(it.content))
                }

                noteType = it.type

                it.color?.takeIf { hex -> hex.isNotBlank() }?.let { colorHex ->
                    val colorInt = colorHex.toColorInt()
                    binding.noteDetailLayout.backgroundTintList = ColorStateList.valueOf(colorInt)
                }
            }
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
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun upsertNote() {
        val title = binding.edtTitle.text.toString()
        val content = if (noteType == NoteType.TEXT) {
            Html.toHtml(binding.edtContent.text, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        } else {
            convertCheckListToContentForSave(checkListAdapter.getCheckListItems())
        }

        if (title.isEmpty() && content.isEmpty()) {
            return
        }

        val categoryId = intent.getIntExtra(CATEGORY_ID, 0)

        val updatedNote = curNoteItem?.copy(
            title = title,
            content = content,
            updatedAt = System.currentTimeMillis(),
            type = noteType
        ) ?: Note(
            title = title, content = content, type = noteType,
            categoryIds = when (categoryId) {
                0 -> emptyList()
                -1 -> emptyList()
                else -> listOf(categoryId)
            },
            color = currentColor.takeIf { it.isNotBlank() }
        )

        noteViewModel.upsertNote(updatedNote)
        showToast("$title Saved", this)

        updatedNote.id.let { noteId ->
            val intent = Intent(applicationContext, NoteWidget::class.java).apply {
                action = NoteWidget.ACTION_NOTE_CHANGED
                putExtra(NoteWidget.EXTRA_NOTE_ID, noteId)
            }
            applicationContext.sendBroadcast(intent)
        }

        val resultIntent = Intent().apply {
            putExtra(CATEGORY_ID, categoryId)
        }
        setResult(RESULT_OK, resultIntent)

        finish()
    }

    private fun handleImportFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = PLAIN_TYPE
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent =
                FileSAFHelper.createFileIntent(binding.edtTitle.text.toString(), "txt", PLAIN_TYPE)
            createTxtLauncher.launch(intent)
        } else {
            FileHelper.exportToTxtFile(title, "$title\n$content")
        }
    }

    private fun shareNote() {
        val title = binding.edtTitle.text.toString()
        val content = binding.edtContent.text.toString()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = PLAIN_TYPE
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$content")
        }
        startActivity(Intent.createChooser(shareIntent, "Share note via"))
    }

    private fun handlePrintFile() {
        val title = binding.edtTitle.text.toString()
        val content = binding.edtContent.text.toString()
        PrintHelper.print(this, title, content)
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
        edtSearch.visibility = View.VISIBLE
        btnClear.visibility = View.VISIBLE
        edtSearch.requestFocus()

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

        binding.edtTitle.isEnabled = enable
        binding.edtContent.isEnabled = enable
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(saveRunnable)
        binding.noteDetailLayout.backgroundTintList = null
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
