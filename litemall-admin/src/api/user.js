import request from '@/utils/request'

export function listUser(query) {
  return request({
    url: '/user/list',
    method: 'get',
    params: query
  })
}

export function deleteUser(data) {
  return request({
    url: '/user/delete',
    method: 'post',
    data
  })
}

export function unbindIdCard(data) {
  return request({
    url: '/user/unbindidcard',
    method: 'post',
    data
  })
}
