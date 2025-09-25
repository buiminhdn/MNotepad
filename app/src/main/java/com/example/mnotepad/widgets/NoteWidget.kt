package com.example.mnotepad.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.example.mnotepad.R
import com.example.mnotepad.activities.MainActivity


/**
 * Implementation of App Widget functionality.
 */
class NoteWidget : AppWidgetProvider() {



    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val intent = Intent(context, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent)

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                /* context = */ context,
                /* requestCode = */  0,
                /* intent = */ Intent(context, MainActivity::class.java),
                /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.note_widget
            ).apply {
                setOnClickPendingIntent(R.id.layoutWidget, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        val title = intent!!.getStringExtra("Title")
        val content = intent.getStringExtra("Content")

        Toast.makeText(context, "voooo", Toast.LENGTH_SHORT).show()

        val views = RemoteViews(
            context!!.packageName,
            R.layout.note_widget
        )
//        if (intent!!.getAction().equals(UPDATE_WIDGET)) {
//            views.setImageViewResource(R.id.newImage, R.drawable.Pic1)
//        }

        views.setString(R.id.tvNoteTitle, ACTION_APPWIDGET_UPDATE, title)
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}