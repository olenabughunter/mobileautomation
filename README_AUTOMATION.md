Mobile Automation - Local Android Test Setup

Overview
This README documents how to run the existing Appium-based Android tests locally using the Maven project at mobile-automation/EurodreamsTests. It lists prerequisites, how to start an emulator and Appium server, how to run the sample test, and useful troubleshooting commands.

Prerequisites
- macOS with Homebrew (optional)
- Java (OpenJDK 11+)
- Maven
- Android SDK (platform-tools, build-tools, emulator, platform SDK)
- An Android AVD (emulator) or a connected device
- Appium server (npm: appium) or Appium Desktop
- adb available on PATH

Environment variables (examples)
- ANDROID_SDK_ROOT=/Users/olena.imfeld/Library/Android/sdk
- PATH should include: $ANDROID_SDK_ROOT/platform-tools and $ANDROID_SDK_ROOT/emulator

Start an emulator
- From Android Studio: AVD Manager -> Start AVD
- From CLI: emulator -avd <AVD_NAME>
- Verify device: adb devices

Start Appium server
- Using npm: npx appium
- Or Appium Desktop: start server via GUI
- Default Appium URL used by tests: http://127.0.0.1:4723/

Scripts
- A helper script is available at scripts/manage_emulator_appium.sh
  - Start emulator + Appium: ./scripts/manage_emulator_appium.sh start
  - Stop services: ./scripts/manage_emulator_appium.sh stop
  - Status: ./scripts/manage_emulator_appium.sh status
- Make it executable before use:
  chmod +x scripts/manage_emulator_appium.sh

Run the sample test
1. Open a terminal in: mobile-automation/EurodreamsTests
2. Ensure emulator is running and visible via adb devices (e.g. emulator-5554)
3. Start Appium server (or use the script)
4. Run the Maven test:
   mvn test -Dtest=SimpleAppiumTestIT

Where tests are
- Test file: src/test/java/com/example/eurodreamstests/SimpleAppiumTestIT.java
  - It uses UiAutomator2 and currently points to the Android Settings app (com.android.settings/.Settings) as a safe sanity check.
  - Capabilities set: udid=emulator-5554, adbExecTimeout=120s, newCommandTimeout=300s
- To test an app already on device, update setAppPackage and setAppActivity or setApp to an .apk path.

Capture UI hierarchy and find locators
- Dump UI using adb:
  adb shell uiautomator dump /sdcard/ui.xml
  adb pull /sdcard/ui.xml
- Use Appium Inspector (or UIAutomatorViewer) to inspect element attributes and accessibility ids.

Troubleshooting
- If Appium cannot create a session, verify Appium logs and that adb recognizes the device.
- If elements are not found, capture a fresh UI dump and increase explicit wait times in tests.
- Use: adb logcat to view device logs.

Next steps (recommended)
- Replace the sanity check with real app interactions once reliable locators are identified.
- Add explicit waits and retry logic.
- Create scripts to start/stop Appium and select devices automatically.

Contact
If you want, I can: capture a UI dump now, run a more advanced test, or create an AVD. Tell me which and I will proceed.
