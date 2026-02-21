<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.orderSn" clearable class="filter-item" style="width: 200px;" placeholder="订单编号" />
      <el-select v-model="listQuery.voucherStatus" clearable class="filter-item" style="width: 150px;" placeholder="凭证状态">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button type="primary" icon="el-icon-search" class="filter-item" @click="handleFilter">查询</el-button>
    </div>

    <el-table v-loading="listLoading" :data="voucherList" border fit highlight-current-row>
      <el-table-column align="center" label="订单ID" prop="id" width="80" />
      <el-table-column align="center" label="订单编号" prop="orderSn" width="180" />
      <el-table-column align="center" label="用户ID" prop="userId" width="80" />
      <el-table-column align="center" label="订单金额" prop="actualPrice" width="100">
        <template slot-scope="scope">
          <span style="color: #e64340;">￥{{ scope.row.actualPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="支付凭证" width="120">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.payVoucher"
            :src="scope.row.payVoucher"
            :preview-src-list="[scope.row.payVoucher]"
            style="width: 80px; height: 80px; cursor: pointer;"
            fit="cover"
          />
          <span v-else style="color: #999;">未上传</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="凭证状态" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.voucherStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.voucherStatus === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.voucherStatus === 2" type="danger">已拒绝</el-tag>
          <el-tag v-else type="info">无凭证</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="下单时间" prop="addTime" width="160" />
      <el-table-column align="center" label="操作" width="220" fixed="right">
        <template slot-scope="scope">
          <div v-if="scope.row.payVoucher && scope.row.voucherStatus === 0">
            <el-button type="success" size="mini" @click="handleAudit(scope.row, 'APPROVED')">通过</el-button>
            <el-button type="danger" size="mini" @click="handleAudit(scope.row, 'REJECTED')">驳回</el-button>
          </div>
          <div v-else-if="scope.row.voucherStatus === 1 && scope.row.orderStatus === 201">
            <el-button type="primary" size="mini" @click="handleShip(scope.row)">发货</el-button>
          </div>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="loadVouchers" />

    <el-dialog title="发货" :visible.sync="shipDialogVisible" width="400px">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="物流公司">
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择物流公司" style="width: 100%;">
            <el-option label="顺丰速运" value="SF" />
            <el-option label="中通快递" value="ZTO" />
            <el-option label="圆通速递" value="YTO" />
            <el-option label="韵达快递" value="YD" />
            <el-option label="申通快递" value="STO" />
            <el-option label="邮政EMS" value="EMS" />
            <el-option label="京东物流" value="JD" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip">确定发货</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listVouchers, auditOrder, shipOrder } from '@/api/voucher'
import Pagination from '@/components/Pagination'

export default {
  name: 'VoucherAudit',
  components: { Pagination },
  data() {
    return {
      voucherList: [],
      total: 0,
      listLoading: false,
      listQuery: {
        page: 1,
        limit: 20,
        orderSn: '',
        voucherStatus: 0
      },
      shipDialogVisible: false,
      shipForm: {
        orderId: null,
        logisticsCompany: '',
        trackingNo: ''
      }
    }
  },
  created() {
    this.loadVouchers()
  },
  methods: {
    loadVouchers() {
      this.listLoading = true
      listVouchers(this.listQuery).then(res => {
        this.voucherList = res.data.data.list || []
        this.total = res.data.data.total || 0
        this.listLoading = false
      }).catch(() => {
        this.voucherList = []
        this.total = 0
        this.listLoading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.loadVouchers()
    },
    handleAudit(row, status) {
      const statusText = status === 'APPROVED' ? '通过' : '驳回'
      this.$confirm(`确定要${statusText}订单 ${row.orderSn} 的支付凭证吗？`, '审核确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        auditOrder(row.id, status, '').then(() => {
          if (status === 'APPROVED') {
            this.$confirm('审核通过！是否立即发货？', '提示', {
              confirmButtonText: '立即发货',
              cancelButtonText: '稍后发货',
              type: 'success'
            }).then(() => {
              this.shipForm = {
                orderId: row.id,
                logisticsCompany: '',
                trackingNo: ''
              }
              this.shipDialogVisible = true
            }).catch(() => {})
          } else {
            this.$notify.success({
              title: '成功',
              message: '已驳回该凭证'
            })
          }
          this.loadVouchers()
        }).catch(() => {
          this.$alert('系统内部错误，请联系管理员维护', '错误', { type: 'error' })
        })
      }).catch(() => {})
    },
    handleShip(row) {
      this.shipForm = {
        orderId: row.id,
        logisticsCompany: '',
        trackingNo: ''
      }
      this.shipDialogVisible = true
    },
    confirmShip() {
      if (!this.shipForm.logisticsCompany) {
        this.$message.error('请选择物流公司')
        return
      }
      if (!this.shipForm.trackingNo) {
        this.$message.error('请输入物流单号')
        return
      }
      shipOrder(this.shipForm.orderId, this.shipForm.logisticsCompany, this.shipForm.trackingNo).then(() => {
        this.$notify.success({
          title: '成功',
          message: '发货成功'
        })
        this.shipDialogVisible = false
        this.loadVouchers()
      }).catch(() => {
        this.$alert('系统内部错误，请联系管理员维护', '错误', { type: 'error' })
      })
    }
  }
}
</script>
