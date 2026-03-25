package com.stephanmeijer.fileconverter.engine

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

private const val TAG = "PandocEngine"

data class ConversionResult(
    val outputBytes: ByteArray,
    val output: String,
    val warnings: List<String>,
    val error: String?,
)

data class PandocFormats(
    val inputFormats: List<String>,
    val outputFormats: List<String>,
)

@Serializable
private data class RawConversionResult(
    val stdoutBase64: String,
    val stderr: String,
    val warnings: String,
)

object PandocEngine {

    @Volatile private var initialized = false
    private val initMutex = Mutex()
    private var webView: WebView? = null
    private val json = Json { ignoreUnknownKeys = true }

    val isInitialized: Boolean get() = initialized

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun initialize(context: Context) {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return

            val initDeferred = CompletableDeferred<Unit>()

            withContext(Dispatchers.Main) {
                val assetLoader = WebViewAssetLoader.Builder()
                    .setDomain("appassets.androidplatform.net")
                    .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                    .build()

                val wv = WebView(context.applicationContext).apply {
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
                    }

                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onInitComplete(result: String) {
                                Log.i(TAG, "Pandoc initialized: $result")
                                initDeferred.complete(Unit)
                            }
                            @JavascriptInterface
                            fun onInitError(error: String) {
                                Log.e(TAG, "Pandoc init error: $error")
                                initDeferred.completeExceptionally(RuntimeException("Pandoc init failed: $error"))
                            }
                        },
                        "Android"
                    )

                    loadUrl("https://appassets.androidplatform.net/assets/pandoc_host.html")
                }
                webView = wv
            }

            initDeferred.await()
            initialized = true
            Log.i(TAG, "PandocEngine ready")
        }
    }

    suspend fun queryVersion(): String {
        requireInitialized()
        val raw = evalJs("""pandocQuery('{"query":"version"}')""")
        return json.decodeFromString<String>(raw)
    }

    suspend fun queryFormats(): PandocFormats {
        requireInitialized()
        val rawIn = evalJs("""pandocQuery('{"query":"input-formats"}')""")
        val rawOut = evalJs("""pandocQuery('{"query":"output-formats"}')""")
        return PandocFormats(
            inputFormats = json.decodeFromString(rawIn),
            outputFormats = json.decodeFromString(rawOut),
        )
    }

    suspend fun convert(
        inputBytes: ByteArray,
        fromFormat: String,
        toFormat: String,
    ): ConversionResult {
        requireInitialized()

        val escapedFrom = json.encodeToString(String.serializer(), fromFormat)
        val escapedTo = json.encodeToString(String.serializer(), toFormat)
        val optionsJson = """{"from":$escapedFrom,"to":$escapedTo,"standalone":true,"embed-resources":true,"input-files":["/stdin"]}"""

        val inputBase64 = Base64.encodeToString(inputBytes, Base64.NO_WRAP)
        val escapedOptions = optionsJson.replace("\\", "\\\\").replace("'", "\\'")

        val jsCode = """
            (function() {
                var b64 = '$inputBase64';
                var binary = atob(b64);
                var bytes = new Uint8Array(binary.length);
                for (var i = 0; i < binary.length; i++) { bytes[i] = binary.charCodeAt(i); }
                return pandocConvertBytes('$escapedOptions', bytes);
            })()
        """.trimIndent()

        val resultJson = evalJs(jsCode)
        val raw = json.decodeFromString<RawConversionResult>(resultJson)

        val warnings: List<String> = if (raw.warnings.isNotBlank() && raw.warnings != "[]") {
            try {
                @Serializable data class W(val message: String = "")
                json.decodeFromString<List<W>>(raw.warnings).map { it.message }
            } catch (_: Exception) {
                raw.warnings.lines().filter { it.isNotBlank() }
            }
        } else emptyList()

        val outputBytes = if (raw.stdoutBase64.isNotEmpty()) {
            Base64.decode(raw.stdoutBase64, Base64.DEFAULT)
        } else {
            ByteArray(0)
        }

        return ConversionResult(
            outputBytes = outputBytes,
            output = String(outputBytes, Charsets.UTF_8),
            warnings = warnings,
            error = raw.stderr.takeIf { it.isNotBlank() },
        )
    }

    private suspend fun evalJs(code: String): String = withContext(Dispatchers.Main) {
        val wv = webView
            ?: throw IllegalStateException("WebView has been destroyed — please restart the app")
        suspendCancellableCoroutine { cont ->
            try {
                wv.evaluateJavascript(code) { result ->
                    val value = if (result != null && result.startsWith("\"") && result.endsWith("\"")) {
                        json.decodeFromString<String>(result)
                    } else {
                        result ?: "null"
                    }
                    cont.resume(value)
                }
            } catch (e: Exception) {
                initialized = false
                cont.resume("")
            }
        }
    }

    fun close() {
        webView?.destroy()
        webView = null
        initialized = false
    }

    private fun requireInitialized() {
        check(initialized) { "PandocEngine.initialize() has not been called" }
    }
}
