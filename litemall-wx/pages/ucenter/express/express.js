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
    latestTrace: null
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
        let traces = [];
        let stateText = '物流状态';
        if (res.data && res.data.State) {
          stateText = that.getStateText(res.data.State);
        }
        if (res.data && res.data.StateEx) {
          stateText = res.data.StateEx;
        }
        if (res.data && res.data.Traces) {
          traces = res.data.Traces.slice().reverse();
        }
        let latestTrace = traces.length ? traces[0] : null;
        that.setData({
          traces: traces,
          stateText: stateText,
          latestTrace: latestTrace
        });
        if (!traces.length) {
          wx.showToast({
            title: '暂无物流信息',
            icon: 'none'
          });
        }
      } else {
        util.showErrorToast(res.errmsg);
      }
      that.setData({
        loading: false
      });
    }).catch(function() {
      that.setData({
        loading: false
      });
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
    if (map[state]) {
      return map[state];
    }
    return '物流状态';
  }
});
