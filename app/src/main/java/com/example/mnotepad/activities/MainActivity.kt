package com.example.mnotepad.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
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
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_TITLE_A_Z
import com.example.mnotepad.helpers.SORT_TITLE_Z_A
import com.example.mnotepad.viewmodels.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var noteAdapter: NoteAdapter
    private val noteViewModel: NoteViewModel by viewModels()
    private var selectedSortTypeIndex: Int = 0
    private lateinit var selectedSortType: String

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

        initNavigationView()
        setupViewModel()
        handleClickAdd()
        handleClickDrawerMenu()
    }

    private fun handleClickDrawerMenu() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navEditCategories -> {
                    startActivity(Intent(this, CategoryActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }

                else -> true
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

    private fun setupViewModel() {
        noteAdapter = NoteAdapter(emptyList(), ::startToNoteDetail)

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = noteAdapter

        noteViewModel.notes.observe(this, noteAdapter::setNotes)

    }

    private fun initNavigationView() {
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
    }

    private fun startToNoteDetail(note: Note) {
        val intent = Intent(this, NoteDetailActivity::class.java).apply {
            putExtra(NOTE_DETAIL_OBJECT, note)
            putExtra(IS_EDITED_ACTION, true)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navSearch -> {
                handleSearch()
                true
            }

            R.id.navSort -> {
                handleSort()
                true
            }

            else -> {
                if (toggle.onOptionsItemSelected(item)) {
                    true
                } else {
                    super.onOptionsItemSelected(item)
                }
            }
        }
    }

    private fun handleSort() {
        selectedSortType = noteSortOptions[selectedSortTypeIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Sort By")
            .setSingleChoiceItems(noteSortOptions, selectedSortTypeIndex) { dialog_, which ->
                selectedSortTypeIndex = which
                selectedSortType = noteSortOptions[which]
            }
            .setPositiveButton("Sort") { dialog, which ->
                when (selectedSortType) {
                    SORT_EDIT_DATE_FROM_NEWEST -> {
                        val allNotes = noteViewModel.notes.value ?: emptyList()
                        val filtered = allNotes.sortedByDescending { it.updatedAt }
                        noteAdapter.setNotes(filtered)
                    }
                    SORT_EDIT_DATE_FROM_OLDEST -> {
                        val allNotes = noteViewModel.notes.value ?: emptyList()
                        val filtered = allNotes.sortedBy { it.updatedAt }
                        noteAdapter.setNotes(filtered)
                    }
                    SORT_TITLE_A_Z -> {
                        val allNotes = noteViewModel.notes.value ?: emptyList()
                        val filtered = allNotes.sortedBy { it.title }
                        noteAdapter.setNotes(filtered)
                    }
                    SORT_TITLE_Z_A -> {
                        val allNotes = noteViewModel.notes.value ?: emptyList()
                        val filtered = allNotes.sortedByDescending { it.title }
                        noteAdapter.setNotes(filtered)
                    }
                    SORT_CREATE_DATE_FROM_NEWEST -> {
                        val allNotes = noteViewModel.notes.value ?: emptyList()
                        val filtered = allNotes.sortedByDescending { it.updatedAt }
                        noteAdapter.setNotes(filtered)
                    }
                    SORT_CREATE_DATE_FROM_OLDEST -> {
                        val allNotes = noteViewModel.notes.value ?: emptyList()
                        val filtered = allNotes.sortedBy { it.updatedAt }
                        noteAdapter.setNotes(filtered)
                    }
                    else -> true
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun handleSearch() {
        val searchBtn = findViewById<View>(R.id.navSearch)
        searchBtn.visibility = View.GONE
        val edtSearch = binding.edtSearch
        edtSearch.visibility = View.VISIBLE;
        edtSearch.requestFocus();

        edtSearch.doOnTextChanged { text, start, before, count ->
            val query = text?.toString() ?: ""

            val allNotes = noteViewModel.notes.value ?: emptyList()
            if (query.isNotEmpty()) {
                val filtered = allNotes.filter { it.title.contains(query, ignoreCase = true) }
                noteAdapter.setNotes(filtered)
            } else {
                noteAdapter.setNotes(allNotes)
                edtSearch.visibility = View.GONE
                searchBtn.visibility = View.VISIBLE
            }
        }
    }


}