var util = require('../../utils/util.js');
var api = require('../../config/api.js');

Page({
  data: {
    orderId: 0,
    orderInfo: {},
    payVoucher: '',
    hasVoucher: false,
    submitting: false
  },

  onLoad: function(options) {
    this.setData({
      orderId: options.orderId
    });
    this.getOrderInfo();
  },

  getOrderInfo: function() {
    let that = this;
    util.request(api.OrderDetail, {
      orderId: that.data.orderId
    }).then(function(res) {
      if (res.errno === 0) {
        that.setData({
          orderInfo: res.data.orderInfo
        });
      }
    });
  },

  chooseImage: function() {
    let that = this;
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: function(res) {
        const tempFilePaths = res.tempFilePaths;
        that.setData({
          payVoucher: tempFilePaths[0],
          hasVoucher: true
        });
      }
    });
  },

  previewImage: function() {
    if (this.data.payVoucher) {
      wx.previewImage({
        current: this.data.payVoucher,
        urls: [this.data.payVoucher]
      });
    }
  },

  deleteImage: function() {
    this.setData({
      payVoucher: '',
      hasVoucher: false
    });
  },

  submitVoucher: function() {
    let that = this;
    
    if (!that.data.hasVoucher) {
      wx.showToast({
        title: '请先上传支付凭证',
        icon: 'none'
      });
      return;
    }

    that.setData({ submitting: true });

    wx.uploadFile({
      url: api.UploadPayVoucher,
      filePath: that.data.payVoucher,
      name: 'file',
      formData: {
        orderId: that.data.orderId
      },
      success: function(res) {
        const data = JSON.parse(res.data);
        if (data.errno === 0) {
          wx.showToast({
            title: '提交成功',
            icon: 'success'
          });
          setTimeout(function() {
            wx.redirectTo({
              url: '/pages/ucenter/orderDetail/orderDetail?id=' + that.data.orderId
            });
          }, 1500);
        } else {
          wx.showToast({
            title: data.errmsg || '提交失败',
            icon: 'none'
          });
        }
      },
      fail: function() {
        wx.showToast({
          title: '上传失败，请重试',
          icon: 'none'
        });
      },
      complete: function() {
        that.setData({ submitting: false });
      }
    });
  }
});
