package com.trungpd.formatdata

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class StorageUtils {
    companion object {

        private val TAG = StorageUtils::class.java.simpleName

        fun createFile(path: String, name: String, content: String): Boolean {
            val file = File(path, name)

            if (!file.exists()){
                Log.e("Trung", "file not exiest: " + file.path)
            } else {
                try {
                    file.createNewFile()

                } catch (e: IOException) {
                    Log.e("Trung", "cannot create file: e: " + e.message)
                    return false
                }
            }

            return createFile(file, content.toByteArray())
        }

        fun createFile(file: File, content: ByteArray): Boolean {
            if (!file.exists()){
                Log.e("Trung", "File k toan tai de write")
                return false

            }

            try {
                val stream: OutputStream = FileOutputStream(file)

                stream.write(content)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                Log.e("Trung", "Failed write file", e)
                return false
            }
            return true
        }

        fun createFile(path: String, content: ByteArray): Boolean {
            try {
                val stream: OutputStream = FileOutputStream(File(path))

                stream.write(content)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                Log.e("Trung", "Failed write file", e)
                return false
            }
            return true
        }

        fun deleteFile(path: String): Boolean {
            val file = File(path)
            if (!file.exists())
                return true

            return file.delete()
        }

        fun isFileExist(path: String): Boolean {
            return File(path).exists()
        }

        fun getPath(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )

            uri?.let {
                try {
                    cursor = context.getContentResolver().query(
                        it, projection, selection, selectionArgs,
                        null
                    )
                    if (cursor != null && cursor!!.moveToFirst()) {
                        val index: Int = cursor!!.getColumnIndexOrThrow(column)
                        return cursor!!.getString(index)
                    }
                } finally {
                    if (cursor != null) cursor!!.close()
                }
            }

            return null
        }


        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }
    }
}