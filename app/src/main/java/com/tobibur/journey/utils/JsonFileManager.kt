package com.tobibur.journey.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.tobibur.journey.domain.model.JournalEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class JsonFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun generate(entries: List<JournalEntry>): ByteArray {
        val jsonArray = JSONArray()

        entries.forEach { entry ->
            val jsonObject = JSONObject().apply {
                put("id", entry.id)
                put("title", entry.title)
                put("content", entry.content)
                put("timestamp", entry.timestamp)
                put("date", formatTimestamp(entry.timestamp))
            }
            jsonArray.put(jsonObject)
        }

        val exportObject = JSONObject().apply {
            put("exportedAt", System.currentTimeMillis())
            put("exportedAtFormatted", formatTimestamp(System.currentTimeMillis()))
            put("totalEntries", entries.size)
            put("entries", jsonArray)
        }

        return exportObject.toString(2).toByteArray(Charsets.UTF_8)
    }

    fun saveJsonToDownloads(jsonBytes: ByteArray): Uri? {
        val filename = generateFilename()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveJsonWithMediaStore(jsonBytes, filename)
        } else {
            saveJsonLegacy(jsonBytes, filename)
        }
    }

    private fun saveJsonWithMediaStore(jsonBytes: ByteArray, filename: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonBytes)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun saveJsonLegacy(jsonBytes: ByteArray, filename: String): Uri? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, filename)

        return try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(jsonBytes)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    private fun generateFilename(): String {
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"))
        return "Journey_Export_$timestamp.json"
    }

    private fun formatTimestamp(timestamp: Long): String {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}