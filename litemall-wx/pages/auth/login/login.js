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
  wxQuickLogin: function() {
    if (this.data.isLoggingIn) {
      return;
    }
    this.doLogin(null, true);
  },
  doLogin: function(userInfo, allowProfileFallback) {
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
      if (allowProfileFallback && message === '请先选择头像和昵称') {
        wx.navigateTo({
          url: '/pages/auth/wxProfile/wxProfile'
        });
        return;
      }
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
  }
})
