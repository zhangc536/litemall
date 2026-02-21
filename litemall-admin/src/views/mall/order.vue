<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.orderSn" clearable class="filter-item" style="width: 200px;" placeholder="订单编号" />
      <el-select v-model="listQuery.orderStatus" clearable class="filter-item" style="width: 150px;" placeholder="订单状态">
        <el-option label="待付款" :value="101" />
        <el-option label="待发货" :value="201" />
        <el-option label="待收货" :value="301" />
        <el-option label="已完成" :value="401" />
        <el-option label="已取消" :value="102" />
        <el-option label="已退款" :value="103" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="filter-item"
        value-format="yyyy-MM-dd"
        @change="handleDateChange"
      />
      <el-button type="primary" icon="el-icon-search" class="filter-item" @click="handleFilter">查询</el-button>
    </div>

    <el-table v-loading="listLoading" :data="orderList" border fit highlight-current-row>
      <el-table-column align="center" label="订单ID" prop="id" width="80" />
      <el-table-column align="center" label="订单编号" prop="orderSn" width="180" />
      <el-table-column align="center" label="用户ID" prop="userId" width="80" />
      <el-table-column align="center" label="收货人" prop="consignee" width="100" />
      <el-table-column align="center" label="手机号" prop="mobile" width="120" />
      <el-table-column align="center" label="订单金额" width="100">
        <template slot-scope="scope">
          <span style="color: #e64340;">￥{{ scope.row.actualPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="订单状态" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.orderStatus === 101" type="warning">待付款</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 201" type="primary">待发货</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 301" type="info">待收货</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 401" type="success">已完成</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 102" type="danger">已取消</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 103" type="danger">已退款</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="支付凭证" width="100">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.payVoucher"
            :src="scope.row.payVoucher"
            :preview-src-list="[scope.row.payVoucher]"
            style="width: 60px; height: 60px; cursor: pointer;"
            fit="cover"
          />
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="凭证状态" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.voucherStatus === 0" type="warning" size="mini">待审核</el-tag>
          <el-tag v-else-if="scope.row.voucherStatus === 1" type="success" size="mini">已通过</el-tag>
          <el-tag v-else-if="scope.row.voucherStatus === 2" type="danger" size="mini">已拒绝</el-tag>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="下单时间" prop="addTime" width="160" />
      <el-table-column align="center" label="操作" width="180" fixed="right">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.orderStatus === 201" type="success" size="mini" @click="handleShip(scope.row)">发货</el-button>
          <el-button v-if="scope.row.payVoucher && scope.row.voucherStatus === 0" type="warning" size="mini" @click="handleAudit(scope.row)">审核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="loadOrders" />

    <el-dialog title="订单详情" :visible.sync="detailDialogVisible" width="700px">
      <el-descriptions v-if="currentOrder" :column="2" border>
        <el-descriptions-item label="订单编号">{{ currentOrder.orderSn }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag v-if="currentOrder.orderStatus === 101" type="warning">待付款</el-tag>
          <el-tag v-else-if="currentOrder.orderStatus === 201" type="primary">待发货</el-tag>
          <el-tag v-else-if="currentOrder.orderStatus === 301" type="info">待收货</el-tag>
          <el-tag v-else-if="currentOrder.orderStatus === 401" type="success">已完成</el-tag>
          <el-tag v-else-if="currentOrder.orderStatus === 102" type="danger">已取消</el-tag>
          <el-tag v-else-if="currentOrder.orderStatus === 103" type="danger">已退款</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder.consignee }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentOrder.mobile }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
        <el-descriptions-item label="商品金额">￥{{ currentOrder.goodsPrice }}</el-descriptions-item>
        <el-descriptions-item label="运费">￥{{ currentOrder.freightPrice }}</el-descriptions-item>
        <el-descriptions-item label="优惠券优惠">￥{{ currentOrder.couponPrice }}</el-descriptions-item>
        <el-descriptions-item label="积分抵扣">￥{{ currentOrder.integralPrice }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span style="color: #e64340; font-size: 18px;">￥{{ currentOrder.actualPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ currentOrder.addTime }}</el-descriptions-item>
        <el-descriptions-item label="用户留言" :span="2">{{ currentOrder.message || '无' }}</el-descriptions-item>
        <el-descriptions-item label="物流公司">{{ currentOrder.shipChannel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ currentOrder.shipSn || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.payVoucher" label="支付凭证" :span="2">
          <el-image :src="currentOrder.payVoucher" :preview-src-list="[currentOrder.payVoucher]" style="width: 150px; height: 150px;" fit="cover" />
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

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

    <el-dialog title="凭证审核" :visible.sync="auditDialogVisible" width="400px">
      <div v-if="currentOrder && currentOrder.payVoucher" style="text-align: center; margin-bottom: 20px;">
        <el-image :src="currentOrder.payVoucher" :preview-src-list="[currentOrder.payVoucher]" style="width: 200px; height: 200px;" fit="cover" />
      </div>
      <div style="text-align: center;">
        <el-button type="success" @click="confirmAudit('APPROVED')">通过</el-button>
        <el-button type="danger" @click="confirmAudit('REJECTED')">驳回</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOrders, shipOrder, auditOrder } from '@/api/order'
import Pagination from '@/components/Pagination'

export default {
  name: 'OrderManage',
  components: { Pagination },
  data() {
    return {
      orderList: [],
      total: 0,
      listLoading: false,
      listQuery: {
        page: 1,
        limit: 20,
        orderSn: '',
        orderStatus: undefined,
        startTime: '',
        endTime: ''
      },
      dateRange: [],
      detailDialogVisible: false,
      shipDialogVisible: false,
      auditDialogVisible: false,
      currentOrder: null,
      shipForm: {
        orderId: null,
        logisticsCompany: '',
        trackingNo: ''
      }
    }
  },
  created() {
    this.loadOrders()
  },
  methods: {
    loadOrders() {
      this.listLoading = true
      listOrders(this.listQuery).then(res => {
        this.orderList = res.data.data.list || []
        this.total = res.data.data.total || 0
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.loadOrders()
    },
    handleDateChange(val) {
      if (val) {
        this.listQuery.startTime = val[0]
        this.listQuery.endTime = val[1]
      } else {
        this.listQuery.startTime = ''
        this.listQuery.endTime = ''
      }
    },
    handleDetail(row) {
      this.currentOrder = row
      this.detailDialogVisible = true
    },
    handleShip(row) {
      this.currentOrder = row
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
        this.$notify.success({ title: '成功', message: '发货成功' })
        this.shipDialogVisible = false
        this.loadOrders()
      })
    },
    handleAudit(row) {
      this.currentOrder = row
      this.auditDialogVisible = true
    },
    confirmAudit(status) {
      const statusText = status === 'APPROVED' ? '通过' : '驳回'
      this.$confirm(`确定要${statusText}该订单的支付凭证吗？`, '审核确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        auditOrder(this.currentOrder.id, status, '').then(() => {
          this.$notify.success({ title: '成功', message: status === 'APPROVED' ? '审核通过' : '已驳回' })
          this.auditDialogVisible = false
          this.loadOrders()
        })
      }).catch(() => {})
    }
  }
}
</script>
