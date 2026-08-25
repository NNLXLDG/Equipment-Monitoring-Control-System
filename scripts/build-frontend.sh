#!/usr/bin/env bash
# 构建前端并复制到后端静态资源目录
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "==> 安装前端依赖"
(cd "$ROOT/frontend" && npm install)

echo "==> 构建前端"
(cd "$ROOT/frontend" && npm run build)

echo "==> 复制产物到后端静态目录"
rm -rf "$ROOT/backend/src/main/resources/static"
mkdir -p "$ROOT/backend/src/main/resources/static"
cp -r "$ROOT/frontend/dist/"* "$ROOT/backend/src/main/resources/static/"

echo "==> 完成：前端已构建并复制到 backend/src/main/resources/static"
