import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/modules/user'

/**
 * v-permission 自定义指令
 * 根据用户角色控制元素显示/隐藏
 *
 * 用法:
 *   <el-button v-permission="['ADMIN']">删除</el-button>
 *   <el-button v-permission="['ADMIN', 'STORE']">编辑</el-button>
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const requiredRoles = binding.value as string[]
    if (!requiredRoles || requiredRoles.length === 0) return

    const userStore = useUserStore()
    const userRole = userStore.userRole

    if (!requiredRoles.includes(userRole)) {
      el.remove()
    }
  }
}
