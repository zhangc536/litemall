<template>
  <el-scrollbar wrap-class="scrollbar-wrapper">
    <el-menu
      :show-timeout="200"
      :default-active="$route.path"
      :collapse="isCollapse"
      mode="vertical"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
    >
      <sidebar-item v-for="route in sidebarRoutes" :key="route.path" :item="route" :base-path="route.path" />
    </el-menu>
  </el-scrollbar>
</template>

<script>
import { mapGetters } from 'vuex'
import SidebarItem from './SidebarItem'

export default {
  components: { SidebarItem },
  computed: {
    ...mapGetters([
      'permission_routes',
      'sidebar'
    ]),
    sidebarRoutes() {
      const routes = Array.isArray(this.permission_routes) ? [...this.permission_routes] : []
      const hasPoints = routes.some(route => route && route.path === '/points')
      if (!hasPoints) {
        routes.push({
          path: '/points',
          alwaysShow: true,
          name: 'pointsManage',
          meta: {
            title: 'app.menu.user_points',
            icon: 'chart'
          },
          children: [
            {
              path: 'list',
              name: 'pointsList',
              meta: {
                title: 'app.menu.user_points',
                noCache: true
              }
            }
          ]
        })
      }
      return routes
    },
    isCollapse() {
      return !this.sidebar.opened
    }
  }
}
</script>
