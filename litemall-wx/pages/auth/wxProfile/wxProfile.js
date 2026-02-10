var util = require('../../../utils/util.js');
var user = require('../../../utils/user.js');

var app = getApp();
Page({
  data: {
    isLoggingIn: false,
    avatarUrl: '',
    nickName: ''
  },
  onLoad: function() {
    app.globalData.hasLogin = false;
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
  wxLogin: function() {
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
