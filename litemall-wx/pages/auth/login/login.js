var api = require('../../../config/api.js');
var util = require('../../../utils/util.js');
var user = require('../../../utils/user.js');

var app = getApp();
Page({
  data: {
    isLoggingIn: false,
    avatarUrl: '',
    nickName: ''
  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
    // 页面渲染完成
    app.globalData.hasLogin = false;
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && userInfo.nickName && userInfo.avatarUrl) {
      this.setData({
        avatarUrl: userInfo.avatarUrl,
        nickName: userInfo.nickName
      });
    }
  },
  onReady: function() {

  },
  onShow: function() {
    // 页面显示
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && userInfo.nickName && userInfo.avatarUrl) {
      if (userInfo.avatarUrl !== this.data.avatarUrl || userInfo.nickName !== this.data.nickName) {
        this.setData({
          avatarUrl: userInfo.avatarUrl,
          nickName: userInfo.nickName
        });
      }
    }
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
    const nickName = (this.data.nickName || '').trim();
    const avatarUrl = this.data.avatarUrl || '';
    if (!nickName || !avatarUrl) {
      util.showErrorToast('请先选择头像并填写昵称');
      return;
    }
    this.doLogin({
      nickName: nickName,
      avatarUrl: avatarUrl
    })
  },
  wxQuickLogin: function() {
    if (this.data.isLoggingIn) {
      return;
    }
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && userInfo.nickName && userInfo.avatarUrl) {
      this.doLogin({
        nickName: userInfo.nickName,
        avatarUrl: userInfo.avatarUrl
      })
      return;
    }
    util.showErrorToast('请先选择头像并填写昵称');
  },
  onChooseAvatar: function(e) {
    const avatarUrl = e && e.detail ? e.detail.avatarUrl : '';
    if (!avatarUrl) {
      util.showErrorToast('未获取到头像');
      return;
    }
    this.setData({
      avatarUrl: avatarUrl
    });
  },
  onNicknameInput: function(e) {
    this.setData({
      nickName: e.detail.value
    });
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
  }
})
