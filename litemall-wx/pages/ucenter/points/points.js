var util = require('../../utils/util.js');
var api = require('../../config/api.js');

Page({
  data: {
    points: 0,
    goodsList: [],
    page: 1,
    limit: 20
  },
  getPoints() {
    let that = this;
    util.request(api.UserIndex).then(function(res) {
      if (res.errno === 0) {
        that.setData({
          points: res.data.userInfo.points || 0
        });
      }
    });
  },
  getGoodsList() {
    let that = this;
    util.request(api.GoodsList, {
      page: that.data.page,
      limit: that.data.limit,
      order: 'desc',
      sort: 'add_time'
    }).then(function(res) {
      if (res.errno === 0) {
        const goodsList = res.data.list.map(item => {
          const price = parseFloat(item.retailPrice || 0);
          return Object.assign({}, item, {
            pointsRequired: Math.ceil(price)
          });
        });
        that.setData({
          goodsList: goodsList
        });
      }
    });
  },
  onShow() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
      return;
    }
    this.getPoints();
    this.getGoodsList();
  }
});
