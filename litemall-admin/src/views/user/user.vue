<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.username" clearable class="filter-item" style="width: 200px;" :placeholder="$t('user_user.placeholder.filter_username')" />
      <el-input v-model="listQuery.mobile" clearable class="filter-item" style="width: 200px;" :placeholder="$t('user_user.placeholder.filter_mobile')" />
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">{{ $t('app.button.search') }}</el-button>
      <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="handleDownload">{{ $t('app.button.download') }}</el-button>
      <el-button class="filter-item" type="success" icon="el-icon-share" @click="goUserTree">用户树</el-button>
    </div>

    <el-table v-loading="listLoading" :data="list" :element-loading-text="$t('app.message.list_loading')" border fit highlight-current-row>
      <el-table-column align="center" width="100px" :label="$t('user_user.table.id')" prop="id" sortable />

      <el-table-column align="center" min-width="140px" :label="$t('user_user.table.nickname')" prop="nickname" />

      <el-table-column align="center" min-width="120px" :label="$t('user_user.table.avatar')" prop="avatar">
        <template slot-scope="scope">
          <img v-if="scope.row.avatar" :src="resolveAvatar(scope.row.avatar)" width="40">
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="120px" :label="$t('user_user.form.username')" prop="username" />

      <el-table-column align="center" min-width="140px" :label="$t('user_user.table.mobile')" prop="mobile" />

      <el-table-column align="center" min-width="100px" label="真实姓名" prop="realName">
        <template slot-scope="scope">
          {{ scope.row.realName || '-' }}
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="180px" label="身份证号" prop="idCard">
        <template slot-scope="scope">
          <span v-if="scope.row.idCard">{{ maskIdCard(scope.row.idCard) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="140px" :label="$t('user_user.table.inviter')" prop="inviterName">
        <template slot-scope="scope">
          {{ scope.row.inviterName || scope.row.inviterUserId || '-' }}
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="120px" :label="$t('user_user.table.user_level')">
        <template slot-scope="scope">
          {{ formatUserLevel(scope.row) }}
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="100px" :label="$t('user_user.table.status')" prop="status">
        <template slot-scope="scope">
          <el-tag :type="formatStatusType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="160px" :label="$t('user_user.table.actions')" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-if="scope.row.idCard" v-permission="['POST /admin/user/unbindidcard']" type="warning" size="mini" @click="handleUnbindIdCard(scope.row)">解绑实名</el-button>
          <el-button v-permission="['POST /admin/user/delete']" type="danger" size="mini" @click="handleDelete(scope.row)">{{ $t('app.button.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />
  </div>
</template>

<script>
import { listUser, deleteUser, unbindIdCard } from '@/api/user'
import Pagination from '@/components/Pagination'

const userLevelMap = {
  0: '普通',
  1: 'VIP',
  2: '高级VIP'
}

const statusMap = {
  0: '可用',
  1: '禁用',
  2: '注销'
}

const statusTypeMap = {
  0: 'success',
  1: 'warning',
  2: 'info'
}

export default {
  name: 'UserList',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        username: undefined,
        mobile: undefined,
        sort: 'add_time',
        order: 'desc'
      },
      downloadLoading: false
    }
  },
  created() {
    this.getList()
  },
  methods: {
    formatUserLevel(row) {
      if (!row) return userLevelMap[0]
      if (row.levelName) return row.levelName
      return userLevelMap[row.userLevel] || userLevelMap[0]
    },
    formatStatus(status) {
      return statusMap[status] || statusMap[0]
    },
    formatStatusType(status) {
      return statusTypeMap[status] || statusTypeMap[0]
    },
    maskIdCard(idCard) {
      if (!idCard || idCard.length < 8) return idCard
      return idCard.substring(0, 4) + '**********' + idCard.substring(idCard.length - 4)
    },
    resolveAvatar(avatar) {
      if (!avatar) {
        return avatar
      }
      const marker = '/wx/storage/fetch/'
      const index = avatar.indexOf(marker)
      if (index === -1) {
        return avatar
      }
      const baseApi = process.env.VUE_APP_BASE_API || 'https://www.zhangcde.asia/admin'
      const base = baseApi.replace(/\/admin\/?$/, '')
      const normalizedBase = base.endsWith('/') ? base.slice(0, -1) : base
      const key = avatar.substring(index + marker.length)
      return normalizedBase + marker + key
    },
    getList() {
      this.listLoading = true
      listUser(this.listQuery).then(response => {
        this.list = response.data.data.list
        this.total = response.data.data.total
        this.listLoading = false
      }).catch(() => {
        this.list = []
        this.total = 0
        this.listLoading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    goUserTree() {
      this.$router.push({ path: '/user/tree' })
    },
    handleDelete(row) {
      deleteUser(row).then(() => {
        this.$notify.success({
          title: '成功',
          message: '删除用户成功'
        })
        this.getList()
      }).catch(response => {
        this.$notify.error({
          title: '失败',
          message: response.data.errmsg
        })
      })
    },
    handleUnbindIdCard(row) {
      this.$confirm('确定要解绑该用户的实名认证信息吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        unbindIdCard({ id: row.id }).then(() => {
          this.$notify.success({
            title: '成功',
            message: '解绑实名认证成功'
          })
          this.getList()
        }).catch(response => {
          this.$notify.error({
            title: '失败',
            message: response.data.errmsg
          })
        })
      }).catch(() => {})
    },
    handleDownload() {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['用户ID', '用户名', '昵称', '手机号码', '真实姓名', '身份证号', '推荐人', '用户等级', '状态']
        const filterVal = ['id', 'username', 'nickname', 'mobile', 'realName', 'idCard', 'inviterName', 'userLevelName', 'status']
        const list = this.list.map(item => ({
          ...item,
          realName: item.realName || '-',
          idCard: item.idCard ? this.maskIdCard(item.idCard) : '-',
          inviterName: item.inviterName || item.inviterUserId || '-',
          userLevelName: this.formatUserLevel(item),
          status: this.formatStatus(item.status)
        }))
        excel.export_json_to_excel2(tHeader, list, filterVal, '用户信息')
        this.downloadLoading = false
      })
    }
  }
}
</script>
