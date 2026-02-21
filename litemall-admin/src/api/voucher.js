import request from '@/utils/request'

export function listVouchers(query) {
  return request({
    url: '/order/voucher/list',
    method: 'get',
    params: query
  })
}

export function uploadVoucher(orderId, file) {
  const formData = new FormData()
  formData.append('orderId', orderId)
  formData.append('file', file)
  return request({
    url: '/order/voucher/upload',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: formData
  })
}

export function auditOrder(orderId, status, remark) {
  return request({
    url: '/order/audit',
    method: 'post',
    data: { orderId, status, remark }
  })
}

export function shipOrder(orderId, logisticsCompany, trackingNo) {
  return request({
    url: '/order/ship',
    method: 'post',
    data: { orderId, logisticsCompany, trackingNo }
  })
}
