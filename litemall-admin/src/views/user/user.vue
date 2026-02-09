<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.username" clearable class="filter-item" style="width: 200px;" :placeholder="$t('user_user.placeholder.filter_username')" />
      <el-input v-model="listQuery.mobile" clearable class="filter-item" style="width: 200px;" :placeholder="$t('user_user.placeholder.filter_mobile')" />
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">{{ $t('app.button.search') }}</el-button>
      <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="handleDownload">{{ $t('app.button.download') }}</el-button>
    </div>

    <el-table v-loading="listLoading" :data="list" :element-loading-text="$t('app.message.list_loading')" border fit highlight-current-row>
      <el-table-column align="center" width="100px" :label="$t('user_user.table.id')" prop="id" sortable />

      <el-table-column align="center" min-width="140px" :label="$t('user_user.table.nickname')" prop="nickname" />

      <el-table-column align="center" min-width="120px" :label="$t('user_user.table.avatar')" prop="avatar">
        <template slot-scope="scope">
          <img v-if="scope.row.avatar" :src="scope.row.avatar" width="40">
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="120px" :label="$t('user_user.form.username')" prop="username" />

      <el-table-column align="center" min-width="140px" :label="$t('user_user.table.mobile')" prop="mobile" />

      <el-table-column align="center" min-width="140px" :label="$t('user_user.table.inviter')" prop="inviterName">
        <template slot-scope="scope">
          {{ scope.row.inviterName || scope.row.inviterUserId || '-' }}
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="120px" :label="$t('user_user.table.user_level')" prop="userLevel">
        <template slot-scope="scope">
          {{ formatUserLevel(scope.row.userLevel) }}
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="100px" :label="$t('user_user.table.status')" prop="status">
        <template slot-scope="scope">
          <el-tag :type="formatStatusType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" min-width="120px" :label="$t('user_user.table.actions')" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-permission="['POST /admin/user/delete']" type="danger" size="mini" @click="handleDelete(scope.row)">{{ $t('app.button.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />
  </div>
</template>

<script>
import { listUser, deleteUser } from '@/api/user'
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
    formatUserLevel(level) {
      return userLevelMap[level] || userLevelMap[0]
    },
    formatStatus(status) {
      return statusMap[status] || statusMap[0]
    },
    formatStatusType(status) {
      return statusTypeMap[status] || statusTypeMap[0]
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
    handleDownload() {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['用户ID', '用户名', '昵称', '手机号码', '推荐人', '用户等级', '状态']
        const filterVal = ['id', 'username', 'nickname', 'mobile', 'inviterName', 'userLevel', 'status']
        const list = this.list.map(item => ({
          ...item,
          inviterName: item.inviterName || item.inviterUserId || '-',
          userLevel: this.formatUserLevel(item.userLevel),
          status: this.formatStatus(item.status)
        }))
        excel.export_json_to_excel2(tHeader, list, filterVal, '用户信息')
        this.downloadLoading = false
      })
    }
  }
}
</script>
