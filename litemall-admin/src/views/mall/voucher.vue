<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.orderSn" clearable class="filter-item" style="width: 200px;" placeholder="订单编号" />
      <el-select v-model="listQuery.voucherStatus" clearable class="filter-item" style="width: 150px;" placeholder="凭证状态">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
    </div>

    <el-table v-loading="listLoading" :data="list" border fit highlight-current-row>
      <el-table-column align="center" label="订单ID" prop="id" width="80" />
      <el-table-column align="center" label="订单编号" prop="orderSn" width="180" />
      <el-table-column align="center" label="用户ID" prop="userId" width="80" />
      <el-table-column align="center" label="订单金额" prop="actualPrice" width="100">
        <template slot-scope="scope">
          <span>￥{{ scope.row.actualPrice }}</span>
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
      <el-table-column align="center" label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <div v-if="scope.row.payVoucher && scope.row.voucherStatus === 0">
            <el-button size="mini" type="success" @click="handleAudit(scope.row, 1)">通过</el-button>
            <el-button size="mini" type="danger" @click="handleAudit(scope.row, 2)">拒绝</el-button>
          </div>
          <div v-else-if="scope.row.voucherStatus === 1 && scope.row.orderStatus === 201">
            <el-button size="mini" type="primary" @click="handleShip(scope.row)">发货</el-button>
          </div>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />

    <el-dialog title="发货" :visible.sync="shipDialogVisible" width="400px">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="物流公司">
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择物流公司">
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
        <el-button type="primary" @click="confirmShip">确定</el-button>
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
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        page: 1,
        limit: 20,
        orderSn: '',
        voucherStatus: undefined
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
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      listVouchers(this.listQuery).then(response => {
        this.list = response.data.data.list || []
        this.total = response.data.data.total || 0
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleAudit(row, status) {
      const statusText = status === 1 ? '通过' : '拒绝'
      this.$confirm(`确定要${statusText}该订单的支付凭证吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        auditOrder(row.id, status === 1 ? 'APPROVED' : 'REJECTED').then(() => {
          this.$message.success('审核成功')
          this.getList()
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
      if (!this.shipForm.logisticsCompany || !this.shipForm.trackingNo) {
        this.$message.error('请填写完整发货信息')
        return
      }
      shipOrder(this.shipForm.orderId, this.shipForm.logisticsCompany, this.shipForm.trackingNo).then(() => {
        this.$message.success('发货成功')
        this.shipDialogVisible = false
        this.getList()
      })
    }
  }
}
</script>
