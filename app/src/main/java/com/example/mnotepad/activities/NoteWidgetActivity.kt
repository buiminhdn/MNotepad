package com.example.mnotepad.activities

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.R
import com.example.mnotepad.adapters.NoteAdapter
import com.example.mnotepad.databinding.ActivityMainBinding
import com.example.mnotepad.databinding.ActivityNoteWidgetBinding
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.ThemeManager.applyTheme
import com.example.mnotepad.viewmodels.NoteViewModel
import com.example.mnotepad.widgets.NoteWidget
import kotlin.getValue

class NoteWidgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteWidgetBinding
    private lateinit var noteAdapter: NoteAdapter
    private val noteViewModel: NoteViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNoteWidgetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
//        setupRecyclerView()
//        setupObservers()

        val startedFromWidget = intent?.getBooleanExtra("from_widget", false) ?: false
        val widgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (startedFromWidget && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            noteAdapter = NoteAdapter(
                emptyList(), { note -> sendNoteToWidget(widgetId, note) }, {}, {}
            )
            binding.rvNotes.layoutManager = LinearLayoutManager(this)
            binding.rvNotes.adapter = adapter()

            noteViewModel.filteredNotes.observe(this) { notes ->
                noteAdapter.setNotes(notes)
            }
        }
    }

    private fun sendNoteToWidget(appWidgetId: Int, note: Note) {
        val i = Intent(this, NoteWidget::class.java).apply {
            action = NoteWidget.ACTION_SET_NOTE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(NoteWidget.EXTRA_NOTE_ID, note.id)
        }
        sendBroadcast(i)
        finishAffinity()
    }


//    private fun setupObservers() {
//        noteViewModel.filteredNotes.observe(this) { notes ->
//            noteAdapter.setNotes(notes)
//        }
//    }
//
//    private fun setupRecyclerView() {
//        noteAdapter = NoteAdapter(
//            emptyList(), {}, {}, {}
//        )
//        binding.rvNotes.layoutManager = LinearLayoutManager(this)
//        binding.rvNotes.adapter = noteAdapter
//    }

    private fun adapter(): NoteAdapter = noteAdapter

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }
}

