var util = require('../../../utils/util.js');
var user = require('../../../utils/user.js');
var api = require('../../../config/api.js');

var app = getApp();
Page({
  data: {
    isLoggingIn: false,
    isUploadingAvatar: false,
    avatarPreviewUrl: '',
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
      avatarPreviewUrl: avatarUrl,
      avatarUrl: '',
      isUploadingAvatar: true
    });
    var that = this;
    wx.uploadFile({
      url: api.StorageUpload,
      filePath: avatarUrl,
      name: 'file',
      success: function(res) {
        var data = null;
        try {
          data = JSON.parse(res.data);
        } catch (e) {
          data = null;
        }
        if (data && data.errno === 0 && data.data && data.data.url) {
          that.setData({
            avatarUrl: data.data.url,
            avatarPreviewUrl: data.data.url
          });
        } else {
          that.setData({
            avatarUrl: ''
          });
          util.showErrorToast('头像上传失败');
        }
      },
      fail: function() {
        that.setData({
          avatarUrl: ''
        });
        util.showErrorToast('头像上传失败');
      },
      complete: function() {
        that.setData({
          isUploadingAvatar: false
        });
      }
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
    if (this.data.isUploadingAvatar) {
      util.showErrorToast('头像上传中');
      return;
    }
    const nickName = (this.data.nickName || '').trim();
    const avatarUrl = this.data.avatarUrl || '';
    const avatarPreviewUrl = this.data.avatarPreviewUrl || '';
    if (!nickName || !avatarPreviewUrl) {
      util.showErrorToast('请先选择头像并填写昵称');
      return;
    }
    if (!avatarUrl) {
      util.showErrorToast('头像上传失败，请重新选择');
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
