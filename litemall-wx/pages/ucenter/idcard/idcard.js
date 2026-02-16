var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');

Page({
  data: {
    realName: '',
    idCard: '',
    bound: false,
    loading: false
  },

  onLoad: function() {
    this.checkIdCard();
  },

  onShow: function() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
      return;
    }
  },

  checkIdCard: function() {
    let that = this;
    util.request(api.UserIdCard).then(function(res) {
      if (res.errno === 0) {
        that.setData({
          realName: res.data.realName || '',
          idCard: res.data.idCard || '',
          bound: res.data.bound || false
        });
      }
    });
  },

  onRealNameInput: function(e) {
    this.setData({
      realName: e.detail.value
    });
  },

  onIdCardInput: function(e) {
    this.setData({
      idCard: e.detail.value
    });
  },

  submit: function() {
    let that = this;
    let realName = this.data.realName.trim();
    let idCard = this.data.idCard.trim();

    if (!realName) {
      util.showErrorToast('请输入真实姓名');
      return;
    }

    if (!idCard) {
      util.showErrorToast('请输入身份证号');
      return;
    }

    if (idCard.length !== 18) {
      util.showErrorToast('身份证号应为18位');
      return;
    }

    this.setData({ loading: true });

    util.request(api.UserIdCard, {
      realName: realName,
      idCard: idCard
    }, 'POST').then(function(res) {
      that.setData({ loading: false });
      if (res.errno === 0) {
        wx.showToast({
          title: '认证成功',
          icon: 'success'
        });
        that.setData({
          realName: res.data.realName,
          idCard: res.data.idCard,
          bound: true
        });
      } else {
        util.showErrorToast(res.errmsg || '认证失败');
      }
    }).catch(function() {
      that.setData({ loading: false });
      util.showErrorToast('网络异常');
    });
  }
});
