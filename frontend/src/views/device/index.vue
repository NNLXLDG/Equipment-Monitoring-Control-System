<template>
  <div class="page">
    <el-card shadow="never">
      <!-- 搜索区 -->
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="编号/名称/型号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="query.deviceType"
            placeholder="全部"
            clearable
            filterable
            allow-create
            default-first-option
            style="width: 150px"
          >
            <el-option v-for="t in deviceTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作区 -->
      <div class="toolbar">
        <el-button v-perm="'device:add'" type="primary" :icon="Plus" @click="openDialog()">
          新增设备
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="devId" label="设备编号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="deviceName" label="设备名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="deviceType" label="类型" width="110" />
        <el-table-column prop="model" label="型号" width="110" />
        <el-table-column prop="manufacturer" label="厂商" min-width="120" show-overflow-tooltip />
        <el-table-column prop="location" label="安装位置" min-width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="DEVICE_STATUS[row.status]?.type || 'info'">
              {{ DEVICE_STATUS[row.status]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="安装日期" width="120">
          <template #default="{ row }">{{ formatDate(row.installDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'device:edit'" link type="primary" @click="openDialog(row)">
              编辑
            </el-button>
            <el-button v-perm="'device:delete'" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑设备' : '新增设备'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="设备编号" prop="devId">
          <el-input v-model="form.devId" placeholder="设备唯一标识（MQTT 主题用）" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="form.deviceType"
            placeholder="请选择或输入"
            filterable
            allow-create
            default-first-option
            style="width: 100%"
          >
            <el-option v-for="t in deviceTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="厂商">
          <el-input v-model="form.manufacturer" placeholder="请输入厂商" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="form.location" placeholder="请输入安装位置" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">在线</el-radio>
            <el-radio :value="0">离线</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="安装日期">
          <el-date-picker
            v-model="form.installDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getDevices, createDevice, updateDevice, deleteDevice } from '@/api/device'
import { DEVICE_STATUS } from '@/constants'
import { formatDate } from '@/utils'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const deviceTypes = ['电热设备', '制冷设备', '动力设备', '其他']

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: null,
  deviceType: ''
})

const emptyForm = () => ({
  id: null,
  devId: '',
  deviceName: '',
  deviceType: '',
  model: '',
  manufacturer: '',
  location: '',
  status: 0,
  installDate: '',
  description: ''
})

const form = reactive(emptyForm())

const rules = {
  devId: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择或输入设备类型', trigger: 'change' }]
}

async function loadList() {
  loading.value = true
  try {
    const params = { ...query }
    if (params.status === null || params.status === '') delete params.status
    if (!params.deviceType) delete params.deviceType
    if (!params.keyword) delete params.keyword
    const data = await getDevices(params)
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
  query.keyword = ''
  query.status = null
  query.deviceType = ''
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
      await updateDevice(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createDevice(form)
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
    await ElMessageBox.confirm(`确定删除设备「${row.deviceName}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await deleteDevice(row.id)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
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
</style>
