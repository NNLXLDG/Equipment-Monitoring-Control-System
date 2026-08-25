// 业务常量与字典映射

export const DEVICE_STATUS = {
  1: { text: '在线', type: 'success' },
  0: { text: '离线', type: 'info' }
}

export const POINT_TYPE = {
  1: { text: '采集(AI)', type: 'primary' },
  2: { text: '控制(DO)', type: 'warning' }
}

export const DATA_TYPE = {
  1: '数值',
  2: '布尔'
}

export const PERM_TYPE = {
  1: { text: '目录', type: 'info' },
  2: { text: '菜单', type: 'primary' },
  3: { text: '按钮', type: 'warning' }
}

export const CONTROL_STATUS = {
  0: { text: '已发送', type: 'info' },
  1: { text: '成功', type: 'success' },
  2: { text: '失败', type: 'danger' }
}

// 菜单 component 字段 -> 扁平路由路径 的映射（用于动态菜单渲染与路由守卫）
export const COMPONENT_PATH_MAP = {
  'dashboard/index': '/',
  'device/index': '/device',
  'point/index': '/point',
  'data/index': '/data',
  'chart/index': '/chart',
  'control/index': '/control',
  'report/index': '/report',
  'system/user/index': '/user',
  'system/role/index': '/role',
  'system/permission/index': '/permission'
}

// 后端未返回 menus 时的静态兜底菜单（结构与后端一致）
export const STATIC_MENUS = [
  {
    id: 0,
    name: '首页看板',
    type: 2,
    path: '/',
    component: 'dashboard/index',
    icon: 'HomeFilled',
    children: []
  },
  {
    id: 1,
    name: '设备监测',
    type: 1,
    path: '/monitor',
    icon: 'Monitor',
    children: [
      { id: 10, name: '设备管理', type: 2, path: 'device', component: 'device/index', icon: 'Cpu', children: [] },
      { id: 20, name: '点位管理', type: 2, path: 'point', component: 'point/index', icon: 'SetUp', children: [] },
      { id: 30, name: '数据查询', type: 2, path: 'data', component: 'data/index', icon: 'DataLine', children: [] },
      { id: 40, name: '图形分析', type: 2, path: 'chart', component: 'chart/index', icon: 'TrendCharts', children: [] },
      { id: 50, name: '控制面板', type: 2, path: 'control', component: 'control/index', icon: 'SwitchButton', children: [] },
      { id: 60, name: '统计报表', type: 2, path: 'report', component: 'report/index', icon: 'PieChart', children: [] }
    ]
  },
  {
    id: 2,
    name: '系统管理',
    type: 1,
    path: '/system',
    icon: 'Setting',
    children: [
      { id: 70, name: '用户管理', type: 2, path: 'user', component: 'system/user/index', icon: 'User', children: [] },
      { id: 80, name: '角色管理', type: 2, path: 'role', component: 'system/role/index', icon: 'UserFilled', children: [] },
      { id: 90, name: '菜单管理', type: 2, path: 'permission', component: 'system/permission/index', icon: 'Menu', children: [] }
    ]
  }
]
