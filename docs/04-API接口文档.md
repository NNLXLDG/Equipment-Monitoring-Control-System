# 基于MQTT协议的企业设备运行监测与远程控制系统 API 接口文档

## 约定
- 基础路径：`/api`
- 认证：请求头 `Authorization: Bearer <token>`（登录/登出/获取验证码除外）
- 统一返回结构：
```json
{ "code": 200, "message": "success", "data": {} }
```
- 分页返回结构（`data` 内）：
```json
{ "records": [], "total": 0, "page": 1, "size": 10 }
```
- 错误：`code` 非 200（401 未认证 / 403 无权限 / 500 业务异常），`message` 为提示。

## 1. 认证 Auth
- `POST /api/auth/login` 入参 `{ "username": "admin", "password": "123456" }`
  返回 `data`: `{ "token": "...", "user": { id, username, realName, roles:["ADMIN"], permissions:["device:add", ...], menus:[...] } }`
- `GET /api/auth/me` 返回当前用户信息（结构同 login 的 user）
- `POST /api/auth/logout` 无返回体

## 2. 用户 User（需 admin 权限）
- `GET /api/users?page=1&size=10&keyword=` 分页列表
- `GET /api/users/{id}` 详情（含角色 id 列表 roles:[...]）
- `POST /api/users` 入参 `{username, password, realName, phone, email, status, roleIds:[...]}`
- `PUT /api/users/{id}` 同 POST（password 可选）
- `DELETE /api/users/{id}`
- `PUT /api/users/{id}/status` 入参 `{status:0|1}`
- `PUT /api/users/{id}/password` 入参 `{password:"new"}`

## 3. 角色 Role
- `GET /api/roles` 全量列表（用于下拉）
- `GET /api/roles?page=&size=&keyword=` 分页
- `POST /api/roles` 入参 `{roleName, roleCode, description, status}`
- `PUT /api/roles/{id}`
- `DELETE /api/roles/{id}`
- `GET /api/roles/{id}/permissions` 返回权限 id 数组
- `PUT /api/roles/{id}/permissions` 入参 `[permId1, permId2, ...]`

## 4. 权限/菜单 Permission
- `GET /api/permissions/tree` 返回树形（含 children）
- `GET /api/permissions` 扁平列表
- `POST /api/permissions` 入参 `{parentId, name, type:1|2|3, path, component, perm, icon, sort}`
- `PUT /api/permissions/{id}`
- `DELETE /api/permissions/{id}`

## 5. 设备 Device
- `GET /api/devices?page=&size=&keyword=&status=&deviceType=` 分页
- `GET /api/devices/all` 全量（下拉）
- `GET /api/devices/{id}`
- `POST /api/devices` 入参 `{devId, deviceName, deviceType, model, manufacturer, location, status, installDate, description}`
- `PUT /api/devices/{id}`
- `DELETE /api/devices/{id}`
- `GET /api/devices/{id}/points` 该设备点位列表

## 6. 设备点位 DevicePoint
- `GET /api/points?deviceId=&pointType=&page=&size=` 分页
- `GET /api/points/{id}`
- `POST /api/points` 入参 `{deviceId, pointCode, pointName, pointType:1|2, dataType:1|2, unit, minValue, maxValue, defaultValue, description}`
- `PUT /api/points/{id}`
- `DELETE /api/points/{id}`

## 7. 采集数据 Data
- `GET /api/data?devId=&pointCode=&start=&end=&page=&size=` 分页（start/end 格式 yyyy-MM-dd HH:mm:ss）
- `GET /api/data/latest?devId=` 返回该设备各点位最新一条：`[{pointCode, pointName, value, unit, collectTime}]`
- `GET /api/data/chart?devId=&pointCode=&start=&end=` 返回 `{ times:[...], values:[...] }`（用于折线图）

## 8. 控制 Control
- `POST /api/control/send` 入参 `{ devId, data: { "DO1": "1", ... } }` 返回下发结果
- `GET /api/control/records?page=&size=&devId=` 控制记录分页

## 9. 看板/报表 Dashboard
- `GET /api/dashboard/summary` 返回 `{ deviceTotal, deviceOnline, pointTotal, dataToday, controlTotal }`
- `GET /api/dashboard/deviceTypeDist` 返回 `[{ name, value }]`
- `GET /api/dashboard/dataTrend?devId=&pointCode=&hours=24` 返回 `{ times:[], values:[] }`
- `GET /api/dashboard/pointStats?devId=&pointCode=&start=&end=` 返回 `{ pointCode, pointName, unit, max, min, avg, count }`
