<template>
  <div class="page">
    <el-row :gutter="16">
      <!-- 左侧设备选择与控制点 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>控制面板</span>
              <el-select
                v-model="selectedDevId"
                placeholder="选择设备"
                filterable
                size="small"
                style="width: 180px"
                @change="handleDeviceChange"
              >
                <el-option v-for="d in devices" :key="d.devId" :label="d.deviceName" :value="d.devId" />
              </el-select>
            </div>
          </template>

          <div v-if="controlPoints.length" class="control-list">
            <div v-for="p in controlPoints" :key="p.pointCode" class="control-item">
              <div class="control-info">
                <div class="control-name">{{ p.pointName }}</div>
                <div class="control-code">{{ p.pointCode }}</div>
                <div class="control-desc">{{ p.description || p.unit || '' }}</div>
              </div>
              <div class="control-input">
                <el-switch
                  v-if="p.dataType === 2"
                  v-model="controls[p.pointCode]"
                  active-value="1"
                  inactive-value="0"
                />
                <el-input
                  v-else
                  v-model="controls[p.pointCode]"
                  placeholder="目标值"
                  style="width: 120px"
                />
              </div>
            </div>
            <el-button
              v-perm="'control:send'"
              type="primary"
              :icon="Promotion"
              :loading="sending"
              class="send-btn"
              @click="handleSend"
            >
              下发控制
            </el-button>
          </div>
          <el-empty v-else description="该设备暂无控制点(DO)" />

          <el-alert
            title="点击「下发控制」将封装 JSON 控制帧下发到设备"
            type="info"
            :closable="false"
            show-icon
            class="tips"
          />
        </el-card>
      </el-col>

      <!-- 右侧控制记录 -->
      <el-col :xs="24" :md="16">
        <el-card shadow="never" header="控制记录">
          <el-table v-loading="loading" :data="records" border stripe>
            <el-table-column prop="devId" label="设备编号" min-width="150" show-overflow-tooltip />
            <el-table-column prop="pointCode" label="点位编码" width="100" />
            <el-table-column prop="pointName" label="点位名称" width="110" show-overflow-tooltip />
            <el-table-column prop="value" label="目标值" width="90" align="center" />
            <el-table-column prop="operatorName" label="操作人" width="100" show-overflow-tooltip />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="CONTROL_STATUS[row.status]?.type || 'info'">
                  {{ CONTROL_STATUS[row.status]?.text || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="下发时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            class="pagination"
            @size-change="loadRecords"
            @current-change="loadRecords"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { sendControl, getControlRecords } from '@/api/control'
import { getAllDevices, getDevicePoints } from '@/api/device'
import { CONTROL_STATUS } from '@/constants'
import { formatDateTime } from '@/utils'

const devices = ref([])
const controlPoints = ref([])
const selectedDevId = ref('')
const controls = reactive({})
const sending = ref(false)
const deviceMap = {}

const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

async function loadDevices() {
  devices.value = (await getAllDevices()) || []
  devices.value.forEach((d) => (deviceMap[d.devId] = d))
  if (devices.value.length) {
    selectedDevId.value = devices.value[0].devId
    await handleDeviceChange()
  }
}

async function handleDeviceChange() {
  controlPoints.value = []
  Object.keys(controls).forEach((k) => delete controls[k])
  if (!selectedDevId.value) return
  const device = deviceMap[selectedDevId.value]
  if (!device || device.id == null) return
  const data = await getDevicePoints(device.id)
  const dos = (data || []).filter((p) => p.pointType === 2)
  controlPoints.value = dos
  dos.forEach((p) => {
    controls[p.pointCode] = p.defaultValue ?? (p.dataType === 2 ? '0' : '')
  })
  page.value = 1
  loadRecords()
}

async function handleSend() {
  if (!selectedDevId.value || !controlPoints.value.length) {
    ElMessage.warning('请先选择设备')
    return
  }
  const data = {}
  controlPoints.value.forEach((p) => {
    data[p.pointCode] = String(controls[p.pointCode] ?? '')
  })
  sending.value = true
  try {
    const res = await sendControl({ devId: selectedDevId.value, data })
    ElMessage.success(res?.message || '控制指令已下发')
    page.value = 1
    loadRecords()
  } finally {
    sending.value = false
  }
}

async function loadRecords() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (selectedDevId.value) params.devId = selectedDevId.value
    const data = await getControlRecords(params)
    records.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadDevices)
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.control-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.control-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
}

.control-name {
  font-weight: 600;
  color: #303133;
}

.control-code {
  font-size: 12px;
  color: #909399;
}

.control-desc {
  font-size: 12px;
  color: #c0c4cc;
}

.send-btn {
  width: 100%;
  margin-top: 4px;
}

.tips {
  margin-top: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
