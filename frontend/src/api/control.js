import request from './request'

// 控制
export function sendControl(data) {
  return request.post('/control/send', data).then((r) => r.data)
}

export function getControlRecords(params) {
  return request.get('/control/records', { params }).then((r) => r.data)
}
