<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="orderId" clearable class="filter-item" style="width: 240px;" placeholder="请输入订单ID" />
      <el-button type="primary" icon="el-icon-search" class="filter-item" @click="loadVouchers">查询</el-button>
    </div>

    <el-table v-loading="listLoading" :data="voucherList" :element-loading-text="$t('app.message.list_loading')" border fit highlight-current-row>
      <el-table-column align="center" label="文件名" prop="name" />
      <el-table-column align="center" label="大小" prop="size" />
      <el-table-column align="center" label="类型" prop="type" />
      <el-table-column align="center" label="预览">
        <template slot-scope="scope">
          <el-image :src="scope.row.url" :preview-src-list="[scope.row.url]" style="width: 60px; height: 60px" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="上传时间" prop="addTime" />
      <el-table-column align="center" label="审核" width="220" class-name="small-padding fixed-width">
        <template>
          <el-button type="success" size="mini" @click="handleAudit('APPROVED')">通过</el-button>
          <el-button type="danger" size="mini" @click="handleAudit('REJECTED')">驳回</el-button>
          <el-button type="primary" size="mini" @click="handleShip">发货</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { listVouchers } from '@/api/voucher'
import { auditOrder, shipOrder } from '@/api/voucher'
export default {
  name: 'VoucherUpload',
  data() {
    return {
      orderId: '',
      voucherList: [],
      listLoading: false,
      uploading: false
    }
  },
  methods: {
    loadVouchers() {
      if (!this.orderId) {
        this.$message.warning('请先输入订单ID')
        return
      }
      this.listLoading = true
      listVouchers(this.orderId).then(res => {
        this.voucherList = res.data.data.list || []
        this.listLoading = false
      }).catch(() => {
        this.voucherList = []
        this.listLoading = false
      })
    },
    handleAudit(status) {
      if (!this.orderId) {
        this.$message.warning('请先输入订单ID')
        return
      }
      this.$prompt('请输入审核备注（可选）', '审核确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '备注（可留空）'
      }).then(({ value }) => {
        auditOrder(this.orderId, status, value || '').then(() => {
          this.$notify.success({ title: '成功', message: status === 'APPROVED' ? '审核通过' : '已驳回' })
          this.loadVouchers()
        }).catch(() => {
          this.$alert('系统内部错误，请联系管理员维护', '错误', { type: 'error' })
        })
      }).catch(() => {})
    },
    handleShip() {
      if (!this.orderId) {
        this.$message.warning('请先输入订单ID')
        return
      }
      this.$prompt('请输入物流单号', '发货', {
        confirmButtonText: '下一步',
        cancelButtonText: '取消',
        inputPlaceholder: '物流单号'
      }).then(({ value: trackingNo }) => {
        this.$prompt('请输入物流公司', '发货', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '物流公司'
        }).then(({ value: logisticsCompany }) => {
          shipOrder(this.orderId, logisticsCompany, trackingNo).then(() => {
            this.$notify.success({ title: '成功', message: '已发货' })
          }).catch(() => {
            this.$alert('系统内部错误，请联系管理员维护', '错误', { type: 'error' })
          })
        }).catch(() => {})
      }).catch(() => {})
    }
  }
}
</script>
