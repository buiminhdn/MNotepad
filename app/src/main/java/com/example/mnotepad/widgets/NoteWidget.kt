package com.example.mnotepad.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.Html
import android.widget.RemoteViews
import android.widget.Toast
import com.example.mnotepad.R
import com.example.mnotepad.activities.MainActivity
import com.example.mnotepad.activities.NoteWidgetActivity
import androidx.core.content.edit
import com.example.mnotepad.activities.NoteDetailActivity
import com.example.mnotepad.database.AppDatabase
import com.example.mnotepad.helpers.IS_EDITED_ACTION
import com.example.mnotepad.helpers.NOTE_DETAIL_OBJECT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Implementation of App Widget functionality.
 */
class NoteWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_SET_NOTE = "com.example.mnotepad.ACTION_SET_NOTE"
        const val ACTION_NOTE_CHANGED = "com.example.mnotepad.ACTION_NOTE_CHANGED"
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val PREFS_NAME = "com.example.mnotepad.NoteWidgetPrefs"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val noteId = prefs.getInt("widget_$appWidgetId", -1)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val note = if (noteId != -1) {
                    db.getNoteDao().getNoteByIdSync(noteId)
                } else null

                val views = RemoteViews(context.packageName, R.layout.note_widget)
                views.setTextViewText(R.id.tvNoteTitle, note?.title ?: "No title")
                views.setTextViewText(
                    R.id.tvNoteContent,
                    Html.fromHtml(note?.content ?: "", Html.FROM_HTML_MODE_LEGACY).toString()
                )

                val intent = if (noteId != -1) {
                    Intent(context, NoteDetailActivity::class.java).apply {
                        putExtra(NOTE_DETAIL_OBJECT, note)
                        putExtra(IS_EDITED_ACTION, true)
                    }
                } else {
                    Intent(context, NoteWidgetActivity::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        putExtra("from_widget", true)
                    }
                }

                val pendingIntent = PendingIntent.getActivity(
                    context, appWidgetId, intent,
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
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context == null || intent == null) return

        when (intent.action) {
            ACTION_SET_NOTE -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                val noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1)

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && noteId != -1) {
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit {
                        putInt("widget_$appWidgetId", noteId)
                    }

                    val manager = AppWidgetManager.getInstance(context)
                    updateAppWidget(context, manager, appWidgetId)
                }
            }

            ACTION_NOTE_CHANGED -> {
                val noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1)
                if (noteId != -1) {
                    val manager = AppWidgetManager.getInstance(context)
                    val cn = ComponentName(context, NoteWidget::class.java)
                    val allWidgetIds = manager.getAppWidgetIds(cn)
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

                    allWidgetIds.forEach { wid ->
                        val saved = prefs.getInt("widget_$wid", -1)
                        if (saved == noteId) {
                            updateAppWidget(context, manager, wid)
                        }
                    }
                }
            }

            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {}
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