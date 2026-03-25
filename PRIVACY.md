# Privacy Policy

**Effective Date: March 25, 2026**

## Overview

This privacy policy describes how the Android File Converter app ("the App") handles your data. Our commitment is simple: **we do not collect, store, or transmit any of your personal data**.

## What We Do NOT Collect

The App does **not** collect or access:

- **Location data** — We never request or use your location
- **Contact information** — We don't access your contacts, phone number, or email
- **Usage analytics** — We don't track how you use the App
- **Crash reports** — We don't send diagnostic data to any server
- **Device identifiers** — We don't collect device IDs, advertising IDs, or similar identifiers
- **Files or documents** — Your files are never uploaded, stored on our servers, or transmitted over the network

## How Your Files Are Processed

All document conversion happens **entirely on your device** using WebAssembly (WASM). When you convert a file:

1. The file is read from your device storage
2. Conversion is performed locally using the Pandoc WASM engine
3. The converted file is saved to your device
4. **No data leaves your device at any point**

The App has no internet permission and makes no network requests.

## Permissions

The App requests only the `FOREGROUND_SERVICE` permission to display a notification during file conversion. It does **not** request:

- Internet access
- Location access
- Contact access
- Camera or microphone access
- Call logs or SMS access

## Changes to This Policy

We may update this policy occasionally. Changes will be posted here with an updated effective date.

## Contact

If you have questions about this privacy policy, please contact us at **[contact@example.com]**.

---

**Your privacy is our priority. All processing happens on your device.**
