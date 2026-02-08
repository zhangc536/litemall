var util = require('./utils/util.js');
var api = require('./config/api.js');
var user = require('./utils/user.js');

App({
  onLaunch: function() {
    Promise.prototype.finally = function(callback){
      let P = this.constructor;
      return this.then(
              value => {
                   P.resolve(callback()).then(() => value)
               },
               reason => {
                   P.resolve(callback()).then(() => { throw reason })
               }
           )
    }
    const opts = wx.getLaunchOptionsSync();
    let inviteCode = null;
    if (opts && opts.query && opts.query.inviteCode) {
      inviteCode = opts.query.inviteCode;
    } else if (opts && opts.scene) {
      try {
        const scene = decodeURIComponent(opts.scene);
        if (scene.indexOf(',') > -1) {
          const parts = scene.split(',');
          if (parts[0] === 'invite' && parts[1]) {
            inviteCode = parts[1];
          }
        } else if (/^\d+$/.test(scene)) {
          inviteCode = scene;
        }
      } catch(e) {}
    }
    if (inviteCode) {
      wx.setStorageSync('inviteCode', inviteCode);
    }
    const updateManager = wx.getUpdateManager();
    wx.getUpdateManager().onUpdateReady(function() {
      wx.showModal({
        title: '更新提示',
        content: '新版本已经准备好，是否重启应用？',
        success: function(res) {
          if (res.confirm) {
            // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
            updateManager.applyUpdate()
          }
        }
      })
    })
  },
  onShow: function(options) {
    user.checkLogin().then(res => {
      this.globalData.hasLogin = true;
    }).catch(() => {
      this.globalData.hasLogin = false;
    });
  },
  globalData: {
    hasLogin: false
  }
})
