var api = require('../../../config/api.js');
var util = require('../../../utils/util.js');
var user = require('../../../utils/user.js');

var app = getApp();
Page({
  data: {
    isLoggingIn: false,
    isUploadingAvatar: false,
    avatarUrl: '',
    nickName: ''
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
    if (this.data.isUploadingAvatar) {
      util.showErrorToast('头像上传中');
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
  onChooseAvatar: function(e) {
    const avatarUrl = e && e.detail ? e.detail.avatarUrl : '';
    if (!avatarUrl) {
      util.showErrorToast('未获取到头像');
      return;
    }
    this.setData({
      avatarUrl: avatarUrl,
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
            avatarUrl: data.data.url
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
  doLogin: function(userInfo) {
    this.setData({
      isLoggingIn: true
    });
    user.loginByWeixin(userInfo).then(res => {
      app.globalData.hasLogin = true;
      wx.navigateBack({
        delta: 1
      })
    }).catch((err) => {
      app.globalData.hasLogin = false;
      util.showErrorToast('微信登录失败');
    }).finally(() => {
      this.setData({
        isLoggingIn: false
      });
    });
  }
})
