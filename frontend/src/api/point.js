import request from './request'

// 设备点位
export function getPoints(params) {
  return request.get('/points', { params }).then((r) => r.data)
}

export function getPoint(id) {
  return request.get(`/points/${id}`).then((r) => r.data)
}

export function createPoint(data) {
  return request.post('/points', data).then((r) => r.data)
}

export function updatePoint(id, data) {
  return request.put(`/points/${id}`, data).then((r) => r.data)
}

export function deletePoint(id) {
  return request.delete(`/points/${id}`).then((r) => r.data)
}
