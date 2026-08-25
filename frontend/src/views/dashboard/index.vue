<template>
  <div class="dashboard">
    <!-- 汇总统计卡片 -->
    <div class="stat-grid">
      <el-card v-for="card in cards" :key="card.key" shadow="hover" class="stat-card">
        <div class="stat-icon" :style="{ backgroundColor: card.color }">
          <el-icon :size="26" color="#fff"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-card>
    </div>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="10">
        <el-card shadow="never" header="设备类型分布">
          <BaseChart :option="typeDistOption" height="320px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header>
            <div class="trend-header">
              <span>最近 24 小时数据趋势</span>
              <el-select
                v-model="trendDevId"
                placeholder="选择设备"
                size="small"
                style="width: 220px"
                @change="loadTrend"
              >
                <el-option
                  v-for="d in devices"
                  :key="d.id"
                  :label="d.deviceName"
                  :value="d.devId"
                />
              </el-select>
            </div>
          </template>
          <BaseChart :option="trendOption" height="320px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import BaseChart from '@/components/BaseChart.vue'
import { getSummary, getDeviceTypeDist, getDataTrend } from '@/api/dashboard'
import { getAllDevices } from '@/api/device'

const summary = reactive({
  deviceTotal: 0,
  deviceOnline: 0,
  pointTotal: 0,
  dataToday: 0,
  controlTotal: 0
})

const cards = computed(() => [
  { key: 'deviceTotal', label: '设备总量', value: summary.deviceTotal, icon: 'Monitor', color: '#409eff' },
  { key: 'deviceOnline', label: '在线设备', value: summary.deviceOnline, icon: 'Cpu', color: '#67c23a' },
  { key: 'pointTotal', label: '点位总数', value: summary.pointTotal, icon: 'SetUp', color: '#e6a23c' },
  { key: 'dataToday', label: '今日数据', value: summary.dataToday, icon: 'DataLine', color: '#909399' },
  { key: 'controlTotal', label: '控制次数', value: summary.controlTotal, icon: 'SwitchButton', color: '#f56c6c' }
])

const typeDist = ref([])
const typeDistOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', left: 'left' },
  series: [
    {
      name: '设备类型',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: typeDist.value
    }
  ]
}))

const devices = ref([])
const trendDevId = ref('')
const trendOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 20, top: 30, bottom: 30 },
  xAxis: { type: 'category', data: [], boundaryGap: false },
  yAxis: { type: 'value' },
  series: [{ name: '数值', type: 'line', smooth: true, areaStyle: {}, data: [] }]
})

async function loadSummary() {
  const data = await getSummary()
  Object.assign(summary, data || {})
}

async function loadTypeDist() {
  const data = await getDeviceTypeDist()
  typeDist.value = data || []
}

async function loadDevices() {
  const data = await getAllDevices()
  devices.value = data || []
  if (devices.value.length) {
    trendDevId.value = devices.value[0].devId
  }
}

async function loadTrend() {
  if (!trendDevId.value) return
  const data = await getDataTrend({ devId: trendDevId.value, hours: 24 })
  trendOption.value = {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: data.times || [], boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      {
        name: '数值',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.2 },
        data: data.values || []
      }
    ]
  }
}

onMounted(async () => {
  await Promise.all([loadSummary(), loadTypeDist(), loadDevices()])
  await loadTrend()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.stat-grid .stat-card {
  flex: 1 1 180px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.chart-row {
  margin-top: 4px;
}

.trend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
