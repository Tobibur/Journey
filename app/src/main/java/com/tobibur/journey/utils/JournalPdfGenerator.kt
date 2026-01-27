package com.tobibur.journey.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.tobibur.journey.domain.model.JournalEntry
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class JournalPdfGenerator @Inject constructor() {

    companion object {
        // A4 size in points (72 points per inch)
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 72f
        private const val CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN)

        // Colors
        private const val PRIMARY_COLOR = 0xFF6750A4.toInt() // Material Purple
        private const val TEXT_COLOR = Color.BLACK
        private const val SECONDARY_TEXT_COLOR = 0xFF666666.toInt()

        // Font sizes
        private const val HEADER_TITLE_SIZE = 24f
        private const val HEADER_SUBTITLE_SIZE = 12f
        private const val DATE_HEADER_SIZE = 14f
        private const val ENTRY_TITLE_SIZE = 18f
        private const val ENTRY_CONTENT_SIZE = 12f
        private const val FOOTER_SIZE = 10f

        // Spacing
        private const val LINE_SPACING = 4f
        private const val PARAGRAPH_SPACING = 16f
        private const val ENTRY_SPACING = 24f
        private const val DATE_HEADER_PADDING = 8f
    }

    private val headerTitlePaint = Paint().apply {
        color = PRIMARY_COLOR
        textSize = HEADER_TITLE_SIZE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val headerSubtitlePaint = Paint().apply {
        color = SECONDARY_TEXT_COLOR
        textSize = HEADER_SUBTITLE_SIZE
        isAntiAlias = true
    }

    private val dateHeaderPaint = Paint().apply {
        color = Color.WHITE
        textSize = DATE_HEADER_SIZE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val dateHeaderBgPaint = Paint().apply {
        color = PRIMARY_COLOR
        style = Paint.Style.FILL
    }

    private val entryTitlePaint = Paint().apply {
        color = TEXT_COLOR
        textSize = ENTRY_TITLE_SIZE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val entryContentPaint = Paint().apply {
        color = TEXT_COLOR
        textSize = ENTRY_CONTENT_SIZE
        isAntiAlias = true
    }

    private val footerPaint = Paint().apply {
        color = SECONDARY_TEXT_COLOR
        textSize = FOOTER_SIZE
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = 0xFFE0E0E0.toInt()
        strokeWidth = 1f
    }

    fun generate(entries: List<JournalEntry>): ByteArray {
        val document = PdfDocument()
        var pageNumber = 1
        var currentY = MARGIN
        var canvas: Canvas
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        canvas = page.canvas

        // Draw header on first page
        currentY = drawHeader(canvas, currentY)

        // Sort entries by timestamp (newest first)
        val sortedEntries = entries.sortedByDescending { it.timestamp }

        for (entry in sortedEntries) {
            val entryHeight = calculateEntryHeight(entry)

            // Check if we need a new page
            if (currentY + entryHeight > PAGE_HEIGHT - MARGIN - 30) {
                drawFooter(canvas, pageNumber)
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN
            }

            currentY = drawEntry(canvas, entry, currentY)
            currentY += ENTRY_SPACING
        }

        drawFooter(canvas, pageNumber)
        document.finishPage(page)

        val outputStream = ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()

        return outputStream.toByteArray()
    }

    private fun drawHeader(canvas: Canvas, startY: Float): Float {
        var y = startY

        // App title
        canvas.drawText("JOURNEY", MARGIN, y + HEADER_TITLE_SIZE, headerTitlePaint)
        y += HEADER_TITLE_SIZE + LINE_SPACING

        // Subtitle
        canvas.drawText("Your Personal Journal", MARGIN, y + HEADER_SUBTITLE_SIZE, headerSubtitlePaint)
        y += HEADER_SUBTITLE_SIZE + LINE_SPACING

        // Export date
        val exportDate = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            .format(Instant.now().atZone(ZoneId.systemDefault()))
        canvas.drawText("Exported: $exportDate", MARGIN, y + HEADER_SUBTITLE_SIZE, headerSubtitlePaint)
        y += HEADER_SUBTITLE_SIZE + PARAGRAPH_SPACING

        // Divider line
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += PARAGRAPH_SPACING

        return y
    }

    private fun drawEntry(canvas: Canvas, entry: JournalEntry, startY: Float): Float {
        var y = startY

        // Date header with background
        val dateText = formatEntryDate(entry.timestamp)
        val dateHeaderHeight = DATE_HEADER_SIZE + (2 * DATE_HEADER_PADDING)
        canvas.drawRect(
            MARGIN,
            y,
            PAGE_WIDTH - MARGIN,
            y + dateHeaderHeight,
            dateHeaderBgPaint
        )
        canvas.drawText(
            dateText,
            MARGIN + DATE_HEADER_PADDING,
            y + DATE_HEADER_PADDING + DATE_HEADER_SIZE,
            dateHeaderPaint
        )
        y += dateHeaderHeight + PARAGRAPH_SPACING

        // Entry title
        val titleLines = wrapText(entry.title, entryTitlePaint, CONTENT_WIDTH)
        for (line in titleLines) {
            canvas.drawText(line, MARGIN, y + ENTRY_TITLE_SIZE, entryTitlePaint)
            y += ENTRY_TITLE_SIZE + LINE_SPACING
        }
        y += LINE_SPACING * 2

        // Entry content
        val contentLines = wrapText(entry.content, entryContentPaint, CONTENT_WIDTH)
        for (line in contentLines) {
            canvas.drawText(line, MARGIN, y + ENTRY_CONTENT_SIZE, entryContentPaint)
            y += ENTRY_CONTENT_SIZE + LINE_SPACING
        }

        return y
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int) {
        val pageText = "Page $pageNumber"
        val textWidth = footerPaint.measureText(pageText)
        canvas.drawText(
            pageText,
            (PAGE_WIDTH - textWidth) / 2,
            PAGE_HEIGHT - MARGIN / 2,
            footerPaint
        )
    }

    private fun calculateEntryHeight(entry: JournalEntry): Float {
        var height = 0f

        // Date header
        height += DATE_HEADER_SIZE + (2 * DATE_HEADER_PADDING) + PARAGRAPH_SPACING

        // Title
        val titleLines = wrapText(entry.title, entryTitlePaint, CONTENT_WIDTH)
        height += titleLines.size * (ENTRY_TITLE_SIZE + LINE_SPACING)
        height += LINE_SPACING * 2

        // Content
        val contentLines = wrapText(entry.content, entryContentPaint, CONTENT_WIDTH)
        height += contentLines.size * (ENTRY_CONTENT_SIZE + LINE_SPACING)

        return height
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("")
                continue
            }

            val words = paragraph.split(" ")
            var currentLine = StringBuilder()

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val testWidth = paint.measureText(testLine)

                if (testWidth <= maxWidth) {
                    currentLine = StringBuilder(testLine)
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine.toString())
                    }
                    // Handle words longer than line width
                    if (paint.measureText(word) > maxWidth) {
                        var remaining = word
                        while (remaining.isNotEmpty()) {
                            var end = remaining.length
                            while (end > 0 && paint.measureText(remaining.substring(0, end)) > maxWidth) {
                                end--
                            }
                            if (end == 0) end = 1
                            lines.add(remaining.substring(0, end))
                            remaining = remaining.substring(end)
                        }
                        currentLine = StringBuilder()
                    } else {
                        currentLine = StringBuilder(word)
                    }
                }
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
            }
        }

        return if (lines.isEmpty()) listOf("") else lines
    }

    private fun formatEntryDate(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}
