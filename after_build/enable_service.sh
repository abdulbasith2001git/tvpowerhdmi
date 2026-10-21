#!/system/bin/sh
SVC="com.tvpower.app/com.tvpower.app.PowerService"
CUR=$(settings get secure enabled_accessibility_services)
case "$CUR" in
  *"$SVC"*) echo "service already enabled" ;;
  ""|null) settings put secure enabled_accessibility_services "$SVC"; echo "service enabled" ;;
  *) settings put secure enabled_accessibility_services "$CUR:$SVC"; echo "service enabled (kept your other services)" ;;
esac
settings put secure accessibility_enabled 1
