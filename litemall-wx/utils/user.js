/**
 * 用户相关服务
 */
const util = require('../utils/util.js');
const api = require('../config/api.js');


/**
 * Promise封装wx.checkSession
 */
function checkSession() {
  return new Promise(function(resolve, reject) {
    wx.checkSession({
      success: function() {
        resolve(true);
      },
      fail: function() {
        reject(false);
      }
    })
  });
}

/**
 * Promise封装wx.login
 */
function login() {
  return new Promise(function(resolve, reject) {
    wx.login({
      success: function(res) {
        if (res.code) {
          resolve(res);
        } else {
          reject(res);
        }
      },
      fail: function(err) {
        reject(err);
      }
    });
  });
}

/**
 * 调用微信登录
 */
function loginByWeixin(userInfo) {

  return new Promise(function(resolve, reject) {
    return login().then((res) => {
      //登录远程服务器
      const inviteCode = wx.getStorageSync('inviteCode');
      const payload = {
        code: res.code,
        inviteCode: inviteCode || ''
      };
      if (userInfo) {
        payload.userInfo = userInfo;
      }
      util.request(api.AuthLoginByWeixin, payload, 'POST').then(res => {
        if (res.errno === 0) {
          //存储用户信息
          wx.setStorageSync('userInfo', res.data.userInfo);
          wx.setStorageSync('token', res.data.token);
          if (inviteCode) {
            wx.removeStorageSync('inviteCode');
          }

          resolve(res);
        } else {
          reject(res);
        }
      }).catch((err) => {
        reject(err);
      });
    }).catch((err) => {
      reject(err);
    })
  });
}

/**
 * 判断用户是否登录
 */
function checkLogin() {
  return new Promise(function(resolve, reject) {
    const userInfo = wx.getStorageSync('userInfo');
    const token = wx.getStorageSync('token');
    const hasProfile = userInfo && userInfo.nickName && userInfo.avatarUrl;
    if (token && hasProfile) {
      resolve(true);
      return;
    }
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    reject(false);
  });
}

module.exports = {
  loginByWeixin,
  checkLogin,
};
