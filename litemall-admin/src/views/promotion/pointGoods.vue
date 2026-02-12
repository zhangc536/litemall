<template>
  <div class="app-container">

    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input v-model="listQuery.goodsName" clearable class="filter-item" style="width: 200px;" placeholder="请输入商品名称" />
      <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">查找</el-button>
      <el-button class="filter-item" type="primary" icon="el-icon-edit" @click="handleCreate">添加</el-button>
    </div>

    <!-- 查询结果 -->
    <el-table v-loading="listLoading" :data="list" element-loading-text="正在查询中..." border fit highlight-current-row>

      <el-table-column align="center" label="ID" prop="id" sortable />

      <el-table-column align="center" label="商品ID" prop="goodsId" />

      <el-table-column align="center" label="商品名称" prop="goodsName" />

      <el-table-column align="center" label="商品图片" prop="picUrl">
        <template slot-scope="scope">
          <img v-if="scope.row.picUrl" :src="scope.row.picUrl" width="40">
        </template>
      </el-table-column>

      <el-table-column align="center" label="所需积分" prop="points" />

      <el-table-column align="center" label="所需金额" prop="price" />

      <el-table-column align="center" label="库存" prop="amount" />

      <el-table-column align="center" label="操作" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form ref="dataForm" :rules="rules" :model="dataForm" status-icon label-position="left" label-width="100px" style="width: 400px; margin-left:50px;">
        <el-form-item label="商品ID" prop="goodsId">
          <el-input v-model="dataForm.goodsId" placeholder="请输入商品ID" @blur="fetchGoodsInfo" />
        </el-form-item>
        <el-form-item label="商品名称" prop="goodsName">
          <el-input v-model="dataForm.goodsName" placeholder="自动获取或手动输入" />
        </el-form-item>
        <el-form-item label="商品图片" prop="picUrl">
          <el-input v-model="dataForm.picUrl" placeholder="图片URL" />
          <img v-if="dataForm.picUrl" :src="dataForm.picUrl" width="40" style="margin-top: 10px;">
        </el-form-item>
        <el-form-item label="所需积分" prop="points">
          <el-input v-model="dataForm.points" />
        </el-form-item>
        <el-form-item label="所需金额" prop="price">
          <el-input v-model="dataForm.price" />
        </el-form-item>
        <el-form-item label="库存" prop="amount">
          <el-input v-model="dataForm.amount" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button v-if="dialogStatus=='create'" type="primary" @click="createData">确定</el-button>
        <el-button v-else type="primary" @click="updateData">确定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listPointGoods, createPointGoods, updatePointGoods, deletePointGoods } from '@/api/pointGoods'
import { detailGoods } from '@/api/goods'
import Pagination from '@/components/Pagination'

export default {
  name: 'PointGoods',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        goodsName: undefined,
        sort: 'add_time',
        order: 'desc'
      },
      dataForm: {
        id: undefined,
        goodsId: undefined,
        goodsName: undefined,
        picUrl: undefined,
        points: 0,
        price: 0.00,
        amount: 0
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑',
        create: '创建'
      },
      rules: {
        goodsId: [{ required: true, message: '商品ID不能为空', trigger: 'blur' }],
        points: [{ required: true, message: '所需积分不能为空', trigger: 'blur' }],
        amount: [{ required: true, message: '库存不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      listPointGoods(this.listQuery).then(response => {
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
    resetForm() {
      this.dataForm = {
        id: undefined,
        goodsId: undefined,
        goodsName: undefined,
        picUrl: undefined,
        points: 0,
        price: 0.00,
        amount: 0
      }
    },
    handleCreate() {
      this.resetForm()
      this.dialogStatus = 'create'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    fetchGoodsInfo() {
      if (!this.dataForm.goodsId) return
      detailGoods(this.dataForm.goodsId).then(response => {
        const goods = response.data.data.goods
        this.dataForm.goodsName = goods.name
        this.dataForm.picUrl = goods.picUrl
      }).catch(err => {
        console.error(err)
      })
    },
    createData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          createPointGoods(this.dataForm).then(response => {
            this.list.unshift(response.data.data)
            this.dialogFormVisible = false
            this.$notify.success({
              title: '成功',
              message: '创建成功'
            })
          }).catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            })
          })
        }
      })
    },
    handleUpdate(row) {
      this.dataForm = Object.assign({}, row)
      this.dialogStatus = 'update'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    updateData() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          updatePointGoods(this.dataForm).then(() => {
            for (const v of this.list) {
              if (v.id === this.dataForm.id) {
                const index = this.list.indexOf(v)
                this.list.splice(index, 1, this.dataForm)
                break
              }
            }
            this.dialogFormVisible = false
            this.$notify.success({
              title: '成功',
              message: '更新成功'
            })
          }).catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            })
          })
        }
      })
    },
    handleDelete(row) {
      deletePointGoods(row).then(response => {
        this.$notify.success({
          title: '成功',
          message: '删除成功'
        })
        const index = this.list.indexOf(row)
        this.list.splice(index, 1)
      }).catch(response => {
        this.$notify.error({
          title: '失败',
          message: response.data.errmsg
        })
      })
    }
  }
}
</script>
