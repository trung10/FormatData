package com.trungpd.formatdata

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        openDirectory()

        //checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openDirectory()
        } else {
            finish()
        }
    }

    fun openDirectory() {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when it loads.
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            data?.data?.also { uri ->
                Log.e("Trung", uri.toString())
                if (createfiles(Environment.getExternalStorageDirectory().path /*+ "/Downloads/realease"*/)) {
                    Log.e("Trung", "Success")

                } else {
                    Log.e("Trung", "failed")

                }

                // Perform operations on the document using its URI.
            }
        }
    }

    fun createfiles(path: String?): Boolean{

        val content = "hello data"
        val listFile = listOf<String>("file_1", "file_2")

        if (path.isNullOrEmpty()) {
            Log.e("Trung", "path empty")
        } else {
            Log.e("Trung", "path $path")

            for (i in listFile.indices) {
                if (!StorageUtils.createFile(path, listFile[i] + ".txt", content)) {

                    for (j in 0 .. i) {
                        StorageUtils.deleteFile(path + "/${listFile[j]}.txt")
                    }

                    return false
                }
            }
        }

        return true
    }




}