<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.username" clearable class="filter-item" style="width: 200px;" :placeholder="$t('user_user.placeholder.filter_username')" />
      <el-input v-model="listQuery.mobile" clearable class="filter-item" style="width: 200px;" :placeholder="$t('user_user.placeholder.filter_mobile')" />
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">{{ $t('app.button.search') }}</el-button>
    </div>

    <el-table v-loading="listLoading" :data="list" :element-loading-text="$t('app.message.list_loading')" border fit highlight-current-row>
      <el-table-column align="center" width="100px" :label="$t('user_user.table.id')" prop="id" sortable />
      <el-table-column align="center" min-width="140px" :label="$t('user_user.table.nickname')" prop="nickname" />
      <el-table-column align="center" min-width="120px" :label="$t('user_user.form.username')" prop="username" />
      <el-table-column align="center" min-width="140px" :label="$t('user_user.table.mobile')" prop="mobile" />
      <el-table-column align="center" min-width="120px" :label="$t('user_points.table.points')" prop="points" />
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />
  </div>
</template>

<script>
import { listUser } from '@/api/user'
import Pagination from '@/components/Pagination'

export default {
  name: 'UserPoints',
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
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
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
    }
  }
}
</script>
