import request from '@/utils/request'

export function listPointGoods(query) {
  return request({
    url: '/pointGoods/list',
    method: 'get',
    params: query
  })
}

export function createPointGoods(data) {
  return request({
    url: '/pointGoods/create',
    method: 'post',
    data
  })
}

export function updatePointGoods(data) {
  return request({
    url: '/pointGoods/update',
    method: 'post',
    data
  })
}

export function deletePointGoods(data) {
  return request({
    url: '/pointGoods/delete',
    method: 'post',
    data
  })
}
