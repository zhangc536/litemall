var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');
var user = require('../../../utils/user.js');
var app = getApp();

Page({
  data: {
    aboutShow: true,
    userInfo: {
      nickName: '点击登录',
      avatarUrl: 'http://yanxuan.nosdn.127.net/8945ae63d940cc42406c3f67019c5cb6.png'
    }
  },
  onLoad: function(options) {

  },
  onReady: function() {

  },
  onShow: function() {

    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    const hasProfile = userInfo && userInfo.nickName && userInfo.avatarUrl;
    if (token && hasProfile) {
      app.globalData.hasLogin = true;
      this.setData({
        aboutShow: true,
        userInfo: userInfo
      });
    } else {
      app.globalData.hasLogin = false;
      wx.removeStorageSync('token');
      wx.removeStorageSync('userInfo');
      this.setData({
        aboutShow: true,
        userInfo: {
          nickName: '点击登录',
          avatarUrl: 'http://yanxuan.nosdn.127.net/8945ae63d940cc42406c3f67019c5cb6.png'
        }
      });
    }

  },
  onHide: function() {
    // 页面隐藏

  },
  onUnload: function() {
    // 页面关闭
  },
  goLogin() {
    const userInfo = wx.getStorageSync('userInfo');
    const hasProfile = userInfo && userInfo.nickName && userInfo.avatarUrl;
    if (!app.globalData.hasLogin || !hasProfile) {
      app.globalData.hasLogin = false;
      wx.removeStorageSync('token');
      wx.removeStorageSync('userInfo');
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
    }
  },
  goOrder() {
    if (app.globalData.hasLogin) {
      wx.navigateTo({
        url: "/pages/ucenter/order/order"
      });
    } else {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
    }
  },
  goCoupon() {
    if (app.globalData.hasLogin) {
      wx.navigateTo({
        url: "/pages/ucenter/coupon/coupon"
      });
    } else {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
    };

  },
  goGroupon() {
    if (app.globalData.hasLogin) {
      wx.navigateTo({
        url: "/pages/groupon/myGroupon/myGroupon"
      });
    } else {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
    };
  },
  goCollect() {
    if (app.globalData.hasLogin) {
      wx.navigateTo({
        url: "/pages/ucenter/collect/collect"
      });
    } else {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
    };
  },
  goFootprint() {
    if (app.globalData.hasLogin) {
      wx.navigateTo({
        url: "/pages/ucenter/footprint/footprint"
      });
    } else {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
    };
  },
  goAddress() {
    if (app.globalData.hasLogin) {
      wx.navigateTo({
        url: "/pages/ucenter/address/address"
      });
    } else {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
    };
  },
  exitLogin: function() {
    wx.showModal({
      title: '',
      confirmColor: '#b4282d',
      content: '退出登录？',
      success: function(res) {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.switchTab({
            url: '/pages/index/index'
          });
        }
      }
    })

  }
})
