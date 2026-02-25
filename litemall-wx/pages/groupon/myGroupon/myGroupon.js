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
    isQrcodeLoading: false,
    hasBoundInviter: false,
    inviter: null
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
        inviteQrcodeUrl: '',
        hasBoundInviter: false,
        inviter: null
      });
      return Promise.resolve();
    }
    this.setData({
      hasLogin: true
    });
    return Promise.all([this.fetchInviteCode(), this.fetchInviteQrcode(), this.fetchInviterInfo()]).catch(() => {
      this.setData({
        hasLogin: false,
        inviteCode: '',
        inviteQrcodeUrl: '',
        hasBoundInviter: false,
        inviter: null
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
  fetchInviterInfo() {
    return util.request(api.AuthInviterInfo).then((res) => {
      if (res.errno === 0) {
        this.setData({
          hasBoundInviter: res.data.hasBound || false,
          inviter: res.data.inviter || null
        });
      }
    }).catch(() => {
      this.setData({
        hasBoundInviter: false,
        inviter: null
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
    if (this.data.hasBoundInviter) {
      wx.showModal({
        title: '提示',
        content: '您已绑定邀请人，无法再次绑定',
        showCancel: false
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
        this.fetchInviterInfo();
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
  },
  onShow: function() {
    this.refreshInviteInfo();
  },
  onHide: function() {
  },
  onUnload: function() {
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
