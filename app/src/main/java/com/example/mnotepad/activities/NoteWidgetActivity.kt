package com.example.mnotepad.activities

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.RemoteViews
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.R
import com.example.mnotepad.adapters.NoteAdapter
import com.example.mnotepad.databinding.ActivityNoteWidgetBinding
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.PREFS_NAME
import com.example.mnotepad.helpers.ThemeManager.applyTheme
import com.example.mnotepad.viewmodels.NoteViewModel
import com.example.mnotepad.widgets.NoteWidget
import com.example.mnotepad.widgets.NoteWidget.Companion.updateAppWidget


class NoteWidgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteWidgetBinding
    private lateinit var noteAdapter: NoteAdapter
    private val noteViewModel: NoteViewModel by viewModels()

    private lateinit var widgetManager : AppWidgetManager
    private lateinit var views: RemoteViews
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

        widgetManager = AppWidgetManager.getInstance(this)
        views = RemoteViews(this.packageName, R.layout.note_widget)

        val widgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            noteAdapter = NoteAdapter(
                { note -> sendNoteToWidget(widgetId, note) }, {}, {}
            )
            binding.rvNotes.layoutManager = LinearLayoutManager(this)
            binding.rvNotes.adapter = noteAdapter

            noteViewModel.filteredNotes.observe(this) { notes ->
                noteAdapter.submitList(notes)
            }
        }
    }

    private fun sendNoteToWidget(appWidgetId: Int, note: Note) {
//        views = RemoteViews(packageName, R.layout.note_widget)
//        views.setTextViewText(R.id.tvNoteTitle, note?.title ?: "No title")
//        views.setTextViewText(
//            R.id.tvNoteContent,
//            Html.fromHtml(note?.content ?: "", Html.FROM_HTML_MODE_LEGACY).toString()
//        )
//        widgetManager.updateAppWidget(appWidgetId, views);
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt("widget_$appWidgetId", note.id)
        }

        updateAppWidget(this, AppWidgetManager.getInstance(this), appWidgetId)

        val resultValue = Intent()

//        resultValue.action = NoteWidget.ACTION_SET_NOTE
//        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
//        resultValue.putExtra(NoteWidget.EXTRA_NOTE_ID, note.id)
        setResult(RESULT_OK, resultValue)
        finish()
//        val i = Intent(this, NoteWidget::class.java).apply {
//            action = NoteWidget.ACTION_SET_NOTE
//            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
//            putExtra(NoteWidget.EXTRA_NOTE_ID, note.id)
//        }
//        sendBroadcast(i)
//        finish()
//        finishAffinity()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }
}
