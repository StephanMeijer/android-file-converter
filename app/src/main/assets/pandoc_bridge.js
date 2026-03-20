/* pandoc_bridge.js: V8 headless bridge to pandoc.wasm for Jetpack JavaScriptEngine.
   Adapted from pandoc.js (C) 2025 Tweag I/O Limited and John MacFarlane. MIT License.

   Prerequisites: wasi_shim.js must be loaded first (provides WASI, OpenFile, File,
   ConsoleStdout, PreopenDirectory, WASIProcExit as globals).

   Usage from Kotlin/JavaScriptEngine:
     1. Provide WASM binary via android.consumeNamedDataAsArrayBuffer('pandoc-wasm')
     2. Call initPandoc() — async, initializes WASM + Haskell RTS
     3. Call pandocQuery(jsonStr) — sync, returns JSON string
     4. Call pandocConvert(optionsJson, stdinStr) — sync, returns JSON string
*/

var _instance = null;
var _fileSystem = null;

function _memoryDataView() {
  return new DataView(_instance.exports.memory.buffer);
}

/**
 * Initialize pandoc WASM runtime.
 * Must be called once before pandocQuery/pandocConvert.
 */
async function initPandoc() {
  var args = ["pandoc.wasm", "+RTS", "-H64m", "-RTS"];
  var env = [];

  _fileSystem = new Map();

  var fds = [
    new OpenFile(new File(new Uint8Array(), { readonly: true })),
    ConsoleStdout.lineBuffered(function(msg) { /* stdout sink */ }),
    ConsoleStdout.lineBuffered(function(msg) { /* stderr sink */ }),
    new PreopenDirectory("/", _fileSystem),
  ];

  var options = { debug: false };
  var wasi = new WASI(args, env, fds, options);

  // Load WASM via fetch — works in WebView with WebViewAssetLoader (full Web API)
  var result = await WebAssembly.instantiateStreaming(fetch('pandoc.wasm'), {
    wasi_snapshot_preview1: wasi.wasiImport,
  });

  _instance = result.instance;

  wasi.initialize(_instance);
  _instance.exports.__wasm_call_ctors();

  // Build argc/argv in WASM memory and initialize Haskell RTS
  var argc_ptr = _instance.exports.malloc(4);
  _memoryDataView().setUint32(argc_ptr, args.length, true);

  var argv = _instance.exports.malloc(4 * (args.length + 1));
  for (var i = 0; i < args.length; ++i) {
    var arg = _instance.exports.malloc(args[i].length + 1);
    new TextEncoder().encodeInto(
      args[i],
      new Uint8Array(_instance.exports.memory.buffer, arg, args[i].length)
    );
    _memoryDataView().setUint8(arg + args[i].length, 0);
    _memoryDataView().setUint32(argv + 4 * i, arg, true);
  }
  _memoryDataView().setUint32(argv + 4 * args.length, 0, true);

  var argv_ptr = _instance.exports.malloc(4);
  _memoryDataView().setUint32(argv_ptr, argv, true);

  _instance.exports.hs_init_with_rtsopts(argc_ptr, argv_ptr);
}

/**
 * Query pandoc for metadata (version, formats, etc.)
 * @param {string} jsonStr - JSON string with query options, e.g. '{"query":"version"}'
 * @returns {string} JSON string with query result
 */
function pandocQuery(jsonStr) {
  var opts_bytes = new TextEncoder().encode(jsonStr);
  var opts_ptr = _instance.exports.malloc(opts_bytes.length);
  new Uint8Array(_instance.exports.memory.buffer, opts_ptr, opts_bytes.length)
    .set(opts_bytes);

  // Set up virtual filesystem for output capture
  _fileSystem.clear();
  var out_file = new File(new Uint8Array(), { readonly: false });
  var err_file = new File(new Uint8Array(), { readonly: false });
  _fileSystem.set("stdout", out_file);
  _fileSystem.set("stderr", err_file);

  try {
    _instance.exports.query(opts_ptr, opts_bytes.length);
  } catch (e) {
    if (e instanceof WASIProcExit && e.code === 0) {
      // Normal exit via proc_exit(0) — not an error
    } else if (e instanceof WASIProcExit) {
      // Non-zero exit — include stderr in result
      var errText = new TextDecoder("utf-8", { fatal: false }).decode(err_file.data);
      return JSON.stringify({ error: "pandoc exited with code " + e.code, stderr: errText });
    } else {
      throw e;
    }
  }

  var err_text = new TextDecoder("utf-8", { fatal: false }).decode(err_file.data);
  var out_text = new TextDecoder("utf-8", { fatal: false }).decode(out_file.data);
  return out_text;
}

/**
 * Convert content using pandoc.
 * @param {string} optionsJson - JSON string with pandoc conversion options
 * @param {string} [stdinStr] - Optional input content string
 * @returns {string} JSON string with {stdout, stderr, warnings}
 */
function pandocConvert(optionsJson, stdinStr) {
  var opts_bytes = new TextEncoder().encode(optionsJson);
  var opts_ptr = _instance.exports.malloc(opts_bytes.length);
  new Uint8Array(_instance.exports.memory.buffer, opts_ptr, opts_bytes.length)
    .set(opts_bytes);

  // Set up virtual filesystem
  _fileSystem.clear();
  var in_file = new File(new Uint8Array(), { readonly: true });
  var out_file = new File(new Uint8Array(), { readonly: false });
  var err_file = new File(new Uint8Array(), { readonly: false });
  var warnings_file = new File(new Uint8Array(), { readonly: false });
  _fileSystem.set("stdin", in_file);
  _fileSystem.set("stdout", out_file);
  _fileSystem.set("stderr", err_file);
  _fileSystem.set("warnings", warnings_file);

  // Write input content to virtual stdin if provided
  if (stdinStr) {
    in_file.data = new TextEncoder().encode(stdinStr);
  }

  try {
    _instance.exports.convert(opts_ptr, opts_bytes.length);
  } catch (e) {
    if (e instanceof WASIProcExit && e.code === 0) {
      // Normal exit — continue to read output
    } else if (e instanceof WASIProcExit) {
      // Non-zero exit — still return what we have
      var errText = new TextDecoder("utf-8", { fatal: false }).decode(err_file.data);
      return JSON.stringify({
        stdout: "",
        stderr: errText,
        warnings: "[]",
        exitCode: e.code
      });
    } else {
      throw e;
    }
  }

  var stdout = new TextDecoder("utf-8", { fatal: false }).decode(out_file.data);
  var stderr = new TextDecoder("utf-8", { fatal: false }).decode(err_file.data);
  var rawWarnings = new TextDecoder("utf-8", { fatal: false }).decode(warnings_file.data);

  var warnings = "[]";
  if (rawWarnings && rawWarnings.length > 0) {
    warnings = rawWarnings;
  }

  return JSON.stringify({
    stdout: stdout,
    stderr: stderr,
    warnings: warnings
  });
}

/**
 * Convert content using pandoc with raw bytes input.
 * @param {string} optionsJson - JSON string with pandoc conversion options
 * @param {Uint8Array} inputBytes - Raw input bytes (for binary formats)
 * @returns {string} JSON string with {stdout, stderr, warnings}
 */
function pandocConvertBytes(optionsJson, inputBytes) {
  var opts_bytes = new TextEncoder().encode(optionsJson);
  var opts_ptr = _instance.exports.malloc(opts_bytes.length);
  new Uint8Array(_instance.exports.memory.buffer, opts_ptr, opts_bytes.length)
    .set(opts_bytes);

  _fileSystem.clear();
  var in_file = new File(new Uint8Array(), { readonly: true });
  var out_file = new File(new Uint8Array(), { readonly: false });
  var err_file = new File(new Uint8Array(), { readonly: false });
  var warnings_file = new File(new Uint8Array(), { readonly: false });
  _fileSystem.set("stdin", in_file);
  _fileSystem.set("stdout", out_file);
  _fileSystem.set("stderr", err_file);
  _fileSystem.set("warnings", warnings_file);

  if (inputBytes) {
    in_file.data = inputBytes;
  }

  try {
    _instance.exports.convert(opts_ptr, opts_bytes.length);
  } catch (e) {
    if (e instanceof WASIProcExit && e.code === 0) {
    } else if (e instanceof WASIProcExit) {
      var errText = new TextDecoder("utf-8", { fatal: false }).decode(err_file.data);
      return JSON.stringify({
        stdout: "",
        stderr: errText,
        warnings: "[]",
        exitCode: e.code
      });
    } else {
      throw e;
    }
  }

  var stdout = new TextDecoder("utf-8", { fatal: false }).decode(out_file.data);
  var stderr = new TextDecoder("utf-8", { fatal: false }).decode(err_file.data);
  var rawWarnings = new TextDecoder("utf-8", { fatal: false }).decode(warnings_file.data);

  var warnings = "[]";
  if (rawWarnings && rawWarnings.length > 0) {
    warnings = rawWarnings;
  }

  return JSON.stringify({
    stdout: stdout,
    stderr: stderr,
    warnings: warnings
  });
}
