<template>
  <el-header class="header">
    <div class="header-left">
      <el-icon class="collapse-btn" :size="20" @click="toggle">
        <Expand v-if="collapse" />
        <Fold v-else />
      </el-icon>
      <Breadcrumb />
    </div>
    <div class="header-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="30" class="user-avatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <span class="user-name">{{ store.realName }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import Breadcrumb from './Breadcrumb.vue'

const props = defineProps({
  collapse: { type: Boolean, default: false }
})
const emit = defineEmits(['update:collapse'])

const router = useRouter()
const store = useUserStore()

function toggle() {
  emit('update:collapse', !props.collapse)
}

async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch (e) {
      return
    }
    await store.logout()
    router.replace('/login')
  }
}
</script>

<style scoped>
.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 16px;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
  color: #333;
}

.user-avatar {
  background-color: #409eff;
}

.user-name {
  font-size: 14px;
}
</style>
