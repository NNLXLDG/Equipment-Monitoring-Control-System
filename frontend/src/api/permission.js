import request from './request'

// 权限/菜单
export function getPermissionTree() {
  return request.get('/permissions/tree').then((r) => r.data)
}

export function getPermissions() {
  return request.get('/permissions').then((r) => r.data)
}

export function createPermission(data) {
  return request.post('/permissions', data).then((r) => r.data)
}

export function updatePermission(id, data) {
  return request.put(`/permissions/${id}`, data).then((r) => r.data)
}

export function deletePermission(id) {
  return request.delete(`/permissions/${id}`).then((r) => r.data)
}
