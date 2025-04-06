<template>
  <div class="ai-log-container">
    <!-- 搜索表单 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="主机">
        <el-input v-model="searchForm.host" placeholder="输入主机名称" />
      </el-form-item>
      <el-form-item label="是否检测到攻击">
        <el-select v-model="searchForm.isDetectedAttack" placeholder="请选择">
          <el-option label="是" :value="true" />
          <el-option label="否" :value="false" />
          <el-option label="全部" :value="null" /> <!-- 使用 null 替代 undefined -->
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 日志表格 -->
    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <template v-if="tableData.length === 0">
        <el-empty description="暂无数据"></el-empty>
      </template>
      <el-table-column prop="host" label="主机名称" width="150" />
      <el-table-column prop="logMessage" label="解析后的日志消息" />
      <el-table-column prop="avg_mse" label="平均均方误差" width="120" />
      <el-table-column prop="attack_detected" label="是否检测到攻击" width="120">
        <template #default="scope">
          {{ scope.row.isDetectedAttack ? '是' : '否' }}
        </template>
      </el-table-column>
      <el-table-column prop="log_text" label="原始日志文本" />
      <el-table-column prop="timestamp" label="时间戳" width="180" />
    </el-table>

    <!-- 分页 -->
    <el-pagination
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import apiClient from "@/api/axiosInstance.js";

const tableData = ref([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchForm = ref({
  host: '',
  isDetectedAttack: null, // 使用 null 替代 undefined
});
const loading = ref(false);

// 数据请求方法
const fetchData = async () => {
  loading.value = true;
  try {
    // 构造请求参数，过滤无效值
    const params = {
      page: page.value,
      size: pageSize.value,
      ...searchForm.value,
    };
    const filteredParams = Object.fromEntries(
        Object.entries(params).filter(([, v]) =>
            v !== '' // 保留 null 和其他合法值
        )
    );

    // 设置请求头
    const headers = {
      Authorization: `Bearer ${localStorage.getItem('token')}`,
    };

    // 发送请求
    const response = await apiClient.get('/deepStudyLog/list', {
      params: filteredParams,
      headers,
    });

    // 更新数据
    tableData.value = response.data.list || [];
    total.value = response.data.total || 0;

    // 检查数据完整性
    if (tableData.value.length === 0) {
      console.warn('接口返回数据为空');
    }
  } catch (error) {
    console.error('获取日志列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 初始化加载数据
onMounted(() => {
  fetchData();
});

// 分页事件处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  fetchData();
};

const handleCurrentChange = (current) => {
  page.value = current;
  fetchData();
};

// 搜索逻辑
const handleSearch = () => {
  page.value = 1; // 搜索时重置到第一页
  fetchData();
};

// 重置搜索条件
const resetSearch = () => {
  searchForm.value = {
    host: '',
    isDetectedAttack: null, // 使用 null
  };
  page.value = 1;
  fetchData();
};
</script>

<style scoped>
.ai-log-container {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
