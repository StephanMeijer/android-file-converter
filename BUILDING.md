# Building Android File Converter

## Prerequisites

- Node.js (v18+) and npm
- wget and unzip

## Downloading pandoc.wasm

The `pandoc.wasm` binary is not checked into version control due to its size (~55MB).
Download it from the pandoc GitHub releases:

```bash
cd /tmp
wget -q https://github.com/jgm/pandoc/releases/download/3.9.0.2/pandoc.wasm.zip
unzip -o pandoc.wasm.zip
cp pandoc.wasm <project-root>/app/src/main/assets/pandoc.wasm
```

The expected version is recorded in `app/src/main/assets/PANDOC_VERSION.txt`.

## Bundling the WASI shim

The `wasi_shim.js` file is an IIFE bundle of `@bjorn3/browser_wasi_shim@0.3.0`.
To regenerate it:

```bash
cd /tmp
npm pack @bjorn3/browser_wasi_shim@0.3.0
mkdir -p wasi_extract && tar xzf bjorn3-browser_wasi_shim-0.3.0.tgz -C wasi_extract
npx esbuild --bundle wasi_extract/package/dist/index.js \
  --format=iife --global-name=WasiShim --target=es2020 \
  --outfile=wasi_shim_bundle.js
```

Then append global aliases to the bundle:

```javascript
var WASI = WasiShim.WASI;
var WASIProcExit = WasiShim.WASIProcExit;
var OpenFile = WasiShim.OpenFile;
var File = WasiShim.File;
var ConsoleStdout = WasiShim.ConsoleStdout;
var PreopenDirectory = WasiShim.PreopenDirectory;
```

Copy the result to `app/src/main/assets/wasi_shim.js`.

## Asset file placement

All files must be in `app/src/main/assets/`:

| File | Description | Git-tracked |
|------|-------------|-------------|
| `pandoc.wasm` | Pandoc WebAssembly binary (~55MB) | No |
| `wasi_shim.js` | IIFE-bundled WASI shim (~62KB) | Yes |
| `pandoc_bridge.js` | V8 bridge adapting pandoc.js for JavaScriptEngine | Yes |
| `PANDOC_VERSION.txt` | Pandoc version string | Yes |

## Updating pandoc version

1. Download the new `pandoc.wasm.zip` from https://github.com/jgm/pandoc/releases
2. Place `pandoc.wasm` in `app/src/main/assets/`
3. Update `app/src/main/assets/PANDOC_VERSION.txt`
