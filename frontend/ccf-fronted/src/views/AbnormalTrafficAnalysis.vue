<template>
  <el-card class="traffic-analysis-card">
    <template #header>
      <div class="card-header">
        <span>异常流量分析</span>
      </div>
    </template>

    <!-- 数据表格 -->
    <el-table
        :data="tableData"
        border
        style="width: 100%"
        max-height="500px"
    >
      <!-- 新增的四个字段 -->
      <el-table-column prop="id" label="ID" width="120" />
      <el-table-column prop="timestamp" label="时间戳" width="220">
        <template #default="scope">
          {{ new Date(scope.row.timestamp).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column prop="result" label="结果" />
      <el-table-column prop="FlowDuration" label="流量持续时间" width="150" />
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
        class="mt-4"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        v-model:page-size="pageSize"
        v-model:current-page="currentPage"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import apiClient from "@/api/axiosInstance.js";

// 接口返回数据
const tableData = ref([])
const total = ref(0)

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)

// 数据加载方法
const loadData = async () => {
  try {
    const response = await apiClient.get('/traffic/list', {
      params: {
        page: currentPage.value, // 接口要求从 0 开始，因此减 1
        size: pageSize.value
      },
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}` // 根据实际token存储方式修改
      }
    })

    // 处理响应数据
    tableData.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('数据加载失败:', error)
  }
}

// 分页事件处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadData()
}

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.traffic-analysis-card {
  margin: 20px;
  padding: 20px;
}

.card-header {
  font-size: 20px;
  text-align: center;
}

.el-table {
  margin-top: 20px;
}

.mt-4 {
  margin-top: 20px;
}
</style>
