import { asyncRoutes, constantRoutes } from '@/router'
import Layout from '@/views/layout/Layout'

/**
 * 通过meta.perms判断是否与当前用户权限匹配
 * @param perms
 * @param route
 */
function hasPermission(perms, route) {
  if (route.meta && route.meta.perms) {
    return perms.some(perm => route.meta.perms.includes(perm))
  } else {
    return true
  }
}

/**
 * 递归过滤异步路由表，返回符合用户角色权限的路由表
 * @param routes asyncRoutes
 * @param perms
 */
function filterAsyncRoutes(routes, perms) {
  const res = []

  routes.forEach(route => {
    const tmp = { ...route }
    if (tmp.children) {
      tmp.children = filterAsyncRoutes(tmp.children, perms)
      if (tmp.children && tmp.children.length > 0) {
        res.push(tmp)
      }
    } else {
      if (hasPermission(perms, tmp)) {
        res.push(tmp)
      }
    }
  })

  return res
}

function isSuperRole(roles) {
  if (!roles || roles.length === 0) {
    return false
  }
  return roles.includes('超级管理员') || roles.includes('超级管理')
}

function buildOrderRoute() {
  return {
    path: '/order',
    component: Layout,
    redirect: 'noredirect',
    alwaysShow: true,
    name: 'orderManage',
    meta: {
      title: '订单管理',
      icon: 'shopping'
    },
    children: [
      {
        path: 'list',
        component: () => import('@/views/mall/order'),
        name: 'orderList',
        meta: {
          title: '订单列表',
          noCache: true
        }
      }
    ]
  }
}

function ensureOrderRoute(routes) {
  const inConstant = constantRoutes.some(route => route && route.path === '/order')
  if (inConstant) {
    return routes
  }
  const hasOrder = routes.some(route => route && route.path === '/order')
  if (hasOrder) {
    return routes
  }
  const fromExisting = [...constantRoutes, ...asyncRoutes].find(route => route && route.path === '/order')
  const orderRoute = fromExisting || buildOrderRoute()
  return routes.concat(orderRoute)
}

const permission = {
  state: {
    routes: constantRoutes,
    addRoutes: []
  },
  mutations: {
    SET_ROUTES: (state, routes) => {
      state.addRoutes = routes
      state.routes = constantRoutes.concat(routes)
    }
  },
  actions: {
    GenerateRoutes({ commit }, data) {
      return new Promise(resolve => {
        const { perms, roles } = data
        let accessedRoutes
        if (isSuperRole(roles) || perms.includes('*')) {
          accessedRoutes = asyncRoutes
        } else {
          accessedRoutes = filterAsyncRoutes(asyncRoutes, perms)
        }
        accessedRoutes = ensureOrderRoute(accessedRoutes)
        commit('SET_ROUTES', accessedRoutes)
        resolve()
      })
    }
  }
}

export default permission
