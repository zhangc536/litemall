<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button type="primary" icon="el-icon-plus" class="filter-item" @click="handleAdd">添加等级</el-button>
    </div>

    <el-table v-loading="listLoading" :data="levelList" border fit highlight-current-row>
      <el-table-column align="center" label="等级ID" prop="id" width="80" />
      <el-table-column align="center" label="等级名称" prop="levelName" width="150" />
      <el-table-column align="center" label="所需经验值" prop="minExperience" width="120" />
      <el-table-column align="center" label="描述" prop="description" />
      <el-table-column align="center" label="排序" prop="sortOrder" width="80" />
      <el-table-column align="center" label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form ref="levelForm" :model="levelForm" :rules="rules" label-width="100px">
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="levelForm.levelName" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="所需经验值" prop="minExperience">
          <el-input-number v-model="levelForm.minExperience" :min="0" :max="999999" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="等级描述">
          <el-input v-model="levelForm.description" type="textarea" :rows="2" placeholder="请输入等级描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="levelForm.sortOrder" :min="0" :max="99" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLevels, createLevel, updateLevel, deleteLevel } from '@/api/userLevel'

export default {
  name: 'UserLevel',
  data() {
    return {
      levelList: [],
      listLoading: false,
      dialogVisible: false,
      dialogTitle: '',
      levelForm: {
        id: null,
        levelName: '',
        minExperience: 0,
        description: '',
        sortOrder: 0
      },
      rules: {
        levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
        minExperience: [{ required: true, message: '请输入所需经验值', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadLevels()
  },
  methods: {
    loadLevels() {
      this.listLoading = true
      listLevels().then(res => {
        this.levelList = res.data.data.list || []
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    handleAdd() {
      this.dialogTitle = '添加等级'
      this.levelForm = {
        id: null,
        levelName: '',
        minExperience: 0,
        description: '',
        sortOrder: 0
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑等级'
      this.levelForm = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm(`确定要删除等级"${row.levelName}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteLevel(row.id).then(() => {
          this.$notify.success({ title: '成功', message: '删除成功' })
          this.loadLevels()
        })
      }).catch(() => {})
    },
    submitForm() {
      this.$refs.levelForm.validate(valid => {
        if (valid) {
          if (this.levelForm.id) {
            updateLevel(this.levelForm).then(() => {
              this.$notify.success({ title: '成功', message: '更新成功' })
              this.dialogVisible = false
              this.loadLevels()
            })
          } else {
            createLevel(this.levelForm).then(() => {
              this.$notify.success({ title: '成功', message: '添加成功' })
              this.dialogVisible = false
              this.loadLevels()
            })
          }
        }
      })
    }
  }
}
</script>
