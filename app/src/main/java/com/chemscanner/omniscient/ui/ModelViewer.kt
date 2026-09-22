package com.chemscanner.omniscient.ui

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.chemscanner.omniscient.marrow.repository.AtmosphericParams // FIXED IMPORT
import com.google.gson.Gson
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

class ModelViewer(private val webView: WebView) {

    private var modelData: String? = null
    private val pageLoaded = CountDownLatch(1)
    private val gson = Gson()

    init {
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.addJavascriptInterface(this, "Android")
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pageLoaded.countDown()
            }
        }
        webView.loadUrl("file:///android_asset/model_viewer.html")
    }

    fun loadModelGltf(buffer: ByteBuffer) {
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        modelData = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

        Thread {
            try {
                pageLoaded.await()
                webView.post {
                    webView.evaluateJavascript("javascript:loadModelFromBase64('$modelData');", null)
                }
            } catch (e: InterruptedException) {
                Timber.e(e, "ModelViewer page loading thread interrupted")
            }
        }.start()
    }

    fun updateAtmosphere(params: AtmosphericParams) {
        val jsonParams = gson.toJson(params)
        webView.post {
            webView.evaluateJavascript("javascript:updateAtmosphericEnvironment('$jsonParams');", null)
        }
    }

    /**
     * UNIQUE: Controls the instability level (0.0 unstable to 1.0 fully stabilized)
     */
    fun setStabilityLevel(level: Float) {
        webView.post {
            webView.evaluateJavascript("javascript:setInstabilityLevel($level);", null)
        }
    }

    @JavascriptInterface
    fun getModelData(): String? {
        return modelData
    }

    fun destroy() {
        webView.destroy()
    }
}
