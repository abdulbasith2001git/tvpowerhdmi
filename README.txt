TV POWER  -  power-off button with countdown + auto HDMI input on wake
=====================================================================

WHAT IT DOES
- Big "Power Off" button. Press it, a large 3-2-1 countdown shows on the TV, then the screen turns off.
  Press Back or OK during the countdown to cancel.
- When the TV turns on, it jumps straight to the HDMI input you choose (HDMI 1, HDMI 2, ...).
- All settings are on the same screen: off method, countdown length, which input.

IMPORTANT, PLEASE READ
- This app was written but NOT test-built or tested on a real TV (no Android build tools where it was made).
  It may need a small fix on the first build. Tell Claude the exact error and it will be fixed.
- Android does not let normal apps switch a TV off, so there are 3 "off methods" in the app.
  Which one actually works depends on your Xiaomi TV. Try them one by one (see STEP 4).

STEP 1  BUILD THE APK  (pick ONE way)
 Way A - GitHub (no installs needed)
   1. Make a free account at github.com and create a new empty repository (private is fine).
   2. Upload ALL files of this folder, INCLUDING the hidden folder ".github".
      (Drag the whole extracted folder's contents into the upload page.)
   3. Click the "Actions" tab -> "Build APK" -> wait for the green tick (about 3 minutes).
      If it does not start, click "Run workflow".
   4. Open the finished run -> scroll down to "Artifacts" -> download "TVPower-apk".
      Unzip it: you get app-debug.apk.
 Way B - Android Studio on your laptop
   1. Open this folder in Android Studio, wait for sync.
   2. Build -> Build APK(s). The file is in app/build/outputs/apk/debug/app-debug.apk

STEP 2  INSTALL
   1. Turn USB debugging ON on the TV (Settings > Device Preferences > Developer options).
   2. Copy these 3 into your platform-tools folder:
        app-debug.apk  (rename it to  tvpower.apk)
        after_build/setup_tvpower.bat
        after_build/enable_service.sh
   3. Double-click setup_tvpower.bat. The app opens on your TV.

STEP 3  CHECK THE STATUS LINE (bottom of the app screen)
   "Helper service: running" must say running.
   If it says NOT running: on the TV go to Settings > Device Preferences > Accessibility
   and switch "TV Power" on.

STEP 4  TEST
   a) Press "When TV turns on, go to:" until it shows the HDMI input your cable box uses.
      Press "Test: switch to that input now" - the TV should jump to it.
   b) Press "Power Off" with method 1. The countdown runs, then the screen should go off.
      If the TV does NOT turn off, press "Off method" to try method 2, then method 3. Tell Claude which one worked.
   c) Turn the TV on with the remote. About 2 seconds later it should jump to your input.

TO REMOVE
   Double-click after_build/remove_tvpower.bat
