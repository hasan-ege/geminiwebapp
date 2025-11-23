package com.bozkurt.geminiwebapp

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var photoURI: Uri? = null
    private var currentPhotoPath: String? = null

    // Kamera izni isteyici
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Kamera izni verilmediği için fotoğraf çekilemiyor.", Toast.LENGTH_SHORT).show()
        }
    }

    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (uploadMessage == null) return@registerForActivityResult

        var results: Array<Uri>? = null

        if (result.resultCode == RESULT_OK) {
            // Eğer data null ise muhtemelen kamera kullanılmıştır
            if (result.data == null || result.data?.dataString == null) {
                if (currentPhotoPath != null) {
                    val file = File(currentPhotoPath!!)
                    if (file.exists() && file.length() > 0) {
                        results = arrayOf(Uri.fromFile(file))
                    }
                }
            } else {
                // Galeri seçimi
                val dataString = result.data?.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }

        uploadMessage?.onReceiveValue(results)
        uploadMessage = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Açılışta Kamera İzni Kontrolü (Opsiyonel ama iyi olur)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        val webView = findViewById<WebView>(R.id.webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            databaseEnabled = true
        }

        webView.webViewClient = WebViewClient()

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (uploadMessage != null) {
                    uploadMessage?.onReceiveValue(null)
                    uploadMessage = null
                }
                uploadMessage = filePathCallback

                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                try {
                    val photoFile: File? = createImageFile()
                    if (photoFile != null) {
                        photoURI = FileProvider.getUriForFile(
                            this@MainActivity,
                            "${applicationContext.packageName}.provider",
                            photoFile
                        )

                        takePictureIntent?.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                        // KRİTİK DÜZELTME: İzin bayrakları ve ClipData
                        takePictureIntent?.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            takePictureIntent?.clipData = ClipData.newRawUri("", photoURI)
                        }
                    } else {
                        takePictureIntent = null
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    takePictureIntent = null
                }

                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val extraMimeTypes = arrayOf("image/*", "video/*", "application/pdf")
                contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Fotoğraf Çek veya Dosya Seç")

                if (takePictureIntent != null) {
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePictureIntent))
                }

                try {
                    fileChooserLauncher.launch(chooserIntent)
                } catch (e: Exception) {
                    uploadMessage?.onReceiveValue(null)
                    uploadMessage = null
                    return false
                }

                return true
            }
        }

        webView.loadUrl("https://gemini.google.com")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        // Cache dizinini kullanmak bazen izin sorunlarını çözer
        val storageDir = externalCacheDir ?: cacheDir
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = image.absolutePath
        return image
    }
}