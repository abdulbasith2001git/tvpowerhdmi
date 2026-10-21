@echo off
set /p IP=Type the TV IP address, for example 192.168.29.102:
set TV=%IP%:5555
adb connect %TV%
adb uninstall com.tvpower.app
echo App removed.
pause
