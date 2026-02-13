<template>
  <div class="app-container">
    <el-form ref="dataForm" :rules="rules" :model="dataForm" status-icon label-width="300px">
      <el-form-item label="是否开启积分功能" prop="litemall_point_enable">
        <el-switch v-model="dataForm.litemall_point_enable" active-value="true" inactive-value="false" />
      </el-form-item>
      <el-form-item label="阶梯奖励配置">
        <el-table :data="tiers" border style="width: 100%; margin-bottom: 10px;">
          <el-table-column label="最低消费金额 (元)" width="200">
            <template slot-scope="scope">
              <el-input v-model="scope.row.amount" placeholder="例如：10000" type="number" />
            </template>
          </el-table-column>
          <el-table-column label="奖励比例 (%)" width="200">
            <template slot-scope="scope">
              <el-input v-model="scope.row.rate" placeholder="例如：10" type="number" />
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button size="mini" type="danger" @click="handleDeleteTier(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" size="mini" @click="handleAddTier">添加阶梯</el-button>
        <div class="info">设置阶梯奖励后，将根据阶梯规则计算积分。</div>
      </el-form-item>

      <el-form-item>
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="update">确定</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { listPoint, updatePoint } from '@/api/config'

export default {
  name: 'PointConfig',
  data() {
    return {
      dataForm: {
        litemall_point_enable: 'false',
        litemall_point_reward_tiers: '[]'
      },
      tiers: [],
      rules: {
      }
    }
  },
  created() {
    this.init()
  },
  methods: {
    init: function() {
      listPoint().then(response => {
        this.dataForm = response.data.data
        // Ensure default values if backend returns null
        if (!this.dataForm.litemall_point_enable) {
          this.$set(this.dataForm, 'litemall_point_enable', 'false')
        }
        if (this.dataForm.litemall_point_reward_tiers) {
          try {
            this.tiers = JSON.parse(this.dataForm.litemall_point_reward_tiers)
          } catch (e) {
            this.tiers = []
          }
        } else {
          this.tiers = []
        }
      })
    },
    handleAddTier() {
      this.tiers.push({ amount: '', rate: '' })
    },
    handleDeleteTier(index) {
      this.tiers.splice(index, 1)
    },
    cancel() {
      this.init()
    },
    update() {
      this.$refs['dataForm'].validate((valid) => {
        if (!valid) {
          return false
        }
        this.doUpdate()
      })
    },
    doUpdate() {
      this.dataForm.litemall_point_reward_tiers = JSON.stringify(this.tiers)
      updatePoint(this.dataForm)
        .then(response => {
          this.$notify.success({
            title: '成功',
            message: '积分配置成功'
          })
        })
        .catch(response => {
          this.$notify.error({
            title: '失败',
            message: response.data.errmsg
          })
        })
    }
  }
}
</script>

<style scoped>
.info {
  margin-left: 15px;
  color: #909399;
  font-size: 12px;
}
</style>
