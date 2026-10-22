# AMIGOS Voice Lite (Version 1.0)

Zero-paid-API Android voice-translator prototype, with Hindi, Kannada, English, Tamil, Telugu, Marathi, Russian.

## What works
1. Choose source and target languages.
2. Tap microphone to launch Android's speech recognition.
3. On first translation the free Google ML Kit language models download (internet required).
4. Translated text displays and the phone's Text-to-Speech speaks it, if a voice is installed.
5. Replay translation and swap languages.

Speech recognition uses the phone's installed speech service and may require internet. Some Android devices lack voices for selected languages. ML Kit translations between two non-English languages may route via English, so quality must be tested before guest use. No account, ads or paid APIs.

## Free APK build with GitHub (no Android Studio or laptop required)

1. Create a FREE GitHub account: https://github.com/join
2. Create a **PUBLIC** repository named `amigos-voice-lite` (GitHub Actions free hosted minutes for public repositories, subject to GitHub terms and abuse limits). Don't add a README during creation.
3. Upload all files and folders from this ZIP into the repository **preserving the folder layout**, including `.github/workflows/build-apk.yml`. GitHub website upload may not retain hidden `.github` easily on mobile; if so, create the workflow in the GitHub web editor at that exact path.
4. Open the repository **Actions** tab. Choose "Build Free Android APK". If it did not run automatically, tap "Run workflow".
5. If the build succeeds, select the run → **Artifacts** → `AMIGOS-Voice-Lite-APK` and download the ZIP.
6. Unzip, copy `app-debug.apk` to the Android phone, allow installing from your file manager, and install. This is a debug-signed pilot APK, not a Play Store release.

Alternative on computer: open this project in Android Studio, wait for Gradle sync, and use Build → Build APK(s).

### Potential build issues
- Google Play speech recognition / Speech Services must be present on the phone.
- ML Kit downloads models over Wi-Fi/mobile data on first use; translations use on-device models after downloads.
- A system TTS voice for the target language must be installed to hear it; otherwise only translated text is shown.
- If GitHub's build reports an error, copy the red build log and send it back for correction.
- The code is a prototype and has **not** been compiled/tested in this environment (Android SDK and Gradle are not installed here).

### Security and privacy
- No user accounts or app-operated servers.
- The speech recognition service may process audio remotely depending on the phone's settings/provider; do not use for confidential staff or guest information without reviewing service terms.
- Do not assume all translations are perfectly accurate. Validate common restaurant phrases with native speakers.

## Sources
- https://developers.google.com/ml-kit/language/translation/android
- https://developer.android.com/reference/android/speech/RecognizerIntent
- https://developer.android.com/reference/android/speech/tts/TextToSpeech

Malayalam is not in the currently supported ML Kit on-device translation list; it requires a different translation engine in a future update.
