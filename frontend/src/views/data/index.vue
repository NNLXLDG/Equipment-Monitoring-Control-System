<template>
  <div class="page">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="设备">
          <el-select
            v-model="query.devId"
            placeholder="请选择设备"
            clearable
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
            placeholder="全部点位"
            clearable
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
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="devId" label="设备编号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="pointCode" label="点位编码" width="100" />
        <el-table-column prop="pointName" label="点位名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="value" label="数值" width="100" align="center" />
        <el-table-column prop="unit" label="单位" width="80" align="center">
          <template #default="{ row }">{{ row.unit || '-' }}</template>
        </el-table-column>
        <el-table-column label="采集时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.collectTime) }}</template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadList"
        @current-change="loadList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getData } from '@/api/data'
import { getAllDevices, getDevicePoints } from '@/api/device'
import { formatDateTime } from '@/utils'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const devices = ref([])
const points = ref([])
const timeRange = ref([])

const deviceMap = {}

const query = reactive({
  page: 1,
  size: 10,
  devId: '',
  pointCode: '',
  start: '',
  end: ''
})

async function loadDevices() {
  devices.value = (await getAllDevices()) || []
  devices.value.forEach((d) => {
    deviceMap[d.devId] = d
  })
}

async function handleDeviceChange() {
  points.value = []
  query.pointCode = ''
  if (!query.devId) return
  const device = deviceMap[query.devId]
  if (!device || device.id == null) return
  const data = await getDevicePoints(device.id)
  points.value = data || []
}

async function loadList() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.devId) delete params.devId
    if (!params.pointCode) delete params.pointCode
    if (timeRange.value?.length === 2) {
      params.start = timeRange.value[0]
      params.end = timeRange.value[1]
    } else {
      delete params.start
      delete params.end
    }
    const data = await getData(params)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadList()
}

function handleReset() {
  query.devId = ''
  query.pointCode = ''
  timeRange.value = []
  points.value = []
  query.page = 1
  loadList()
}

onMounted(loadDevices)
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
