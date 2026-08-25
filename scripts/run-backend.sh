#!/usr/bin/env bash
# 启动后端（内嵌 MQTT Broker + 数据模拟器）
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/backend"
mvn spring-boot:run
