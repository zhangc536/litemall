import request from '@/utils/request'

export function listLevels() {
  return request({
    url: '/userLevel/list',
    method: 'get'
  })
}

export function createLevel(data) {
  return request({
    url: '/userLevel/create',
    method: 'post',
    data
  })
}

export function updateLevel(data) {
  return request({
    url: '/userLevel/update',
    method: 'post',
    data
  })
}

export function deleteLevel(id) {
  return request({
    url: '/userLevel/delete',
    method: 'post',
    data: { id }
  })
}
