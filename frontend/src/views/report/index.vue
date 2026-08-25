<template>
  <div class="page">
    <!-- 汇总卡片 -->
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

    <el-row :gutter="16">
      <el-col :xs="24" :md="10">
        <el-card shadow="never" header="设备类型分布">
          <BaseChart :option="typeDistOption" height="320px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="14">
        <el-card shadow="never" header="点位数值统计">
          <el-form :inline="true" :model="statsQuery" class="search-form">
            <el-form-item label="设备">
              <el-select
                v-model="statsQuery.devId"
                placeholder="选择设备"
                filterable
                style="width: 180px"
                @change="handleDeviceChange"
              >
                <el-option v-for="d in devices" :key="d.devId" :label="d.deviceName" :value="d.devId" />
              </el-select>
            </el-form-item>
            <el-form-item label="点位">
              <el-select
                v-model="statsQuery.pointCode"
                placeholder="选择点位"
                filterable
                style="width: 150px"
              >
                <el-option
                  v-for="p in points"
                  :key="p.pointCode"
                  :label="`${p.pointName}(${p.pointCode})`"
                  :value="p.pointCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="statsTimeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始"
                end-placeholder="结束"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 340px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="loadStats">统计</el-button>
            </el-form-item>
          </el-form>

          <el-table v-if="stats" :data="[stats]" border>
            <el-table-column prop="pointCode" label="点位编码" width="110" />
            <el-table-column prop="pointName" label="点位名称" width="120" />
            <el-table-column prop="unit" label="单位" width="80">
              <template #default="{ row }">{{ row.unit || '-' }}</template>
            </el-table-column>
            <el-table-column prop="max" label="最大值" />
            <el-table-column prop="min" label="最小值" />
            <el-table-column prop="avg" label="平均值" />
            <el-table-column prop="count" label="数据条数" />
          </el-table>
          <el-empty v-else description="选择设备与点位后点击统计" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import BaseChart from '@/components/BaseChart.vue'
import { getSummary, getDeviceTypeDist, getPointStats } from '@/api/dashboard'
import { getAllDevices, getDevicePoints } from '@/api/device'

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
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: typeDist.value
    }
  ]
}))

const devices = ref([])
const points = ref([])
const stats = ref(null)
const deviceMap = {}
const statsTimeRange = ref([])

const statsQuery = reactive({ devId: '', pointCode: '' })

async function loadSummary() {
  const data = await getSummary()
  Object.assign(summary, data || {})
}

async function loadTypeDist() {
  typeDist.value = (await getDeviceTypeDist()) || []
}

async function loadDevices() {
  devices.value = (await getAllDevices()) || []
  devices.value.forEach((d) => (deviceMap[d.devId] = d))
}

async function handleDeviceChange() {
  points.value = []
  statsQuery.pointCode = ''
  stats.value = null
  if (!statsQuery.devId) return
  const device = deviceMap[statsQuery.devId]
  if (!device || device.id == null) return
  points.value = (await getDevicePoints(device.id)) || []
}

async function loadStats() {
  if (!statsQuery.devId || !statsQuery.pointCode) {
    ElMessage.warning('请选择设备与点位')
    return
  }
  const params = { devId: statsQuery.devId, pointCode: statsQuery.pointCode }
  if (statsTimeRange.value?.length === 2) {
    params.start = statsTimeRange.value[0]
    params.end = statsTimeRange.value[1]
  }
  stats.value = (await getPointStats(params)) || null
}

onMounted(async () => {
  await Promise.all([loadSummary(), loadTypeDist(), loadDevices()])
})
</script>

<style scoped>
.page {
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
</style>
