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
