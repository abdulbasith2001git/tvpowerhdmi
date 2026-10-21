@echo off
set TV=192.168.29.102:5555
adb connect %TV%
adb uninstall com.tvpower.app
echo App removed.
pause
