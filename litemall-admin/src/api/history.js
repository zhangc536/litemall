import request from '@/utils/request'

export function listHistory(query) {
  return request({
    url: '/history/list',
    method: 'get',
    params: query
  })
}

export function deleteHistory(data) {
  return request({
    url: '/history/delete',
    method: 'post',
    data
  })
}
