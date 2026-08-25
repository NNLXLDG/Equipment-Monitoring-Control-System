import request from './request'

// 看板/报表
export function getSummary() {
  return request.get('/dashboard/summary').then((r) => r.data)
}

export function getDeviceTypeDist() {
  return request.get('/dashboard/deviceTypeDist').then((r) => r.data)
}

export function getDataTrend(params) {
  return request.get('/dashboard/dataTrend', { params }).then((r) => r.data)
}

export function getPointStats(params) {
  return request.get('/dashboard/pointStats', { params }).then((r) => r.data)
}
