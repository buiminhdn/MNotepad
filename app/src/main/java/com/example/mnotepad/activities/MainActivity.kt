package com.example.mnotepad.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.R
import com.example.mnotepad.adapters.NoteAdapter
import com.example.mnotepad.assets.OptionsData.colorPalette
import com.example.mnotepad.assets.OptionsData.noteSortOptions
import com.example.mnotepad.databinding.ActivityMainBinding
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.CATEGORY_ID
import com.example.mnotepad.helpers.CATEGORY_MENU_ID
import com.example.mnotepad.helpers.ColorPickerDialogHelper
import com.example.mnotepad.helpers.FileSAFHelper
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.ThemeManager.applyTheme
import com.example.mnotepad.helpers.ThemeManager.toggleThemeChange
import com.example.mnotepad.helpers.UNCATEGORIZE
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.CategoryViewModel
import com.example.mnotepad.viewmodels.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var noteAdapter: NoteAdapter
    private val noteViewModel: NoteViewModel by viewModels()
    private var listCategories: List<Category> = emptyList()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private var selectedSortTypeIndex: Int = 0
    private lateinit var selectFolderLauncher: ActivityResultLauncher<Intent>
    private lateinit var importMultipleTxtLauncher: ActivityResultLauncher<Intent>
    private var selectedNotesToExport: List<Pair<String, String>> = emptyList()
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var noteDetailLauncher: ActivityResultLauncher<Intent>

    private var currentCategoryId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme(this)
        toggleThemeChange(false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbarAndDrawer()
        setupRecyclerView()
        setupObservers()
        initSelectFolderLauncher()
        initImportMultipleTxtLauncher()
        initSettingLauncher()
        initNoteDetailLauncher()
        handleClickAdd()
    }

    private fun initNoteDetailLauncher() {
        noteDetailLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val categoryId = result.data?.getIntExtra(CATEGORY_ID, 0) ?: 0
                noteViewModel.filterByCategory(categoryId)
                currentCategoryId = categoryId
            }
        }
    }

    private fun initSettingLauncher() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    recreate()
                }
            }
    }

    private fun initImportMultipleTxtLauncher() {
        importMultipleTxtLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val notes = FileSAFHelper.importMultipleTxt(this, data)

                val noteEntities = notes.map { (title, content) ->
                    Note(title = title, content = content)
                }

                noteViewModel.insertNotes(noteEntities)

                showToast("Imported ${notes.size} files", this)
            }
        }
    }

    private fun initSelectFolderLauncher() {
        selectFolderLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val treeUri = result.data?.data
                if (treeUri != null) {
                    // Ghi notes ra thư mục
                    FileSAFHelper.exportSelectedNotesToTxt(this, treeUri, selectedNotesToExport)
                    showToast("Export ${selectedNotesToExport.size} notes successfully!", this)
                }
            }
        }
    }

    private fun handleClickAdd() {
        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, NoteDetailActivity::class.java).apply {
                putExtra(IS_EDITED_ACTION, false)
                putExtra(CATEGORY_ID, currentCategoryId)
            }
            noteDetailLauncher.launch(intent)
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            ::openNoteDetail, ::updateMenuForMultiSelect, ::updateSelectCount
        )

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = noteAdapter

        val resId = R.anim.layout_animation_slide_up
        val animation: LayoutAnimationController? = AnimationUtils.loadLayoutAnimation(this, resId)
        binding.rvNotes.setLayoutAnimation(animation)
    }

    private fun updateSelectCount(size: Int) {
        if (size == 0) {
            binding.toolbarTitle.text = getResources().getString(R.string.txt_app_name)
        } else {
            binding.toolbar.menu.findItem(R.id.navSelectAllNotes).isVisible = false
            binding.toolbarTitle.text = size.toString()
        }
    }

    private fun updateMenuForMultiSelect(isMultiSelect: Boolean) {
        val menu = binding.toolbar.menu

        val searchItem = menu.findItem(R.id.navSearch)
        val sortItem = menu.findItem(R.id.navSort)
        val selectAllItem = menu.findItem(R.id.navSelectAll)
        val deleteAllItem = menu.findItem(R.id.navDeleteAll)
        val categorizeItem = menu.findItem(R.id.navCategorizeSelected)
        val colorizeItem = menu.findItem(R.id.navColorizeSelected)

        if (isMultiSelect) {
            searchItem?.isVisible = false
            sortItem?.isVisible = false
            selectAllItem?.isVisible = true
            deleteAllItem?.isVisible = true
            categorizeItem.isVisible = true
            colorizeItem.isVisible = true
        } else {
            binding.toolbarTitle.text = getString(R.string.txt_app_name)
            searchItem?.isVisible = true
            sortItem?.isVisible = true
            selectAllItem?.isVisible = false
            deleteAllItem?.isVisible = false
            categorizeItem.isVisible = false
            colorizeItem.isVisible = false
        }
    }

    private fun setupObservers() {
        noteViewModel.filteredNotes.observe(this) { notes ->
            noteAdapter.submitList(notes)
        }
        categoryViewModel.categories.observe(this) {
            listCategories = it
            addMenuItemInNavMenuDrawer()
        }
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "MNotepad"

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.nav_open,
            R.string.nav_close,
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { item ->
            if (item.groupId == CATEGORY_MENU_ID) {
                showToast("ID ${item.itemId}", this)
                noteViewModel.filterByCategory(item.itemId)
                binding.toolbarSubtitle.visibility = View.VISIBLE
                binding.toolbarSubtitle.text = item.toString()
                currentCategoryId = item.itemId
                removeSelected()
            } else {
                when (item.itemId) {
                    R.id.navNotes -> {
                        noteViewModel.filterByCategory(0)
                        binding.toolbarSubtitle.visibility = View.GONE
                        currentCategoryId = 0
                        removeSelected()
                    }

                    R.id.navUncategorized -> {
                        noteViewModel.filterByCategory(-1)
                        binding.toolbarSubtitle.visibility = View.VISIBLE
                        binding.toolbarSubtitle.text = UNCATEGORIZE
                        currentCategoryId = -1
                        removeSelected()
                    }

                    R.id.navEditCategories -> startActivity(
                        Intent(
                            this,
                            CategoryActivity::class.java
                        )
                    )

                    R.id.navTrash -> startActivity(Intent(this, TrashActivity::class.java))

                    R.id.navSettings -> {
                        val intent = Intent(this, SettingsActivity::class.java)
                        resultLauncher.launch(intent)
                    }

                    R.id.navRating -> {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://play.google.com/store/apps/details?id=com.atomczak.notepat".toUri()
                            )
                        )
                    }

                    R.id.navHelp -> {
                        startActivity(Intent(this, HelpActivity::class.java))
                    }

                    R.id.navUsers -> {
                        startActivity(Intent(this, UserActivity::class.java))
                    }
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun openNoteDetail(note: Note) {
        val intent = Intent(this, NoteDetailActivity::class.java).apply {
            putExtra(NOTE_DETAIL_OBJECT, note)
            putExtra(IS_EDITED_ACTION, true)
            putExtra(CATEGORY_ID, currentCategoryId)
        }
        noteDetailLauncher.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        menu.findItem(R.id.navSelectAll)?.isVisible = false
        menu.findItem(R.id.navDeleteAll)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navSearch -> {
                showSearchBar()
                true
            }

            R.id.navSort -> {
                showSortDialog()
                true
            }

            R.id.navSelectAll -> {
                binding.toolbar.menu.findItem(R.id.navSelectAllNotes).isVisible = false
                toggleSelect(noteAdapter.isAllSelected())
                true
            }

            R.id.navDeleteAll -> {
                handleDeleteAll()
                true
            }

            R.id.navExportSelected -> {
                handleExportSelected()
                true
            }

            R.id.navImportTxtFiles -> {
                handleImportTxtFiles()
                true
            }

            R.id.navCategorizeSelected -> {
                handleCategorize()
                true
            }

            R.id.navColorizeSelected -> {
                showColorPickerDialog()
                true
            }

            R.id.navSelectAllNotes -> {
                noteViewModel.filteredNotes.value?.let {
                    if (it.isNotEmpty()) {
                        binding.toolbar.menu.findItem(R.id.navSelectAllNotes).isVisible = false
                    }
                }
                toggleSelect(noteAdapter.isAllSelected())
                true
            }

            else -> toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
        }
    }

    private fun toggleSelect(isSelected: Boolean) {
        if (noteViewModel.filteredNotes.value?.isEmpty() == true) {
            return
        }
        if (isSelected) {
            noteAdapter.clearSelection()
            binding.toolbar.menu.findItem(R.id.navSelectAllNotes).isVisible = true
        } else {
            noteAdapter.selectAll()
            binding.toolbarTitle.text = noteAdapter.getSelectedNotesCount().toString()
        }
    }

    private fun showColorPickerDialog() {
        ColorPickerDialogHelper.show(
            this,
            colorPalette,
            onColorSelected = { selectedColor ->
                val selectedNotes = noteAdapter.getSelectedNotes()
                noteViewModel.updateNotes(selectedNotes.map { it.copy(color = selectedColor) })
                noteAdapter.toggleSelectMode(false)
            },
            onReset = {
                val selectedNotes = noteAdapter.getSelectedNotes()
                noteViewModel.updateNotes(selectedNotes.map { it.copy(color = null) })
                noteAdapter.toggleSelectMode(false)
            }
        )
    }

    private fun addMenuItemInNavMenuDrawer() {
        val navView = findViewById<View?>(R.id.navView) as NavigationView
        val menu = navView.menu

        menu.clear()

        navView.inflateMenu(R.menu.drawer_menu)

        val submenu: Menu = menu.addSubMenu("Categories")

        if (listCategories.isEmpty()) {
            menu.findItem(R.id.navUncategorized).isVisible = false
            return
        }

        for (category in listCategories) {
            submenu.add(CATEGORY_MENU_ID, category.id, category.orderIndex, category.name)
        }
    }

    private fun handleImportTxtFiles() {
        val intent = FileSAFHelper.createMultipleImportIntent()
        importMultipleTxtLauncher.launch(intent)
    }

    private fun handleExportSelected() {
        val selected = noteAdapter.getSelectedNotes()
        if (selected.isEmpty()) {
            val allNotes = noteViewModel.filteredNotes.value
            if (allNotes != null && allNotes.isNotEmpty()) {
                selectedNotesToExport = allNotes.map { note ->
                    note.title to note.content
                }
            }
        } else {
            selectedNotesToExport = selected.map { note ->
                note.title to note.content
            }
        }


        // Mở UI cho user chọn thư mục
        val intent = FileSAFHelper.createDirectoryIntent()
        selectFolderLauncher.launch(intent)
    }

    private fun handleDeleteAll() {
        val selectedNotes = noteAdapter.getSelectedNotes()
        if (selectedNotes.isNotEmpty()) {
            noteViewModel.softDeleteNotes(selectedNotes)
            noteAdapter.clearSelection()
            updateMenuForMultiSelect(false)
            showToast("Deleted ${selectedNotes.size} notes", this)
        } else {
            showToast("No notes selected", this)
        }
    }

    private fun showSortDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sort By")
            .setSingleChoiceItems(noteSortOptions, selectedSortTypeIndex) { _, which ->
                selectedSortTypeIndex = which
            }
            .setPositiveButton("Sort") { _, _ ->
                val sortType = noteSortOptions[selectedSortTypeIndex]
                noteViewModel.sortBy(sortType)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSearchBar() {
        binding.toolbarTitle.visibility = View.GONE
        val btnSearch = findViewById<View>(R.id.navSearch)
        val btnSort = findViewById<View>(R.id.navSort)
        val edtSearch = binding.edtSearch
        val btnClear = binding.btnClearSearch

        btnSearch.visibility = View.GONE
        btnSort.visibility = View.GONE
        edtSearch.visibility = View.VISIBLE
        btnClear.visibility = View.VISIBLE
        edtSearch.requestFocus()

        edtSearch.doOnTextChanged { text, _, _, _ ->
            noteViewModel.filterByQuery(text?.toString() ?: "")
        }

        btnClear.setOnClickListener {
            edtSearch.text.clear()
            edtSearch.clearFocus()
            edtSearch.visibility = View.GONE
            btnClear.visibility = View.GONE
            btnSearch.visibility = View.VISIBLE
            btnSort.visibility = View.VISIBLE
            binding.toolbarTitle.visibility = View.VISIBLE
        }
    }

    private fun handleCategorize() {
        if (listCategories.isEmpty()) {
            showToast("Please add at least 1 category first", this)
            return
        }

        val names = listCategories.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(listCategories.size)

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Categories")
            .setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Confirm") { _, _ ->
                val checkedIds = arrayListOf<Int>()

                for (i in 0..checkedItems.size - 1) {
                    if (checkedItems[i]) {
                        checkedIds.add(listCategories[i].id)
                    }
                }

                val selectedNotes = noteAdapter.getSelectedNotes()

                noteViewModel.updateNotes(selectedNotes.map { it.copy(categoryIds = checkedIds) })
                showToast("Update Categories successfully", this)
                noteAdapter.toggleSelectMode(false)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun removeSelected() {
        if (noteAdapter.isMultiSelected()) {
            noteAdapter.toggleSelectMode(false)
        }
    }
}
