#!/usr/bin/env bash
# SkillBridge — GPS Route Waypoint Simulator for Worker Device
# Streams progressive GPS coordinates to Android emulator via ADB
# Route: Dharampeth to Ramdaspeth, Nagpur, India

EMULATOR_SERIAL=${1:-"emulator-5556"}

echo "Streaming GPS waypoints to target device: ${EMULATOR_SERIAL}..."

# Waypoint Array (Longitude Latitude)
WAYPOINTS=(
  "79.0882 21.1458" # Dharampeth Start
  "79.0870 21.1460"
  "79.0855 21.1462"
  "79.0842 21.1464"
  "79.0830 21.1466" # Ramdaspeth Destination
)

for pt in "${WAYPOINTS[@]}"; do
  LON=$(echo $pt | cut -d' ' -f1)
  LAT=$(echo $pt | cut -d' ' -f2)
  echo "Setting GPS fix: Lat=${LAT}, Lon=${LON}"
  adb -s "${EMULATOR_SERIAL}" emu geo fix "${LON}" "${LAT}" 2>/dev/null || echo "adb geo fix: ${LON} ${LAT}"
  sleep 4
done

echo "GPS simulation route complete. Worker arrived at destination site."
