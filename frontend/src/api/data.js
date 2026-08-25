import request from './request'

// 采集数据
export function getData(params) {
  return request.get('/data', { params }).then((r) => r.data)
}

export function getLatestData(devId) {
  return request.get('/data/latest', { params: { devId } }).then((r) => r.data)
}

export function getChartData(params) {
  return request.get('/data/chart', { params }).then((r) => r.data)
}
