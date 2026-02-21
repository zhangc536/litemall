<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="searchUserId" clearable class="filter-item" style="width: 200px;" placeholder="输入用户ID" />
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
      <el-button class="filter-item" type="success" icon="el-icon-refresh" @click="loadRootUsers">显示顶级用户</el-button>
    </div>

    <el-tree
      v-if="treeData.length > 0"
      :data="treeData"
      :props="defaultProps"
      node-key="id"
      :load="loadNode"
      lazy
      :expand-on-click-node="false"
      default-expand-all
    >
      <span slot-scope="{ node, data }" class="custom-tree-node">
        <span class="node-content">
          <el-avatar v-if="data.avatar" :src="resolveAvatar(data.avatar)" :size="30" style="margin-right: 8px;" />
          <el-avatar v-else icon="el-icon-user-solid" :size="30" style="margin-right: 8px;" />
          <span class="user-info">
            <span class="user-id">ID: {{ data.id }}</span>
            <span class="user-name">{{ data.nickname || data.username || '未命名' }}</span>
            <el-tag v-if="data.levelName" type="info" size="mini">{{ data.levelName }}</el-tag>
            <span class="user-mobile">{{ data.mobile || '-' }}</span>
            <span v-if="data.inviteCode" class="invite-code">邀请码: {{ data.inviteCode }}</span>
            <span v-if="data.teamCount !== undefined" class="team-count">团队: {{ data.teamCount }}人</span>
          </span>
        </span>
        <span class="node-actions">
          <el-button type="text" size="mini" @click="viewUserDetail(data)">详情</el-button>
        </span>
      </span>
    </el-tree>

    <el-empty v-else description="暂无数据" />
  </div>
</template>

<script>
import { getUserTree, getUserChildren } from '@/api/user'

export default {
  name: 'UserTree',
  data() {
    return {
      searchUserId: '',
      treeData: [],
      defaultProps: {
        label: 'nickname',
        children: 'children',
        isLeaf: 'isLeaf'
      }
    }
  },
  created() {
    this.loadRootUsers()
  },
  methods: {
    resolveAvatar(avatar) {
      if (!avatar) return avatar
      const marker = '/wx/storage/fetch/'
      const index = avatar.indexOf(marker)
      if (index === -1) return avatar
      const baseApi = process.env.VUE_APP_BASE_API || 'https://www.zhangcde.asia/admin'
      const base = baseApi.replace(/\/admin\/?$/, '')
      const normalizedBase = base.endsWith('/') ? base.slice(0, -1) : base
      const key = avatar.substring(index + marker.length)
      return normalizedBase + marker + key
    },
    loadRootUsers() {
      this.treeData = []
      getUserTree({ type: 'root' }).then(response => {
        this.treeData = response.data.data.list || []
      }).catch(() => {
        this.treeData = []
      })
    },
    handleSearch() {
      if (!this.searchUserId) {
        this.loadRootUsers()
        return
      }
      this.treeData = []
      getUserTree({ userId: this.searchUserId }).then(response => {
        this.treeData = response.data.data.list || []
      }).catch(() => {
        this.$notify.error({
          title: '失败',
          message: '未找到该用户'
        })
        this.treeData = []
      })
    },
    loadNode(node, resolve) {
      if (node.level === 0) {
        return resolve(this.treeData)
      }
      getUserChildren({ parentId: node.data.id }).then(response => {
        const children = response.data.data.list || []
        resolve(children)
      }).catch(() => {
        resolve([])
      })
    },
    viewUserDetail(user) {
      this.$alert(`
        <p><strong>用户ID:</strong> ${user.id}</p>
        <p><strong>用户名:</strong> ${user.username || '-'}</p>
        <p><strong>昵称:</strong> ${user.nickname || '-'}</p>
        <p><strong>手机号:</strong> ${user.mobile || '-'}</p>
        <p><strong>真实姓名:</strong> ${user.realName || '-'}</p>
        <p><strong>邀请码:</strong> ${user.inviteCode || '-'}</p>
        <p><strong>推荐人ID:</strong> ${user.inviterUserId || '-'}</p>
        <p><strong>积分:</strong> ${user.points || 0}</p>
        <p><strong>经验值:</strong> ${user.experience || 0}</p>
        <p><strong>等级:</strong> ${user.levelName || '-'}</p>
        <p><strong>注册时间:</strong> ${user.addTime || '-'}</p>
      `, '用户详情', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定'
      })
    }
  }
}
</script>

<style scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
.node-content {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-id {
  color: #909399;
  font-size: 12px;
}
.user-name {
  font-weight: bold;
}
.user-mobile {
  color: #606266;
  font-size: 12px;
}
.invite-code {
  color: #409EFF;
  font-size: 12px;
}
.team-count {
  color: #67C23A;
  font-size: 12px;
}
.node-actions {
  margin-left: 20px;
}
</style>
