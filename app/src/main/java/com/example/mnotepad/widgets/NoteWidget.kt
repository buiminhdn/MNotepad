package com.example.mnotepad.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.Html
import android.widget.RemoteViews
import androidx.core.content.edit
import com.example.mnotepad.R
import com.example.mnotepad.activities.NoteDetailActivity
import com.example.mnotepad.database.NoteDatabase
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import com.example.mnotepad.helpers.PREFS_NAME
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NoteWidget : AppWidgetProvider() {

    @Inject
    lateinit var noteDatabase: NoteDatabase

    companion object {
        const val ACTION_NOTE_CHANGED = "ACTION_NOTE_CHANGED"
        const val EXTRA_NOTE_ID = "extra_note_id"

        fun updateAppWidget(
            context: Context,
            noteDatabase: NoteDatabase,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val noteId = prefs.getInt("widget_$appWidgetId", -1)

            CoroutineScope(Dispatchers.IO).launch {
                val note = if (noteId != -1) {
                    noteDatabase.getNoteDao().getNoteByIdSync(noteId)
                } else {
                    null
                }

                val views = RemoteViews(context.packageName, R.layout.note_widget)
                views.setTextViewText(R.id.tvNoteTitle, note?.title ?: "No title")
                views.setTextViewText(
                    R.id.tvNoteContent,
                    Html.fromHtml(note?.content ?: "", Html.FROM_HTML_MODE_LEGACY).toString()
                )
                val intent = Intent(context, NoteDetailActivity::class.java).apply {
                    putExtra(NOTE_DETAIL_OBJECT, note)
                    putExtra(IS_EDITED_ACTION, true)
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.layoutWidget, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, noteDatabase, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context == null || intent == null) return

        when (intent.action) {
            ACTION_NOTE_CHANGED -> {
                val noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1)
                if (noteId != -1) {
                    val widgetManager = AppWidgetManager.getInstance(context)
                    val component = ComponentName(context, NoteWidget::class.java)
                    val allWidgetIds = widgetManager.getAppWidgetIds(component)
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

                    allWidgetIds.forEach { wid ->
                        val saved = prefs.getInt("widget_$wid", -1)
                        if (saved == noteId) {
                            updateAppWidget(context, noteDatabase, widgetManager, wid)
                        }
                    }
                }
            }
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs?.edit {
            appWidgetIds?.forEach { wid ->
                remove("widget_$wid")
            }
        }
        super.onDeleted(context, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
