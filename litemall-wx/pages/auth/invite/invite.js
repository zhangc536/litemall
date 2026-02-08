var api = require('../../../config/api.js');
var util = require('../../../utils/util.js');

Page({
  data: {
    inviteCode: '',
    isSubmitting: false
  },
  onLoad: function() {
    const inviteCode = wx.getStorageSync('inviteCode') || '';
    if (inviteCode) {
      this.setData({
        inviteCode: inviteCode
      });
    }
  },
  bindInviteInput: function(e) {
    this.setData({
      inviteCode: e.detail.value
    });
  },
  submitInvite: function() {
    if (this.data.isSubmitting) {
      return;
    }
    const inviteCode = (this.data.inviteCode || '').trim();
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
        wx.showModal({
          title: '绑定成功',
          content: '邀请码绑定成功',
          showCancel: false,
          success: () => {
            const pages = getCurrentPages();
            if (pages.length > 1) {
              wx.navigateBack({ delta: 1 });
            } else {
              wx.switchTab({ url: '/pages/index/index' });
            }
          }
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
  }
});
