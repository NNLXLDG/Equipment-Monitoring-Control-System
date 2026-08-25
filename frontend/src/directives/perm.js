// 全局自定义指令：按钮级权限控制
// 用法：<el-button v-perm="'device:add'">新增</el-button>
import { useUserStore } from '@/stores/user'

export default {
  mounted(el, binding) {
    const value = binding.value
    if (!value) return
    const store = useUserStore()
    if (!store.hasPerm(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
