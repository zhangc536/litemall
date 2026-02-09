var api = require('../../../config/api.js');
var util = require('../../../utils/util.js');
var user = require('../../../utils/user.js');

var app = getApp();
Page({
  data: {
    isLoggingIn: false
  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
    // 页面渲染完成
    app.globalData.hasLogin = false;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  },
  onReady: function() {

  },
  onShow: function() {
    // 页面显示
  },
  onHide: function() {
    // 页面隐藏

  },
  onUnload: function() {
    // 页面关闭

  },
  wxLogin: function(e) {
    if (this.data.isLoggingIn) {
      return;
    }
    if (!wx.getUserProfile) {
      util.showErrorToast('当前微信版本不支持获取用户信息');
      return;
    }
    wx.getUserProfile({
      desc: '用于完善会员资料', // 声明获取用户个人信息后的用途，后续会展示在弹窗中，请谨慎填写
      success: (res) => {
        this.doLogin(res.userInfo)
      },
      fail: (err) => {
        util.showErrorToast(err && err.errMsg ? err.errMsg : '微信登录失败');
      }
    })
  },
  doLogin: function(userInfo) {
    this.setData({
      isLoggingIn: true
    });
    user.loginByWeixin(userInfo).then(res => {
      app.globalData.hasLogin = true;
      wx.reLaunch({
        url: '/pages/index/index'
      })
    }).catch((err) => {
      app.globalData.hasLogin = false;
      const message = err && err.errmsg ? err.errmsg : (err && err.errMsg ? err.errMsg : '');
      if (message) {
        wx.showModal({
          title: '登录失败',
          content: message,
          showCancel: false
        });
      } else {
        util.showErrorToast('微信登录失败');
      }
    }).finally(() => {
      this.setData({
        isLoggingIn: false
      });
    });
  },
  accountLogin: function() {
    wx.navigateTo({
      url: "/pages/auth/accountLogin/accountLogin"
    });
  }
})
