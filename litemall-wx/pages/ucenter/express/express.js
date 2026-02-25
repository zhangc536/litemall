var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');

Page({
  data: {
    orderId: 0,
    expName: '',
    expNo: '',
    traces: [],
    loading: false,
    stateText: '物流状态',
    latestTrace: null,
    success: true,
    reason: ''
  },
  onLoad: function(options) {
    this.setData({
      orderId: Number(options.orderId || 0),
      expName: options.expName ? decodeURIComponent(options.expName) : '',
      expNo: options.expNo ? decodeURIComponent(options.expNo) : ''
    });
    this.loadExpress();
  },
  loadExpress: function() {
    let that = this;
    if (!that.data.orderId) {
      util.showErrorToast('参数错误');
      return;
    }
    that.setData({
      loading: true
    });
    util.request(api.ExpressQuery, {
      orderId: that.data.orderId
    }, 'POST').then(function(res) {
      if (res.errno === 0) {
        let data = res.data || {};
        let traces = [];
        let stateText = '物流状态';
        let success = data.Success !== false;
        let reason = data.Reason || '';

        if (data.State) {
          stateText = that.getStateText(data.State);
        }
        if (data.StateEx) {
          stateText = data.StateEx;
        }
        if (data.Traces && data.Traces.length > 0) {
          traces = data.Traces.slice().reverse();
        }

        let latestTrace = traces.length ? traces[0] : null;

        that.setData({
          traces: traces,
          stateText: stateText,
          latestTrace: latestTrace,
          success: success,
          reason: reason
        });

        if (!success && reason) {
          wx.showToast({
            title: reason,
            icon: 'none',
            duration: 3000
          });
        } else if (!traces.length) {
          wx.showToast({
            title: '暂无物流轨迹',
            icon: 'none'
          });
        }
      } else {
        util.showErrorToast(res.errmsg || '查询失败');
        that.setData({
          success: false,
          reason: res.errmsg || '查询失败'
        });
      }
      that.setData({
        loading: false
      });
    }).catch(function(err) {
      that.setData({
        loading: false,
        success: false,
        reason: '网络错误，请稍后重试'
      });
      util.showErrorToast('网络错误，请稍后重试');
    });
  },
  getStateText: function(state) {
    let map = {
      '0': '暂无轨迹',
      '1': '已揽收',
      '2': '在途中',
      '3': '已签收',
      '4': '问题件',
      '5': '转寄',
      '6': '清关',
      '7': '代签收'
    };
    return map[state] || '物流状态';
  },
  copyExpNo: function() {
    let expNo = this.data.expNo;
    if (expNo) {
      wx.setClipboardData({
        data: expNo,
        success: function() {
          wx.showToast({
            title: '已复制单号',
            icon: 'success'
          });
        }
      });
    }
  },
  refreshExpress: function() {
    this.loadExpress();
  }
});
