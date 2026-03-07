#!/bin/sh

set -e
cd "$(dirname -- "$0")"

cd ./bin

BINARY=unknown

if [ "$OS" = "Windows_NT" ]; then
  BINARY=jq-windows-amd64.exe
else
  echo "$(uname -s)_$(uname -m)"
  case "$(uname -s)_$(uname -m)" in
    Darwin_arm64)
      BINARY=jq-macos-arm64
      ;;
    Darwin_amd64)
      BINARY=jq-macos-amd64
      ;;
    Linux_arm64)
      BINARY=jq-linux-arm64
      ;;
    Linux_amd64)
      BINARY=jq-linux-amd64
      ;;
  esac
fi

if [ "$BINARY" != "unknown" ]; then
  echo "installing jq version $BINARY"
  curl -s -L -o jq "https://github.com/jqlang/jq/releases/download/jq-1.7.1/$BINARY"
  chmod +x ./jq
else
  echo "unable to figure out operating system to know which binary to install."
fi
