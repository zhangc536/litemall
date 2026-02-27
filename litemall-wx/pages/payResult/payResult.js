var util = require('../../utils/util.js');
var api = require('../../config/api.js');

var app = getApp();
Page({
  data: {
    status: false,
    orderId: 0,
    isPointOrder: false
  },
  onLoad: function(options) {
    this.setData({
      orderId: options.orderId,
      status: options.status === '1' ? true : false,
      isPointOrder: options.isPointOrder === '1' ? true : false
    })
  },
  onReady: function() {

  },
  onShow: function() {

  },
  onHide: function() {

  },
  onUnload: function() {

  },
  uploadVoucher() {
    wx.navigateTo({
      url: '/pages/payVoucher/payVoucher?orderId=' + this.data.orderId
    });
  }
})
