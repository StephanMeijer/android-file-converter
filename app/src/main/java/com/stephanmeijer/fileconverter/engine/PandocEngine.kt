package com.stephanmeijer.fileconverter.engine

import android.content.Context
import android.util.Log
import androidx.javascriptengine.IsolateStartupParameters
import androidx.javascriptengine.JavaScriptIsolate
import androidx.javascriptengine.JavaScriptSandbox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private const val TAG = "PandocEngine"
private const val HEAP_SIZE_BYTES: Long = 512L * 1024 * 1024

data class ConversionResult(
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
    val stdout: String,
    val stderr: String,
    // warnings is a JSON string nested inside the outer JSON (double-encoded)
    val warnings: String,
)

@Serializable
private data class PandocWarning(
    val message: String = "",
)

object PandocEngine {

    @Volatile
    private var initialized: Boolean = false

    private val initMutex = Mutex()

    private var sandbox: JavaScriptSandbox? = null
    private var isolate: JavaScriptIsolate? = null

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun initialize(context: Context) {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return
            withContext(Dispatchers.IO) {
                doInitialize(context)
            }
            initialized = true
        }
    }

    private suspend fun doInitialize(context: Context) {
        check(JavaScriptSandbox.isSupported()) {
            "JavaScriptSandbox is not supported on this device"
        }

        val sb = JavaScriptSandbox.createConnectedInstanceAsync(context).await()
        sandbox = sb

        check(sb.isFeatureSupported(JavaScriptSandbox.JS_FEATURE_WASM_COMPILATION)) {
            "Device does not support WASM compilation"
        }
        check(sb.isFeatureSupported(JavaScriptSandbox.JS_FEATURE_PROVIDE_CONSUME_ARRAY_BUFFER)) {
            "Device does not support provideNamedData / consumeNamedDataAsArrayBuffer"
        }
        check(sb.isFeatureSupported(JavaScriptSandbox.JS_FEATURE_PROMISE_RETURN)) {
            "Device does not support returning Promises from evaluateJavaScriptAsync"
        }

        val params = IsolateStartupParameters().apply {
            maxHeapSizeBytes = HEAP_SIZE_BYTES
        }
        val iso = sb.createIsolate(params)
        isolate = iso

        iso.addOnTerminatedCallback(
            java.util.concurrent.Executor { it.run() },
            androidx.core.util.Consumer { info ->
                Log.e(TAG, "JS isolate terminated: $info")
                initialized = false
                isolate = null
            },
        )

        val wasiShim = context.assets.open("wasi_shim.js").bufferedReader().use { it.readText() }
        iso.evaluateJavaScriptAsync(wasiShim).await()
        Log.d(TAG, "wasi_shim.js loaded")

        val bridge = context.assets.open("pandoc_bridge.js").bufferedReader().use { it.readText() }
        iso.evaluateJavaScriptAsync(bridge).await()
        Log.d(TAG, "pandoc_bridge.js loaded")

        val wasmBytes = context.assets.open("pandoc.wasm").use { it.readBytes() }
        iso.provideNamedData("pandoc-wasm", wasmBytes)
        Log.d(TAG, "pandoc.wasm provided (${wasmBytes.size} bytes)")

        // initPandoc() is async with no return value — wrap to get a value back
        iso.evaluateJavaScriptAsync(
            "(async () => { await initPandoc(); return 'ok'; })()"
        ).await()
        Log.i(TAG, "pandoc WASM runtime initialized")
    }

    suspend fun queryVersion(): String = withContext(Dispatchers.IO) {
        requireInitialized()
        val raw = isolate!!.evaluateJavaScriptAsync(
            """pandocQuery('{"query":"version"}')"""
        ).await()
        // pandocQuery returns JSON-encoded text: "3.9.0.2" (with quotes)
        json.decodeFromString<String>(raw)
    }

    suspend fun queryFormats(): PandocFormats = withContext(Dispatchers.IO) {
        requireInitialized()
        val rawIn = isolate!!.evaluateJavaScriptAsync(
            """pandocQuery('{"query":"input-formats"}')"""
        ).await()
        val rawOut = isolate!!.evaluateJavaScriptAsync(
            """pandocQuery('{"query":"output-formats"}')"""
        ).await()
        PandocFormats(
            inputFormats = json.decodeFromString<List<String>>(rawIn),
            outputFormats = json.decodeFromString<List<String>>(rawOut),
        )
    }

    suspend fun convert(
        input: String,
        fromFormat: String,
        toFormat: String,
        standalone: Boolean = false,
    ): ConversionResult = withContext(Dispatchers.IO) {
        requireInitialized()
        val iso = isolate!!

        val optionsJson = buildString {
            append("""{"from":""")
            append(json.encodeToString(String.serializer(), fromFormat))
            append(""","to":""")
            append(json.encodeToString(String.serializer(), toFormat))
            if (standalone) append(""","standalone":true""")
            append(""","files":["/stdin"]}""")
        }

        val escapedOptions = optionsJson
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")

        // provideNamedData avoids fragile JS string escaping for large input
        val inputBytes = input.toByteArray(Charsets.UTF_8)
        iso.provideNamedData("pandoc-stdin", inputBytes)

        val jsCode = """
            (function() {
                var inputBuf = android.consumeNamedDataAsArrayBuffer('pandoc-stdin');
                var inputStr = new TextDecoder().decode(inputBuf);
                return pandocConvert('$escapedOptions', inputStr);
            })()
        """.trimIndent()

        val resultJson = iso.evaluateJavaScriptAsync(jsCode).await()

        val raw = json.decodeFromString<RawConversionResult>(resultJson)

        val warnings: List<String> = if (raw.warnings.isNotBlank() && raw.warnings != "[]") {
            try {
                json.decodeFromString<List<PandocWarning>>(raw.warnings).map { it.message }
            } catch (_: Exception) {
                raw.warnings.lines().filter { it.isNotBlank() }
            }
        } else {
            emptyList()
        }

        ConversionResult(
            output = raw.stdout,
            warnings = warnings,
            error = raw.stderr.takeIf { it.isNotBlank() },
        )
    }

    fun close() {
        isolate?.close()
        sandbox?.close()
        isolate = null
        sandbox = null
        initialized = false
    }

    private fun requireInitialized() {
        check(initialized) { "PandocEngine.initialize() has not been called" }
    }
}
