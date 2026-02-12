import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import store from '@/store'
import { getToken } from '@/utils/auth'

axios.defaults.withCredentials = false

const resolvedBaseURL = process.env.VUE_APP_BASE_API || 'https://www.zhangcde.asia/admin'

const service = axios.create({
  baseURL: resolvedBaseURL,
  timeout: 5000
})

// request interceptor
service.interceptors.request.use(
  config => {
    // Do something before request is sent
    if (store.getters.token) {
      // 让每个请求携带token-- ['X-Litemall-Admin-Token']为自定义key 请根据实际情况自行修改
      config.headers['X-Litemall-Admin-Token'] = getToken()
    }
    return config
  },
  error => Promise.reject(error)
)

function buildError(errmsg) {
  const data = { errmsg }
  return { data }
}

// response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data

    if (res.errno === 501) {
      MessageBox.alert('系统未登录，请重新登录', '错误', {
        confirmButtonText: '确定',
        type: 'error'
      }).then(() => {
        store.dispatch('FedLogOut').then(() => {
          location.reload()
        })
      })
      return Promise.reject(buildError('系统未登录，请重新登录'))
    } else if (res.errno === 502) {
      MessageBox.alert('系统内部错误，请联系管理员维护', '错误', {
        confirmButtonText: '确定',
        type: 'error'
      })
      return Promise.reject(buildError('系统内部错误，请联系管理员维护'))
    } else if (res.errno === 503) {
      MessageBox.alert('请求业务目前未支持', '警告', {
        confirmButtonText: '确定',
        type: 'error'
      })
      return Promise.reject(buildError('请求业务目前未支持'))
    } else if (res.errno === 504) {
      MessageBox.alert('更新数据已经失效，请刷新页面重新操作', '警告', {
        confirmButtonText: '确定',
        type: 'error'
      })
      return Promise.reject(buildError('更新数据已经失效，请刷新页面重新操作'))
    } else if (res.errno === 505) {
      MessageBox.alert('更新失败，请再尝试一次', '警告', {
        confirmButtonText: '确定',
        type: 'error'
      })
      return Promise.reject(buildError('更新失败，请再尝试一次'))
    } else if (res.errno === 506) {
      MessageBox.alert('没有操作权限，请联系管理员授权', '错误', {
        confirmButtonText: '确定',
        type: 'error'
      })
      return Promise.reject(buildError('没有操作权限，请联系管理员授权'))
    } else if (res.errno !== 0) {
      // 非5xx的错误属于业务错误，留给具体页面处理
      return Promise.reject(response)
    } else {
      return response
    }
  }, error => {
    const status = error && error.response ? error.response.status : null
    if (status === 401) {
      MessageBox.alert('系统未登录，请重新登录', '错误', {
        confirmButtonText: '确定',
        type: 'error'
      }).then(() => {
        store.dispatch('FedLogOut').then(() => {
          location.reload()
        })
      })
      return Promise.reject(buildError('系统未登录，请重新登录'))
    }
    Message({
      message: '登录连接超时（后台不能连接，请联系系统管理员）',
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(buildError('登录连接超时（后台不能连接，请联系系统管理员）'))
  })

export default service
