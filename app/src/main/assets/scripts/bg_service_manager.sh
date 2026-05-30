#!/system/bin/sh
OPTION=$1

disable_bgs() {
	accessibility_services=$(settings get secure enabled_accessibility_services)
	new_accessibility_services=""
	for service in $(echo "$accessibility_services" | tr ":" "\n"); do
		ct_accessibility_service=$(echo "$service" | grep "xyz.chenzyadb.cu_toolbox")
		if [ -z "$ct_accessibility_service" ]; then
			new_accessibility_services="${new_accessibility_services}:${service}"
		fi
	done
	lenth=$(expr lenth "$new_accessibility_services")
  new_accessibility_services=$(expr substr "$new_accessibility_services" 2 "$lenth")
	settings put secure accessibility_enabled 0 2>/dev/null
	settings put secure enabled_accessibility_services "$new_accessibility_services" 2>/dev/null
	settings put secure accessibility_enabled 1 2>/dev/null
}

enable_bgs() {
  ct_accessibility_service="xyz.chenzyadb.cu_toolbox/xyz.chenzyadb.cu_toolbox.BackgroundAccessibilityService"
	accessibility_services=$(settings get secure enabled_accessibility_services)
	settings put secure accessibility_enabled 0 2>/dev/null
	settings put secure enabled_accessibility_services "${ct_accessibility_service}:${accessibility_services}" 2>/dev/null
	settings put secure accessibility_enabled 1 2>/dev/null
}

if [ "$OPTION" = "enable" ]; then
	enable_bgs
elif [ "$OPTION" = "disable" ]; then
	disable_bgs
else
	echo "Unknown command."
fi

exit 0
