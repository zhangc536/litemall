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
      orderId: options.orderId || 0,
      status: options.status === '1' ? true : false,
      isPointOrder: options.isPointOrder === '1' ? true : false
    })
    this.verifyOrderStatus();
  },
  verifyOrderStatus: function() {
    var that = this;
    if (!that.data.orderId) {
      return;
    }
    util.request(api.OrderDetail + that.data.orderId, {}, 'GET').then(function(res) {
      if (res.errno === 0 && res.data && res.data.orderInfo) {
        var orderStatus = res.data.orderInfo.orderStatus;
        var isPaid = orderStatus === 201 || orderStatus === 301 || orderStatus === 401;
        that.setData({
          status: isPaid
        });
      }
    }).catch(function(err) {
    });
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
