import request from './request'

// 角色
export function getRoles(params) {
  return request.get('/roles', { params }).then((r) => r.data)
}

export function getAllRoles() {
  return request.get('/roles').then((r) => r.data)
}

export function createRole(data) {
  return request.post('/roles', data).then((r) => r.data)
}

export function updateRole(id, data) {
  return request.put(`/roles/${id}`, data).then((r) => r.data)
}

export function deleteRole(id) {
  return request.delete(`/roles/${id}`).then((r) => r.data)
}

export function getRolePermissions(id) {
  return request.get(`/roles/${id}/permissions`).then((r) => r.data)
}

export function updateRolePermissions(id, permIds) {
  return request.put(`/roles/${id}/permissions`, permIds).then((r) => r.data)
}
