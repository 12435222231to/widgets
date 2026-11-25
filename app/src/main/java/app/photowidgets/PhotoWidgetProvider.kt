package app.photowidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.RemoteViews
import java.io.InputStream
import kotlin.math.max

class PhotoWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PIN_WIDGET) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val photoUri = intent.getStringExtra(EXTRA_PHOTO_URI)
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && photoUri != null) {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                    .putString(photoKey(widgetId), photoUri).apply()
                updateWidget(context, widgetId)
            }
        }
        super.onReceive(context, intent)
    }
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { updateWidget(context, it) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            appWidgetIds.forEach { remove(photoKey(it)) }
        }.apply()
    }

    companion object {
        const val PREFS_NAME = "photo_widget_settings"
        const val ACTION_PIN_WIDGET = "app.photowidgets.ACTION_PIN_WIDGET"
        const val EXTRA_PHOTO_URI = "extra_photo_uri"
        fun photoKey(widgetId: Int) = "photo_$widgetId"

        fun updateWidget(context: Context, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_photo)
            val uriString = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(photoKey(widgetId), null)

            val bitmap = uriString?.let { decodeWidgetBitmap(context, Uri.parse(it)) }
            if (bitmap != null) {
                views.setImageViewBitmap(R.id.widget_image, bitmap)
                views.setViewVisibility(R.id.widget_image, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.widget_placeholder, android.view.View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_image, android.view.View.GONE)
                views.setViewVisibility(R.id.widget_placeholder, android.view.View.VISIBLE)
            }

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
        }

        private fun decodeWidgetBitmap(context: Context, uri: Uri): Bitmap? {
            return try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

                val sample = max(1, max(bounds.outWidth, bounds.outHeight) / 1200)
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                val stream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
                stream.use { BitmapFactory.decodeStream(it, null, options) }
            } catch (_: Exception) {
                null
            }
        }
    }
}
