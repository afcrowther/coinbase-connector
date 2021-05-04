#!/bin/bash

# Ensure that we have a market name
if [[ $# -lt 1 ]]; then
  readonly SCRIPT_NAME=$(basename "$0")
  echo "Usage: ${SCRIPT_NAME} <market-name>"
  echo "  e.g. ${SCRIPT_NAME} BTC-USD"
  exit 1
fi

# tune the gc towards a lot of short lived objects
JVM_ARGS="-Xms4G -Xmx12G -XX:+UseCompressedOops -XX:SurvivorRatio=1 -XX:NewRatio=1"

java ${JVM_ARGS} -jar ../../CoinbaseConnector.jar "$1"