import request from '@/utils/request'

export function listOrders(query) {
  return request({
    url: '/order/list',
    method: 'get',
    params: query
  })
}

export function orderDetail(id) {
  return request({
    url: '/order/detail',
    method: 'get',
    params: { id }
  })
}

export function shipOrder(orderId, logisticsCompany, trackingNo) {
  return request({
    url: '/order/ship',
    method: 'post',
    data: { orderId, logisticsCompany, trackingNo }
  })
}

export function auditOrder(orderId, status, remark) {
  return request({
    url: '/order/audit',
    method: 'post',
    data: { orderId, status, remark }
  })
}

export function refundOrder(orderId, refundAmount) {
  return request({
    url: '/order/refund',
    method: 'post',
    data: { orderId, refundAmount }
  })
}
