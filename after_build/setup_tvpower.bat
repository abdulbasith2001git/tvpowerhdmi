@echo off
rem Put this file, enable_service.sh and tvpower.apk in your platform-tools folder.
set TV=192.168.29.102:5555
set PKG=com.tvpower.app
echo Connecting to the TV...
adb connect %TV%
echo.
echo Installing the app...
adb install -r tvpower.apk
echo.
echo Allowing the app to open the TV input screen from the background...
adb shell appops set %PKG% SYSTEM_ALERT_WINDOW allow
echo.
echo Turning on the helper service (keeps your other services)...
adb push enable_service.sh /data/local/tmp/enable_service.sh
adb shell sh /data/local/tmp/enable_service.sh
echo.
echo Turning on Device admin (second way to lock the screen)...
adb shell dpm set-active-admin %PKG%/.AdminReceiver
echo.
echo Opening the app on the TV...
adb shell am start -n %PKG%/.MainActivity
echo.
echo Done! Look at your TV.
pause
