<template>
  <el-container class="layout">
    <Sidebar :collapse="collapse" />
    <el-container class="layout-body">
      <Header v-model:collapse="collapse" />
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from './components/Sidebar.vue'
import Header from './components/Header.vue'

const collapse = ref(false)
</script>

<style scoped>
.layout {
  height: 100%;
}

.layout-body {
  height: 100%;
  min-width: 0;
  flex-direction: column;
}

.layout-main {
  padding: 16px;
  overflow: auto;
  background-color: #f0f2f5;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
