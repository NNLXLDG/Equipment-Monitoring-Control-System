import request from './request'

// 设备
export function getDevices(params) {
  return request.get('/devices', { params }).then((r) => r.data)
}

export function getAllDevices() {
  return request.get('/devices/all').then((r) => r.data)
}

export function getDevice(id) {
  return request.get(`/devices/${id}`).then((r) => r.data)
}

export function createDevice(data) {
  return request.post('/devices', data).then((r) => r.data)
}

export function updateDevice(id, data) {
  return request.put(`/devices/${id}`, data).then((r) => r.data)
}

export function deleteDevice(id) {
  return request.delete(`/devices/${id}`).then((r) => r.data)
}

export function getDevicePoints(id) {
  return request.get(`/devices/${id}/points`).then((r) => r.data)
}
