var util = require('../../utils/util.js');
var api = require('../../config/api.js');

var app = getApp();

Page({
  data: {
    checkedGoodsList: [],
    checkedAddress: {},
    goodsTotalPrice: 0.00,
    freightPrice: 0.00,
    grouponPrice: 0.00,
    orderTotalPrice: 0.00,
    actualPrice: 0.00,
    cartId: 0,
    addressId: 0,
    message: '',
    grouponLinkId: 0,
    grouponRulesId: 0,
    hasIdCard: false
  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
    if (options) {
      const cartId = options.cartId ? parseInt(options.cartId) : 0;
      const addressId = options.addressId ? parseInt(options.addressId) : 0;
      const grouponRulesId = options.grouponRulesId ? parseInt(options.grouponRulesId) : 0;
      const grouponLinkId = options.grouponLinkId ? parseInt(options.grouponLinkId) : 0;
      this.setData({
        cartId: isNaN(cartId) ? 0 : cartId,
        addressId: isNaN(addressId) ? 0 : addressId,
        grouponRulesId: isNaN(grouponRulesId) ? 0 : grouponRulesId,
        grouponLinkId: isNaN(grouponLinkId) ? 0 : grouponLinkId
      });
      try {
        wx.setStorageSync('cartId', this.data.cartId);
        wx.setStorageSync('addressId', this.data.addressId);
        wx.setStorageSync('grouponRulesId', this.data.grouponRulesId);
        wx.setStorageSync('grouponLinkId', this.data.grouponLinkId);
      } catch (e) {}
    }
  },

  //获取checkou信息
  getCheckoutInfo: function() {
    let that = this;
    util.request(api.CartCheckout, {
      cartId: that.data.cartId,
      addressId: that.data.addressId,
      grouponRulesId: that.data.grouponRulesId
    }).then(function(res) {
      if (res.errno === 0) {
        that.setData({
          checkedGoodsList: res.data.checkedGoodsList,
          checkedAddress: res.data.checkedAddress,
          actualPrice: res.data.actualPrice,
          grouponPrice: res.data.grouponPrice,
          freightPrice: res.data.freightPrice,
          goodsTotalPrice: res.data.goodsTotalPrice,
          orderTotalPrice: res.data.orderTotalPrice,
          addressId: res.data.addressId,
          grouponRulesId: res.data.grouponRulesId,
        });
      }
      wx.hideLoading();
    });
  },
  checkIdCard: function() {
    let that = this;
    return new Promise(function(resolve, reject) {
      util.request(api.UserIdCard).then(function(res) {
        if (res.errno === 0 && res.data && res.data.idCard) {
          that.setData({ hasIdCard: true });
          resolve(true);
        } else {
          that.setData({ hasIdCard: false });
          resolve(false);
        }
      }).catch(function() {
        that.setData({ hasIdCard: false });
        resolve(false);
      });
    });
  },
  selectAddress() {
    wx.navigateTo({
      url: '/pages/ucenter/address/address?from=checkout',
    })
  },
  bindMessageInput: function(e) {
    this.setData({
      message: e.detail.value
    });
  },
  onReady: function() {
    // 页面渲染完成

  },
  onShow: function() {
    // 页面显示
    wx.showLoading({
      title: '加载中...',
    });
    try {
      var cartId = wx.getStorageSync('cartId');
      if (cartId === "") {
        cartId = 0;
      }
      var addressId = wx.getStorageSync('addressId');
      if (addressId === "") {
        addressId = 0;
      }
      var grouponRulesId = wx.getStorageSync('grouponRulesId');
      if (grouponRulesId === "") {
        grouponRulesId = 0;
      }
      var grouponLinkId = wx.getStorageSync('grouponLinkId');
      if (grouponLinkId === "") {
        grouponLinkId = 0;
      }

      this.setData({
        cartId: cartId,
        addressId: addressId,
        grouponRulesId: grouponRulesId,
        grouponLinkId: grouponLinkId
      });

    } catch (e) {
      // Do something when catch error
      console.log(e);
    }

    this.getCheckoutInfo();
  },
  onHide: function() {
    // 页面隐藏

  },
  onUnload: function() {
    // 页面关闭

  },
  submitOrder: function() {
    let that = this;
    if (this.data.addressId <= 0) {
      util.showErrorToast('请选择收货地址');
      return false;
    }
    this.checkIdCard().then(function(hasIdCard) {
      if (!hasIdCard) {
        wx.showModal({
          title: '提示',
          content: '购买商品前需要先完成实名认证，是否立即前往？',
          confirmText: '去认证',
          cancelText: '取消',
          success: function(res) {
            if (res.confirm) {
              wx.navigateTo({
                url: '/pages/ucenter/idcard/idcard'
              });
            }
          }
        });
        return;
      }
      that.doSubmitOrder();
    });
  },
  doSubmitOrder: function() {
    let that = this;
    util.request(api.OrderSubmit, {
      cartId: that.data.cartId,
      addressId: that.data.addressId,
      message: that.data.message,
      grouponRulesId: that.data.grouponRulesId,
      grouponLinkId: that.data.grouponLinkId
    }, 'POST').then(res => {
      if (res.errno === 0) {
        const orderId = res.data.orderId;
        wx.redirectTo({
          url: '/pages/payVoucher/payVoucher?orderId=' + orderId
        });
      } else {
        util.showErrorToast(res.errmsg);
      }
    });
  }
});
