import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const Layout = () => import('@/layout/index.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页看板', icon: 'HomeFilled' }
      },
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/device/index.vue'),
        meta: { title: '设备管理', icon: 'Cpu', requiresMenu: true }
      },
      {
        path: 'point',
        name: 'Point',
        component: () => import('@/views/point/index.vue'),
        meta: { title: '点位管理', icon: 'SetUp', requiresMenu: true }
      },
      {
        path: 'data',
        name: 'Data',
        component: () => import('@/views/data/index.vue'),
        meta: { title: '数据查询', icon: 'DataLine', requiresMenu: true }
      },
      {
        path: 'chart',
        name: 'Chart',
        component: () => import('@/views/chart/index.vue'),
        meta: { title: '图形分析', icon: 'TrendCharts', requiresMenu: true }
      },
      {
        path: 'control',
        name: 'Control',
        component: () => import('@/views/control/index.vue'),
        meta: { title: '控制面板', icon: 'SwitchButton', requiresMenu: true }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/report/index.vue'),
        meta: { title: '统计报表', icon: 'PieChart', requiresMenu: true }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', requiresMenu: true }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', requiresMenu: true }
      },
      {
        path: 'permission',
        name: 'Permission',
        component: () => import('@/views/system/permission/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu', requiresMenu: true }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta?.title ? `${to.meta.title} - 基于MQTT协议的企业设备运行监测与远程控制系统` : '基于MQTT协议的企业设备运行监测与远程控制系统'

  const store = useUserStore()
  // 公开页面（登录页）
  if (to.meta.public) {
    return next()
  }
  // 未登录
  if (!store.token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  // 菜单级访问控制
  if (to.meta.requiresMenu && !store.allowedPaths.has(to.path)) {
    return next('/403')
  }
  next()
})

export default router
