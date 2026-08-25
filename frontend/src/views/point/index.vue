<template>
  <div class="page">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="所属设备">
          <el-select
            v-model="query.deviceId"
            placeholder="全部设备"
            clearable
            filterable
            style="width: 220px"
          >
            <el-option v-for="d in devices" :key="d.id" :label="d.deviceName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.pointType" placeholder="全部" clearable style="width: 150px">
            <el-option label="采集(AI)" :value="1" />
            <el-option label="控制(DO)" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button v-perm="'point:add'" type="primary" :icon="Plus" @click="openDialog()">
          新增点位
        </el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="所属设备" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ deviceName(row.deviceId) }}</template>
        </el-table-column>
        <el-table-column prop="pointCode" label="点位编码" width="110" />
        <el-table-column prop="pointName" label="点位名称" min-width="120" show-overflow-tooltip />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="POINT_TYPE[row.pointType]?.type || 'info'">
              {{ POINT_TYPE[row.pointType]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据类型" width="90" align="center">
          <template #default="{ row }">{{ DATA_TYPE[row.dataType] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" align="center">
          <template #default="{ row }">{{ row.unit || '-' }}</template>
        </el-table-column>
        <el-table-column label="量程" width="130" align="center">
          <template #default="{ row }">
            {{ row.minValue ?? '-' }} ~ {{ row.maxValue ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="defaultValue" label="缺省值" width="90" align="center">
          <template #default="{ row }">{{ row.defaultValue ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'point:edit'" link type="primary" @click="openDialog(row)">
              编辑
            </el-button>
            <el-button v-perm="'point:delete'" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
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

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑点位' : '新增点位'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属设备" prop="deviceId">
          <el-select v-model="form.deviceId" placeholder="请选择设备" filterable style="width: 100%">
            <el-option v-for="d in devices" :key="d.id" :label="d.deviceName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="点位编码" prop="pointCode">
          <el-input v-model="form.pointCode" placeholder="如 AI1 / DO1" />
        </el-form-item>
        <el-form-item label="点位名称" prop="pointName">
          <el-input v-model="form.pointName" placeholder="请输入点位名称" />
        </el-form-item>
        <el-form-item label="点位类型" prop="pointType">
          <el-radio-group v-model="form.pointType">
            <el-radio :value="1">采集(AI)</el-radio>
            <el-radio :value="2">控制(DO)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <el-radio-group v-model="form.dataType">
            <el-radio :value="1">数值</el-radio>
            <el-radio :value="2">布尔</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如 ℃ / mm / W" />
        </el-form-item>
        <el-form-item label="量程">
          <div class="range-inputs">
            <el-input-number v-model="form.minValue" :precision="3" placeholder="下限" style="width: 100%" />
            <span>~</span>
            <el-input-number v-model="form.maxValue" :precision="3" placeholder="上限" style="width: 100%" />
          </div>
        </el-form-item>
        <el-form-item label="缺省值">
          <el-input v-model="form.defaultValue" placeholder="请输入缺省值" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getPoints, createPoint, updatePoint, deletePoint } from '@/api/point'
import { getAllDevices } from '@/api/device'
import { POINT_TYPE, DATA_TYPE } from '@/constants'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const devices = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const deviceMap = computed(() => {
  const m = {}
  devices.value.forEach((d) => (m[d.id] = d))
  return m
})

function deviceName(id) {
  return deviceMap.value[id]?.deviceName || id || '-'
}

const query = reactive({
  page: 1,
  size: 10,
  deviceId: null,
  pointType: null
})

const emptyForm = () => ({
  id: null,
  deviceId: null,
  pointCode: '',
  pointName: '',
  pointType: 1,
  dataType: 1,
  unit: '',
  minValue: null,
  maxValue: null,
  defaultValue: '',
  description: ''
})

const form = reactive(emptyForm())

const rules = {
  deviceId: [{ required: true, message: '请选择所属设备', trigger: 'change' }],
  pointCode: [{ required: true, message: '请输入点位编码', trigger: 'blur' }],
  pointName: [{ required: true, message: '请输入点位名称', trigger: 'blur' }]
}

async function loadDevices() {
  devices.value = (await getAllDevices()) || []
}

async function loadList() {
  loading.value = true
  try {
    const params = { ...query }
    if (params.deviceId === null || params.deviceId === '') delete params.deviceId
    if (params.pointType === null || params.pointType === '') delete params.pointType
    const data = await getPoints(params)
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
  query.deviceId = null
  query.pointType = null
  query.page = 1
  loadList()
}

function openDialog(row) {
  Object.assign(form, emptyForm(), row || {})
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id) {
      await updatePoint(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createPoint(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除点位「${row.pointName}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await deletePoint(row.id)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(async () => {
  await loadDevices()
  await loadList()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.range-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
</style>
