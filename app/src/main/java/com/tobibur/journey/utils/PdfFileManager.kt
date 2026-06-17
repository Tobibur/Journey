package com.tobibur.journey.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PdfFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun savePdfToDownloads(pdfBytes: ByteArray): Uri? {
        val filename = generateFilename()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePdfWithMediaStore(pdfBytes, filename)
        } else {
            savePdfLegacy(pdfBytes, filename)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePdfWithMediaStore(pdfBytes: ByteArray, filename: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(pdfBytes)
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
    private fun savePdfLegacy(pdfBytes: ByteArray, filename: String): Uri? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, filename)

        return try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(pdfBytes)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    private fun generateFilename(): String {
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"))
        return "Journey_Export_$timestamp.pdf"
    }
}
