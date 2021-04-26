#!/bin/bash

# Ensure that we have a market name
if [[ $# -lt 1 ]]; then
  readonly SCRIPT_NAME=$(basename "$0")
  echo "Usage: ${SCRIPT_NAME} <market-name>"
  echo "  e.g. ${SCRIPT_NAME} BTC_USD"
  exit 1
fi

JVM_ARGS="-Xms1G -Xmx2G -XX:+UseConcMarkSweepGC"

java "${JVM_ARGS}" -jar CoinbaseConnector.jar "$1"