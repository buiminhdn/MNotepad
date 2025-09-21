package com.example.mnotepad.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.R
import com.example.mnotepad.adapters.NoteAdapter
import com.example.mnotepad.assets.OptionsData.Companion.noteSortOptions
import com.example.mnotepad.databinding.ActivityMainBinding
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.FileHelper
import com.example.mnotepad.helpers.FileSAFHelper
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var noteAdapter: NoteAdapter
    private val noteViewModel: NoteViewModel by viewModels()
    private var selectedSortTypeIndex: Int = 0
    private lateinit var selectFolderLauncher: ActivityResultLauncher<Intent>
    private lateinit var importMultipleTxtLauncher: ActivityResultLauncher<Intent>
    private var selectedNotes: List<Pair<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
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

                showToast( "Imported ${notes.size} files", this)
            }
        }
    }

    private fun initSelectFolderLauncher() {
        selectFolderLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val treeUri = result.data?.data
                if (treeUri != null) {
                    // Ghi notes ra thư mục
                    FileSAFHelper.exportSelectedNotesToTxt(this, treeUri, selectedNotes)
                    Toast.makeText(this, "Export thành công!", Toast.LENGTH_SHORT).show()
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
            emptyList(), ::openNoteDetail,::updateMenuForMultiSelect
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
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navEditCategories -> startActivity(Intent(this, CategoryActivity::class.java))
                R.id.navTrash -> startActivity(Intent(this, TrashActivity::class.java))
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

            else -> toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
        }
    }

    private fun handleImportTxtFiles() {
        val intent = FileSAFHelper.createMultipleImportIntent()
        importMultipleTxtLauncher.launch(intent)
    }

    private fun handleExportSelected() {
        val selected = noteAdapter.getSelectedNotes()
        if (selected.isEmpty()) return

        selectedNotes = selected.map { note ->
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
            noteViewModel.filter(text?.toString() ?: "")
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


}