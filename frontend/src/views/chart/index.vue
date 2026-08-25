<template>
  <div class="page">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="设备">
          <el-select
            v-model="query.devId"
            placeholder="请选择设备"
            filterable
            style="width: 200px"
            @change="handleDeviceChange"
          >
            <el-option v-for="d in devices" :key="d.devId" :label="d.deviceName" :value="d.devId" />
          </el-select>
        </el-form-item>
        <el-form-item label="点位">
          <el-select
            v-model="query.pointCode"
            placeholder="请选择点位"
            filterable
            style="width: 160px"
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
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">分析</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="16">
        <el-card shadow="never" header="趋势折线图">
          <BaseChart :option="chartOption" height="380px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" header="点位统计">
          <el-descriptions v-if="stats" :column="1" border>
            <el-descriptions-item label="点位编码">{{ stats.pointCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="点位名称">{{ stats.pointName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="单位">{{ stats.unit || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最大值">{{ stats.max ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="最小值">{{ stats.min ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="平均值">{{ stats.avg ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="数据条数">{{ stats.count ?? 0 }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="请选择设备与点位后点击分析" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import BaseChart from '@/components/BaseChart.vue'
import { getChartData } from '@/api/data'
import { getPointStats } from '@/api/dashboard'
import { getAllDevices, getDevicePoints } from '@/api/device'

const devices = ref([])
const points = ref([])
const timeRange = ref([])
const stats = ref(null)
const deviceMap = {}

const query = reactive({
  devId: '',
  pointCode: ''
})

const chartOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: 50, right: 20, top: 30, bottom: 40 },
  xAxis: { type: 'category', data: [], boundaryGap: false },
  yAxis: { type: 'value' },
  dataZoom: [{ type: 'inside' }, { type: 'slider' }],
  series: [{ name: '数值', type: 'line', smooth: true, areaStyle: { opacity: 0.2 }, data: [] }]
})

async function loadDevices() {
  devices.value = (await getAllDevices()) || []
  devices.value.forEach((d) => (deviceMap[d.devId] = d))
}

async function handleDeviceChange() {
  points.value = []
  query.pointCode = ''
  stats.value = null
  if (!query.devId) return
  const device = deviceMap[query.devId]
  if (!device || device.id == null) return
  const data = await getDevicePoints(device.id)
  points.value = data || []
}

async function handleQuery() {
  if (!query.devId || !query.pointCode) {
    ElMessage.warning('请选择设备与点位')
    return
  }
  const params = { devId: query.devId, pointCode: query.pointCode }
  if (timeRange.value?.length === 2) {
    params.start = timeRange.value[0]
    params.end = timeRange.value[1]
  }
  const [chart, pointStats] = await Promise.all([
    getChartData(params),
    getPointStats(params)
  ])
  chartOption.value = {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: chart.times || [], boundaryGap: false },
    yAxis: { type: 'value' },
    dataZoom: [{ type: 'inside' }, { type: 'slider' }],
    series: [
      {
        name: '数值',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.2 },
        data: chart.values || []
      }
    ]
  }
  stats.value = pointStats || null
}

onMounted(loadDevices)
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
