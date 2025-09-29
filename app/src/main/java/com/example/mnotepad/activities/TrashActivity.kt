package com.example.mnotepad.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.R
import com.example.mnotepad.adapters.NoteAdapter
import com.example.mnotepad.databinding.ActivityTrashBinding
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.FileSAFHelper
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.NoteViewModel

class TrashActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrashBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var selectFolderLauncher: ActivityResultLauncher<Intent>

    private var selectedNotes: List<Pair<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initToolbar()
        setupRecyclerView()
        observeViewModel()
        initSelectFolderLauncher()
    }

    private fun initSelectFolderLauncher() {
        selectFolderLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val treeUri = result.data?.data
                if (treeUri != null) {
                    FileSAFHelper.exportSelectedNotesToTxt(this, treeUri, selectedNotes)
                    showToast("Export successfully!", this)
                }
            }
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            ::startToNoteDetail, {}, {}
        )
        binding.rvDeletedNotes.layoutManager = LinearLayoutManager(this)
        binding.rvDeletedNotes.adapter = noteAdapter
    }

    private fun observeViewModel() {
        noteViewModel.deletedNotes.observe(this, noteAdapter::submitList)
    }

    private fun startToNoteDetail(note: Note) {
        val intent = Intent(this, NoteDetailActivity::class.java).apply {
            putExtra(NOTE_DETAIL_OBJECT, note)
            putExtra(IS_EDITED_ACTION, true)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_trash_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navDeleteAll -> {
                noteViewModel.deleteAllNotes()
                showToast("All trashed notes deleted", applicationContext)
                true
            }

            R.id.navUndeleteAll -> {
                noteViewModel.undeleteAllNotes()
                showToast("All trashed notes restored", applicationContext)
                true
            }

            R.id.navExportAll -> {
                handleExportSelected()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleExportSelected() {
        val selected = noteAdapter.getSelectedNotes()
        if (selected.isEmpty()) return

        selectedNotes = selected.map { note ->
            note.title to note.content
        }

        val intent = FileSAFHelper.createDirectoryIntent()
        selectFolderLauncher.launch(intent)
    }
}
