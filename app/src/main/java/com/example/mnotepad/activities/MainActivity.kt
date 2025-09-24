package com.example.mnotepad.activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.R
import com.example.mnotepad.adapters.NoteAdapter
import com.example.mnotepad.assets.OptionsData.Companion.colorPalette
import com.example.mnotepad.assets.OptionsData.Companion.noteSortOptions
import com.example.mnotepad.databinding.ActivityMainBinding
import com.example.mnotepad.entities.enums.AppTheme
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.ColorPickerDialogHelper
import com.example.mnotepad.helpers.FileSAFHelper
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.ThemeManager
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.CategoryViewModel
import com.example.mnotepad.viewmodels.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView


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

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
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
        handleClickAdd()


    }

    private fun initImportMultipleTxtLauncher() {
        importMultipleTxtLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
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
                    showToast("Export successfully!", this)
                }
            }
        }
    }

    private fun handleClickAdd() {
        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, NoteDetailActivity::class.java).apply {
                putExtra(IS_EDITED_ACTION, false)
            }
            startActivity(intent)
        }

    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            emptyList(), ::openNoteDetail, ::updateMenuForMultiSelect
        )
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = noteAdapter
    }

    private fun updateMenuForMultiSelect(isMultiSelect: Boolean) {
        val menu = binding.toolbar.menu

        val searchItem = menu.findItem(R.id.navSearch)
        val sortItem = menu.findItem(R.id.navSort)
        val selectAllItem = menu.findItem(R.id.navSelectAll)
        val deleteAllItem = menu.findItem(R.id.navDeleteAll)

        if (isMultiSelect) {
            searchItem?.isVisible = false
            sortItem?.isVisible = false
            selectAllItem?.isVisible = true
            deleteAllItem?.isVisible = true
        } else {
            searchItem?.isVisible = true
            sortItem?.isVisible = true
            selectAllItem?.isVisible = false
            deleteAllItem?.isVisible = false
        }
    }

    private fun setupObservers() {
        noteViewModel.filteredNotes.observe(this) { notes ->
            noteAdapter.setNotes(notes)
        }
        categoryViewModel.categories.observe(this) {
            listCategories = it
            addMenuItemInNavMenuDrawer();
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
            if (item.groupId == 2) {
                showToast("ID ${item.itemId}", this)
                noteViewModel.filterByCategory(item.itemId)
            } else {
                when (item.itemId) {
                    R.id.navNotes -> {
                        noteViewModel.filterByCategory(0)
                    }

                    R.id.navEditCategories -> startActivity(
                        Intent(
                            this,
                            CategoryActivity::class.java
                        )
                    )

                    R.id.navTrash -> startActivity(Intent(this, TrashActivity::class.java))
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun openNoteDetail(note: Note) {
        startActivity(Intent(this, NoteDetailActivity::class.java).apply {
            putExtra(NOTE_DETAIL_OBJECT, note)
            putExtra(IS_EDITED_ACTION, true)
        })
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
                showSearchBar(); true
            }

            R.id.navSort -> {
                showSortDialog(); true
            }

            R.id.navSelectAll -> {
                if (noteAdapter.isAllSelected()) {
                    noteAdapter.clearSelection()
                    item.title = getString(R.string.all)
                } else {
                    noteAdapter.selectAll()
                    item.title = getString(R.string.unselect_all)
                }
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
                handleImportTxtFiles(); true
            }

            R.id.navCategorizeSelected -> {
                handleCategorize()
                true
            }

            R.id.navColorizeSelected -> {
                showColorPickerDialog(); true
            }

            R.id.navTheme -> {
                showThemePicker(); true

            }

            else -> toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
        }
    }

    private fun showThemePicker() {
        val themes = AppTheme.entries.toTypedArray()
        val names = themes.map { it.displayName }.toTypedArray()
        val current = ThemeManager.getSavedTheme(this)
        val checkedIndex = themes.indexOf(current)

        AlertDialog.Builder(this)
            .setTitle("Chọn giao diện")
            .setSingleChoiceItems(names, checkedIndex) { dialog, which ->
                val selected = themes[which]
                ThemeManager.setTheme(this, selected) // sẽ lưu và recreate()
                dialog.dismiss()
            }
            .setNegativeButton("Huỷ", null)
            .show()
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
        val submenu: Menu = menu.addSubMenu("Categories")

        for (category in listCategories) {
            submenu.add(2, category.id, category.orderIndex, category.name)
        }

        navView.invalidate()
    }

    private fun handleImportTxtFiles() {
        val intent = FileSAFHelper.createMultipleImportIntent()
        importMultipleTxtLauncher.launch(intent)
    }

    private fun handleExportSelected() {
        val selected = noteAdapter.getSelectedNotes()
        if (selected.isEmpty()) return

        selectedNotesToExport = selected.map { note ->
            note.title to note.content
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
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}