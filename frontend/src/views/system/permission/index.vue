<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button v-perm="'permission:add'" type="primary" :icon="Plus" @click="openDialog()">
          新增菜单
        </el-button>
        <el-button :icon="Refresh" @click="loadTree">刷新</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tree"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="PERM_TYPE[row.type]?.type || 'info'">
              {{ PERM_TYPE[row.type]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="120" show-overflow-tooltip />
        <el-table-column prop="component" label="组件" width="160" show-overflow-tooltip />
        <el-table-column prop="perm" label="权限标识" width="140" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="openDialog(null, row)">新增子项</el-button>
            <el-button v-perm="'permission:edit'" link type="primary" @click="openDialog(row)">
              编辑
            </el-button>
            <el-button v-perm="'permission:delete'" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑菜单' : '新增菜单'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            node-key="id"
            check-strictly
            default-expand-all
            placeholder="选择上级菜单（留空为根目录）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" placeholder="如 /monitor 或 device" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="form.component" placeholder="如 device/index" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.perm" placeholder="如 device:add" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 Cpu" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission
} from '@/api/permission'
import { PERM_TYPE } from '@/constants'

const loading = ref(false)
const saving = ref(false)
const tree = ref([])
const dialogVisible = ref(false)
const formRef = ref()

// 上级菜单选项：根 + 目录/菜单（不含按钮）
const parentOptions = computed(() => {
  const root = [{ id: 0, name: '根目录', children: [] }]
  const flatten = (list) => {
    const res = []
    for (const item of list || []) {
      if (item.type !== 3) {
        const copy = { ...item, children: flatten(item.children) }
        res.push(copy)
      }
    }
    return res
  }
  return [{ ...root[0], children: flatten(tree.value) }]
})

const emptyForm = () => ({
  id: null,
  parentId: 0,
  name: '',
  type: 2,
  path: '',
  component: '',
  perm: '',
  icon: '',
  sort: 0
})

const form = reactive(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

async function loadTree() {
  loading.value = true
  try {
    tree.value = (await getPermissionTree()) || []
  } finally {
    loading.value = false
  }
}

function openDialog(row, parent) {
  if (row) {
    Object.assign(form, emptyForm(), {
      id: row.id,
      parentId: row.parentId ?? 0,
      name: row.name,
      type: row.type,
      path: row.path || '',
      component: row.component || '',
      perm: row.perm || '',
      icon: row.icon || '',
      sort: row.sort || 0
    })
  } else {
    Object.assign(form, emptyForm(), { parentId: parent ? parent.id : 0 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { ...form }
    if (payload.parentId === 0 || payload.parentId == null) {
      payload.parentId = 0
    }
    if (form.id) {
      await updatePermission(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createPermission(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadTree()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await deletePermission(row.id)
  ElMessage.success('删除成功')
  loadTree()
}

onMounted(loadTree)
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
</style>
