<template>
  <el-card class="traffic-analysis-card">
    <template #header>
      <div class="card-header">
        <span>异常流量分析</span>
      </div>
    </template>

    <!-- 数据表格 -->
    <el-table
        :data="currentPageData"
        border
        style="width: 100%"
        max-height="500px"
    >
      <el-table-column prop="sourceIp" label="源IP" width="180" />
      <el-table-column prop="destinationIp" label="目的IP" width="180" />
      <el-table-column prop="attackType" label="攻击类型" />
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
import { ref, computed, onMounted } from 'vue'

// 模拟数据（替换为真实接口）
const mockData = Array.from({ length: 100 }, (_, index) => ({
  id: index + 1,
  sourceIp: `192.168.1.${Math.floor(Math.random() * 255)}`,
  destinationIp: `10.0.0.${Math.floor(Math.random() * 255)}`,
  attackType: ['DDoS', 'SQL注入', 'XSS攻击', '端口扫描'][Math.floor(Math.random() * 4)]
}))

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(mockData.length)

// 当前页数据计算属性
const currentPageData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return mockData.slice(start, end)
})

// 分页事件处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 切换每页数量时重置到第一页
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 模拟数据加载（替换为真实接口）
onMounted(() => {
  // 模拟异步加载数据
  // 实际开发中应替换为：
  // axios.get('/api/abnormal-traffic').then(res => {
  //   mockData = res.data.list
  //   total.value = res.data.total
  // })
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
