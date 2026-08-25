# 基于MQTT协议的企业设备运行监测与远程控制系统 - 前端

基于 **Vue 3 + Vite + Vue Router 4 + Pinia + Element Plus + ECharts + Axios** 的单页应用（SPA），对接后端统一返回结构 `{ code, message, data }`。

## 技术栈

- Vue 3（组合式 API，JavaScript）
- Vite 5
- Vue Router 4
- Pinia 2
- Element Plus 2（中文 locale，全量图标注册）
- ECharts 5
- Axios

## 目录结构

```
frontend/
├── index.html
├── package.json
├── vite.config.js
├── src/
│   ├── main.js               # 入口：注册 Pinia/Router/ElementPlus/图标/v-perm
│   ├── App.vue
│   ├── api/                  # 按模块封装的接口（统一走 request.js）
│   │   ├── request.js        # axios 实例 + 请求/响应拦截器
│   │   └── auth|user|role|permission|device|point|data|control|dashboard.js
│   ├── stores/
│   │   ├── index.js          # Pinia 实例
│   │   └── user.js           # 用户/token/roles/permissions/menus + 菜单路径解析
│   ├── router/index.js       # 路由 + 登录/菜单权限守卫
│   ├── directives/perm.js    # v-perm 按钮级权限指令
│   ├── constants/index.js    # 状态字典、静态兜底菜单、组件路径映射
│   ├── utils/index.js        # 时间格式化
│   ├── components/BaseChart.vue  # ECharts 封装
│   ├── layout/               # 主布局（侧边栏 + Header + 面包屑）
│   └── views/                # 页面
│       ├── login/            # 登录
│       ├── dashboard/        # 首页看板
│       ├── device/           # 设备管理
│       ├── point/            # 点位管理
│       ├── data/             # 数据查询
│       ├── chart/            # 图形分析
│       ├── control/          # 控制面板
│       ├── report/           # 统计报表
│       ├── system/{user,role,permission}/  # 用户/角色/菜单管理
│       └── error/{403,404}.vue
```

## 运行方式

```bash
npm install        # 安装依赖（网络失败可加 --registry=https://registry.npmmirror.com）
npm run dev        # 开发模式，默认 http://localhost:5173，/api 代理到 http://localhost:8080
npm run build      # 生产构建，输出到 frontend/dist
npm run preview    # 预览生产构建产物
```

## 权限说明

- 登录响应 `data.user` 包含 `roles`（角色编码数组）、`permissions`（权限标识数组）、`menus`（菜单树，type=1 目录 / type=2 菜单）。
- 主布局侧边栏按 `menus` 动态渲染：目录用 `el-sub-menu`，菜单用 `el-menu-item`；后端未返回 `menus` 时回退到 `src/constants/index.js` 中的 `STATIC_MENUS`。
- 按钮级权限：自定义指令 `v-perm="'device:add'"`，当前用户 `permissions` 不含该标识则移除元素；`ADMIN` 角色放行全部。
- 路由守卫：未登录跳转 `/login`；`meta.requiresMenu` 的路由按用户菜单推导的可访问路径集合做拦截，无权限跳 `/403`。

## 路由与页面清单

| 路由 | 名称 | 说明 |
|------|------|------|
| `/login` | 登录 | 账号密码 + 记住我 |
| `/` | 首页看板 | 汇总卡片 + 类型分布饼图 + 24h 趋势折线图 |
| `/device` | 设备管理 | 搜索 + 分页 + 新增/编辑/删除 |
| `/point` | 点位管理 | 设备下拉 + 类型筛选 + 分页 + CRUD，展示量程/单位/缺省值 |
| `/data` | 数据查询 | 设备/点位/时间范围筛选 + 分页 |
| `/chart` | 图形分析 | 设备+点位+时间范围折线图 + 点位统计 |
| `/control` | 控制面板 | 设备控制点下发 + 控制记录分页 |
| `/report` | 统计报表 | 汇总卡片 + 类型分布 + 点位统计 |
| `/user` | 用户管理 | 分页 + 搜索 + CRUD + 分配角色 + 启停 + 重置密码 |
| `/role` | 角色管理 | 分页 + CRUD + 分配权限（树形勾选） |
| `/permission` | 菜单管理 | 树形表格 + 新增/编辑/删除 |
| `/403` `/404` | 错误页 | - |

## 与 API 契约的偏差说明

1. **设备标识双 ID 问题**：接口中同时存在设备数字主键 `id` 与字符串 `devId`。按数据库表设计，`/devices/{id}/points`、`/points?deviceId=` 使用数字主键 `id`；`/data`、`/data/chart`、`/control/*`、`/dashboard/dataTrend`、`/dashboard/pointStats` 使用字符串 `devId`。前端在设备下拉中同时保留两者，按接口分别取值。
2. **菜单路径**：后端菜单 `path` 为相对路径（如 `device`），前端通过 `component` 字段映射到扁平路由（`/device` 等），与任务给定的路由列表保持一致。
3. **菜单管理页按钮**：数据库初始化脚本未为「菜单管理」定义 `permission:add/edit/delete` 按钮权限，前端仍按 `v-perm` 约定控制（`ADMIN` 角色放行）。
