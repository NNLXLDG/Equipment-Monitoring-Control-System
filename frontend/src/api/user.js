import request from './request'

// 用户
export function getUsers(params) {
  return request.get('/users', { params }).then((r) => r.data)
}

export function getUser(id) {
  return request.get(`/users/${id}`).then((r) => r.data)
}

export function createUser(data) {
  return request.post('/users', data).then((r) => r.data)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data).then((r) => r.data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`).then((r) => r.data)
}

export function updateUserStatus(id, status) {
  return request.put(`/users/${id}/status`, { status }).then((r) => r.data)
}

export function resetUserPassword(id, password) {
  return request.put(`/users/${id}/password`, { password }).then((r) => r.data)
}
