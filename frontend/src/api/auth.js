import request from './request'

// 认证
export function login(data) {
  return request.post('/auth/login', data).then((r) => r.data)
}

export function getMe() {
  return request.get('/auth/me').then((r) => r.data)
}

export function logout() {
  return request.post('/auth/logout')
}
