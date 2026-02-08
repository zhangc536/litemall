const util = require('../../utils/util.js');
const api = require('../../config/api.js');
const user = require('../../utils/user.js');

//获取应用实例
const app = getApp();

Page({
  data: {
    goodsList: [],
    goodsCount: 0,
    page: 1,
    limit: 10,
    totalPages: 1
  },

  onShareAppMessage: function() {
    return {
      title: 'litemall小程序商场',
      desc: '开源微信小程序商城',
      path: '/pages/index/index'
    }
  },

  onPullDownRefresh() {
    wx.showNavigationBarLoading() //在标题栏中显示加载
    this.setData({
      goodsList: [],
      page: 1,
      totalPages: 1
    });
    Promise.all([this.getGoodsList(), this.getGoodsCount()]).finally(() => {
      wx.hideNavigationBarLoading() //完成停止加载
      wx.stopPullDownRefresh() //停止下拉刷新
    });
  },

  getGoodsList: function() {
    let that = this;
    return util.request(api.GoodsList, {
      page: that.data.page,
      limit: that.data.limit
    }).then(function(res) {
      if (res.errno === 0) {
        that.setData({
          goodsList: that.data.goodsList.concat(res.data.list),
          totalPages: res.data.pages
        });
      }
    });
  },
  getGoodsCount: function() {
    let that = this;
    return util.request(api.GoodsCount).then(function (res) {
      that.setData({
        goodsCount: res.data
      });
    });
  },
  onLoad: function(options) {

    // 页面初始化 options为页面跳转所带来的参数
    if (options.scene) {
      //这个scene的值存在则证明首页的开启来源于朋友圈分享的图,同时可以通过获取到的goodId的值跳转导航到对应的详情页
      var scene = decodeURIComponent(options.scene);
      console.log("scene:" + scene);

      let info_arr = [];
      info_arr = scene.split(',');
      let _type = info_arr[0];
      let id = info_arr[1];

      if (_type == 'goods') {
        wx.navigateTo({
          url: '../goods/goods?id=' + id
        });
      } else if (_type == 'groupon') {
        wx.navigateTo({
          url: '../goods/goods?grouponId=' + id
        });
      } else if (_type == 'invite') {
        if (id) {
          wx.setStorageSync('inviteCode', id);
        }
      } else {
        wx.navigateTo({
          url: '../index/index'
        });
      }
    }

    // 页面初始化 options为页面跳转所带来的参数
    if (options.grouponId) {
      //这个pageId的值存在则证明首页的开启来源于用户点击来首页,同时可以通过获取到的pageId的值跳转导航到对应的详情页
      wx.navigateTo({
        url: '../goods/goods?grouponId=' + options.grouponId
      });
    }

    // 页面初始化 options为页面跳转所带来的参数
    if (options.goodId) {
      //这个goodId的值存在则证明首页的开启来源于分享,同时可以通过获取到的goodId的值跳转导航到对应的详情页
      wx.navigateTo({
        url: '../goods/goods?id=' + options.goodId
      });
    }

    // 页面初始化 options为页面跳转所带来的参数
    if (options.orderId) {
      //这个orderId的值存在则证明首页的开启来源于订单模版通知,同时可以通过获取到的pageId的值跳转导航到对应的详情页
      wx.navigateTo({
        url: '../ucenter/orderDetail/orderDetail?id=' + options.orderId
      });
    }

    this.getGoodsList();
    this.getGoodsCount();
  },
  onReady: function() {
    // 页面渲染完成
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
  onReachBottom() {
    if (this.data.totalPages > this.data.page) {
      this.setData({
        page: this.data.page + 1
      });
      this.getGoodsList();
    } else {
      wx.showToast({
        title: '没有更多商品了',
        icon: 'none',
        duration: 2000
      });
      return false;
    }
  }
})
