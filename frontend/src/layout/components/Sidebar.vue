<template>
  <el-aside :width="collapse ? '64px' : '220px'" class="sidebar">
    <div class="logo" @click="goHome">
      <el-icon :size="26" color="#409eff"><Monitor /></el-icon>
      <span v-show="!collapse" class="logo-text">设备监测与控制系统</span>
    </div>
    <el-scrollbar>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapse"
        :collapse-transition="false"
        router
        background-color="#001529"
        text-color="#a6adb4"
        active-text-color="#ffffff"
        class="sidebar-menu"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页看板</template>
        </el-menu-item>

        <template v-for="menu in menus" :key="menu.id">
          <!-- 根目录 type=1 -->
          <el-sub-menu v-if="menu.type === 1" :index="menu.path || String(menu.id)">
            <template #title>
              <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.id"
              :index="resolvePath(child)"
            >
              <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
              <template #title>{{ child.name }}</template>
            </el-menu-item>
          </el-sub-menu>

          <!-- 根菜单 type=2 -->
          <el-menu-item v-else :index="resolvePath(menu)">
            <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
            <template #title>{{ menu.name }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore, resolveMenuPath } from '@/stores/user'

defineProps({
  collapse: { type: Boolean, default: false }
})

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const menus = computed(() => store.menus)
const activeMenu = computed(() => route.path)

function resolvePath(menu) {
  return resolveMenuPath(menu)
}

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.sidebar {
  background-color: #001529;
  transition: width 0.2s;
  overflow: hidden;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;
  font-weight: 600;
  white-space: nowrap;
}

.logo-text {
  font-size: 16px;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}
</style>
