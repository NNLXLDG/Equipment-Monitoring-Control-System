import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, getMe } from '@/api/auth'
import { STATIC_MENUS, COMPONENT_PATH_MAP } from '@/constants'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem('user') || 'null')
  } catch (e) {
    return null
  }
}

// 解析单个菜单项对应的前端路由路径
export function resolveMenuPath(menu) {
  if (!menu) return '/'
  if (menu.component && COMPONENT_PATH_MAP[menu.component]) {
    return COMPONENT_PATH_MAP[menu.component]
  }
  if (menu.path && menu.path.startsWith('/')) return menu.path
  return '/' + (menu.path || '')
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: readUser()
  }),

  getters: {
    isLogin: (state) => !!state.token,
    username: (state) => state.user?.username || '',
    realName: (state) => state.user?.realName || state.user?.username || '',
    roles: (state) => state.user?.roles || [],
    permissions: (state) => state.user?.permissions || [],
    // 后端未返回 menus 时使用静态菜单兜底
    menus: (state) => (state.user?.menus?.length ? state.user.menus : STATIC_MENUS),
    // 当前用户可访问的路由路径集合（用于路由守卫）
    allowedPaths() {
      const paths = new Set(['/'])
      const walk = (list) => {
        for (const m of list || []) {
          if (m.type === 2) paths.add(resolveMenuPath(m))
          if (m.children?.length) walk(m.children)
        }
      }
      walk(this.menus)
      return paths
    }
  },

  actions: {
    hasPerm(perm) {
      if (!perm) return true
      // 超级管理员放行
      if (this.roles.includes('ADMIN')) return true
      return this.permissions.includes(perm)
    },

    async login(form) {
      const data = await apiLogin(form)
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(data.user))
      return data
    },

    async fetchMe() {
      const user = await getMe()
      this.user = user
      localStorage.setItem('user', JSON.stringify(user))
      return user
    },

    async logout() {
      try {
        await apiLogout()
      } catch (e) {
        // 忽略登出接口异常，本地照常清理
      }
      this.reset()
    },

    reset() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
