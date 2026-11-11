package com.example.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object StorageHelper {
    /**
     * Copies a selected photo or video Uri into the app's internal persistent directory.
     * Returns the absolute file path (or file:// URI) so it never loses access permission.
     */
    suspend fun saveMediaToInternalStorage(context: Context, sourceUri: Uri, isVideo: Boolean): String =
        withContext(Dispatchers.IO) {
            try {
                val mediaDir = File(context.filesDir, "facebook_media").apply {
                    if (!exists()) mkdirs()
                }
                val extension = if (isVideo) "mp4" else "jpg"
                val destFile = File(mediaDir, "media_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension")
                
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream: InputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                destFile.absolutePath
            } catch (e: Exception) {
                // Fallback to Uri string if copy fails
                sourceUri.toString()
            }
        }
}
