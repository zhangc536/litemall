var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');

Page({
  data: {
    inviteCode: '',
    inviteCodeInput: '',
    isSubmitting: false,
    isLoading: false,
    hasLogin: false,
    inviteQrcodeUrl: '',
    isQrcodeLoading: false
  },
  onLoad: function(options) {
    this.setData({
      inviteCodeInput: ''
    });
  },

  onPullDownRefresh() {
    wx.showNavigationBarLoading()
    this.refreshInviteInfo().finally(() => {
      wx.hideNavigationBarLoading()
      wx.stopPullDownRefresh()
    });
  },

  refreshInviteInfo() {
    const token = wx.getStorageSync('token');
    if (!token) {
      this.setData({
        hasLogin: false,
        inviteCode: '',
        inviteQrcodeUrl: ''
      });
      return Promise.resolve();
    }
    this.setData({
      hasLogin: true
    });
    return Promise.all([this.fetchInviteCode(), this.fetchInviteQrcode()]).catch(() => {
      this.setData({
        hasLogin: false,
        inviteCode: '',
        inviteQrcodeUrl: ''
      });
    });
  },
  fetchInviteCode() {
    if (this.data.isLoading) {
      return Promise.resolve();
    }
    this.setData({
      isLoading: true
    });
    return util.request(api.AuthInfo).then((res) => {
      if (res.errno === 0) {
        const code = res.data.inviteCode ? String(res.data.inviteCode) : '';
        this.setData({
          inviteCode: code
        });
      }
    }).finally(() => {
      this.setData({
        isLoading: false
      });
    });
  },
  bindInviteInput: function(e) {
    this.setData({
      inviteCodeInput: e.detail.value
    });
  },
  fetchInviteQrcode() {
    if (this.data.isQrcodeLoading) {
      return Promise.resolve();
    }
    this.setData({
      isQrcodeLoading: true
    });
    return util.request(api.AuthInviteQrcode).then((res) => {
      if (res.errno === 0) {
        const isObject = res.data && typeof res.data === 'object';
        const code = isObject && (res.data.inviteCode || res.data.code) ? String(res.data.inviteCode || res.data.code) : '';
        const url = isObject ? (res.data.url || '') : (res.data || '');
        const patch = {};
        if (code) {
          patch.inviteCode = code;
        }
        patch.inviteQrcodeUrl = url;
        this.setData(patch);
      }
    }).finally(() => {
      this.setData({
        isQrcodeLoading: false
      });
    });
  },
  submitInvite: function() {
    if (this.data.isSubmitting) {
      return;
    }
    if (!this.data.hasLogin) {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
      return;
    }
    const inviteCode = (this.data.inviteCodeInput || '').trim();
    if (!inviteCode) {
      wx.showModal({
        title: '错误信息',
        content: '请输入邀请码',
        showCancel: false
      });
      return;
    }
    this.setData({
      isSubmitting: true
    });
    util.request(api.AuthBindInviteCode, { inviteCode: inviteCode }, 'POST')
      .then((res) => {
        if (res.errno !== 0) {
          util.showErrorToast(res.errmsg || '绑定失败');
          return;
        }
        wx.removeStorageSync('inviteCode');
        this.setData({
          inviteCodeInput: ''
        });
        wx.showModal({
          title: '绑定成功',
          content: '邀请码绑定成功',
          showCancel: false
        });
      })
      .catch((err) => {
        const message = err && err.errmsg ? err.errmsg : (err && err.errMsg ? err.errMsg : '');
        if (message) {
          util.showErrorToast(message);
        } else {
          util.showErrorToast('绑定失败');
        }
      })
      .finally(() => {
        this.setData({
          isSubmitting: false
        });
      });
  },
  onReady: function() {
    // 页面渲染完成
  },
  onShow: function() {
    // 页面显示
    this.refreshInviteInfo();
  },
  onHide: function() {
    // 页面隐藏
  },
  onUnload: function() {
    // 页面关闭
  },
  onShareAppMessage: function() {
    const inviteCode = this.data.inviteCode;
    const path = inviteCode ? '/pages/index/index?inviteCode=' + inviteCode : '/pages/index/index';
    return {
      title: '邀请你来逛商城',
      path: path
    };
  }
})
