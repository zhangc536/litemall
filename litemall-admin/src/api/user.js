import request from '@/utils/request'

export function listUser(query) {
  return request({
    url: '/user/list',
    method: 'get',
    params: query
  })
}
