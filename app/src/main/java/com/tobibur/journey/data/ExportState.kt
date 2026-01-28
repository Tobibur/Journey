package com.tobibur.journey.data

import android.net.Uri

sealed class ExportState {
    data class Success(val uri: Uri, val entryCount: Int, val exportType: ExportType) :
        ExportState()
    data object NoEntries : ExportState()
    data class Error(val message: String) : ExportState()
}

enum class ExportType {
    PDF, JSON
}