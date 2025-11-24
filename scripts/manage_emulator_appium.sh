#!/usr/bin/env bash
# manage_emulator_appium.sh
# Usage: ./manage_emulator_appium.sh start|stop|status
# - start: starts emulator (if none) and Appium server using npx
# - stop: stops Appium server and emulator
# - status: shows running emulator(s) and Appium PID

set -euo pipefail

EMULATOR_NAME="Pixel_9_Pro_XL"
APP_PORT=4723
APP_HOST=127.0.0.1
WORKDIR="$(cd "$(dirname "$0")/.." && pwd)"
PIDFILE="$WORKDIR/appium.pid"
LOGFILE="$WORKDIR/appium.log"

function ensure_android_paths() {
  if [ -z "${ANDROID_SDK_ROOT:-}" ]; then
    echo "ANDROID_SDK_ROOT is not set. Please export ANDROID_SDK_ROOT."
    exit 1
  fi
  export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$PATH"
}

function emulator_running() {
  adb devices | awk 'NR>1 && $2=="device" {print $1}' | grep -q "emulator-" || return 1
}

function start_emulator() {
  if emulator_running; then
    echo "Emulator already running."
    return 0
  fi
  echo "Starting emulator: $EMULATOR_NAME"
  # start emulator in background; user may change flags
  nohup emulator -avd "$EMULATOR_NAME" -no-snapshot-load > /dev/null 2>&1 &
  # wait for device
  echo -n "Waiting for emulator to boot"
  for i in {1..60}; do
    if adb devices | grep -q "emulator-"; then
      echo "\nEmulator appeared"
      # wait for fully booted
      adb wait-for-device
      # small delay for system to settle
      sleep 2
      return 0
    fi
    echo -n "."
    sleep 1
  done
  echo "\nTimed out waiting for emulator" >&2
  return 1
}

function start_appium() {
  if [ -f "$PIDFILE" ]; then
    PID=$(cat "$PIDFILE" 2>/dev/null || true)
    if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
      echo "Appium already running (PID $PID)"
      return 0
    else
      rm -f "$PIDFILE"
    fi
  fi
  echo "Starting Appium on $APP_HOST:$APP_PORT (log: $LOGFILE)"
  if command -v npx >/dev/null 2>&1; then
    nohup npx appium --address "$APP_HOST" --port "$APP_PORT" > "$LOGFILE" 2>&1 &
    echo $! > "$PIDFILE"
    sleep 1
    echo "Appium started (PID $(cat "$PIDFILE"))"
  else
    echo "npx not found. Install Appium: npm install -g appium OR use npx." >&2
    return 1
  fi
}

function stop_appium() {
  if [ -f "$PIDFILE" ]; then
    PID=$(cat "$PIDFILE" 2>/dev/null || true)
    if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
      echo "Stopping Appium (PID $PID)"
      kill "$PID" || true
      sleep 1
    fi
    rm -f "$PIDFILE" || true
  else
    echo "No appium.pid found; trying to find Appium process"
    PK=$(pgrep -f "appium") || true
    if [ -n "$PK" ]; then
      echo "Killing Appium PIDs: $PK"
      echo "$PK" | xargs kill || true
    else
      echo "No Appium process found."
    fi
  fi
}

function stop_emulator() {
  if emulator_running; then
    echo "Stopping emulator(s)"
    adb devices | awk 'NR>1 && $2=="device" {print $1}' | grep "emulator-" | while read -r id; do
      echo "Killing $id"
      adb -s "$id" emu kill || true
    done
  else
    echo "No emulator running."
  fi
}

function status() {
  echo "ADB devices:"
  adb devices -l || true
  echo "\nAppium status:"
  if [ -f "$PIDFILE" ]; then
    PID=$(cat "$PIDFILE" 2>/dev/null || true)
    if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
      echo "Appium running (PID $PID) - log: $LOGFILE"
    else
      echo "PID file exists but process not running"
    fi
  else
    echo "No appium.pid found"
  fi
}

case "${1:-}" in
  start)
    ensure_android_paths
    start_emulator
    start_appium
    ;;
  stop)
    stop_appium
    stop_emulator
    ;;
  status)
    ensure_android_paths
    status
    ;;
  *)
    echo "Usage: $0 start|stop|status"
    exit 1
    ;;
esac
