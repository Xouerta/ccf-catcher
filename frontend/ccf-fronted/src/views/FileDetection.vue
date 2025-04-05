<template>
  <el-card class="file-detection-card">
    <template #header>
      <div class="card-header">
        <span>文件检测</span>
      </div>
    </template>

    <!-- 文件上传区域 -->
    <el-upload
        class="upload-demo"
        drag
        :http-request="handleUpload"
        :before-upload="beforeUpload"
        :show-file-list="false"
    >
      <el-icon class="upload-icon"><upload-filled /></el-icon>
      <div class="upload-text">拖拽文件至此或点击选择</div>
    </el-upload>

    <!-- 上传进度 -->
    <div v-if="uploadProgress.show" class="progress-container">
      <el-progress
          :percentage="uploadProgress.percent"
          :status="uploadProgress.status"
      />
      <div class="progress-text">{{ uploadProgress.message }}</div>
    </div>

    <!-- 分析结果 -->
    <div v-if="analysisResult" class="result-card">
      <el-card class="result-card-content">
        <div class="result-header">检测结果</div>
        <div class="result-item">
          <span class="label">文件名：</span>
          <span class="value">{{ analysisResult.originalName }}</span>
        </div>
        <div class="result-item">
          <span class="label">检测状态：</span>
          <span
              class="value"
              :style="{
              color: analysisResult.malicious ? 'red' : 'green'
            }"
          >
            {{ analysisResult.malicious ? '恶意文件' : '安全文件' }}
          </span>
        </div>
        <div class="result-item">
          <span class="label">详细信息：</span>
          <span class="value">{{ analysisResult.details || '暂无详细信息' }}</span>
        </div>
      </el-card>
    </div>

    <!-- 文件列表 -->
    <el-table :data="files" class="file-list-table" v-if="files.length">
      <el-table-column prop="originalName" label="文件名" width="300"/>
      <el-table-column prop="uploadTime" label="上传时间" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <span :style="{ color: scope.row.malicious ? 'red' : 'green' }">
            {{ scope.row.malicious ? '恶意文件' : '安全文件' }}
          </span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页控件 -->
    <el-pagination
        class="pagination"
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { UploadFilled } from '@element-plus/icons-vue';
import { ElMessage, ElTable, ElPagination } from 'element-plus';
import apiClient from '@/api/axiosInstance.js';

const uploadProgress = ref({
  show: false,
  percent: 0,
  status: 'success',
  message: ''
});
const analysisResult = ref(null);

// 文件列表相关状态
const files = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 文件列表获取方法
const fetchFiles = async () => {
  try {
    const res = await apiClient.get('/files/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      },
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    });

    if (res.data.success) {
      files.value = res.data.list;
      total.value = res.data.total;
    } else {
      ElMessage.error(res.data.message || '获取文件列表失败');
    }
  } catch (error) {
    ElMessage.error(`接口调用失败：${error.message}`);
  }
};

// 分页变化处理
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchFiles();
};

// 文件上传处理
// 文件上传处理
const handleUpload = async ({ file }) => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('未找到有效 Token，请重新登录');
    }

    const formData = new FormData();
    formData.append('file', file);
    const queryParams = `?file=${encodeURIComponent(file.name)}`;

    uploadProgress.value.show = true;
    uploadProgress.value.message = '正在上传文件...';

    const response = await apiClient.post(
        `/files/upload${queryParams}`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'multipart/form-data'
          },
          onUploadProgress: (progressEvent) => {
            const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100);
            uploadProgress.value.percent = percent;
            uploadProgress.value.message = `上传进度：${percent}%`;
          }
        }
    );

    // 新增：检查 HTTP 状态码
    if (response.status !== 200) {
      throw new Error(`HTTP 错误: ${response.status}`);
    }

    // 新增：检查业务逻辑 success 字段
    if (!response.data.success) {
      throw new Error(response.data.message || '文件上传失败');
    }

    const fileId = response.data.data.id;
    const res = await apiClient.get(`/files/${fileId}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    if (!res.data.success) {
      throw new Error(res.data.message || '检测结果获取失败');
    }

    analysisResult.value = {
      ...res.data.data,
      details: res.data.data.details || '检测完成'
    };
    uploadProgress.value.status = 'success';
    uploadProgress.value.message = '上传完成';
  } catch (error) {
    ElMessage.error(`上传失败：${error.message}`);
    uploadProgress.value.status = 'exception';
    uploadProgress.value.message = error.message;
  } finally {
    uploadProgress.value.show = true;
  }
};

// 上传前验证
const beforeUpload = (file) => {
  const isLt2G = file.size / 1024 / 1024 / 1024 < 2;
  if (!isLt2G) {
    ElMessage.warning('文件大小不能超过 2GB!');
    return false;
  }
  return true;
};

// 页面加载时初始化数据
onMounted(() => {
  fetchFiles();
});
</script>

<style scoped>
.file-detection-card {
  margin: 20px;
  padding: 20px;
}

.upload-demo {
  border: 2px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.upload-icon {
  font-size: 48px;
  color: #8c939d;
  margin: 20px 0;
}

.upload-text {
  color: #606266;
  font-size: 14px;
  text-align: center;
}

.progress-container {
  margin: 20px 0;
}

.result-card {
  margin-top: 30px;
}

.result-card-content {
  padding: 20px;
}

.result-header {
  font-size: 18px;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.result-item {
  margin: 10px 0;
}

.label {
  font-weight: bold;
  width: 120px;
  display: inline-block;
}

.value {
  vertical-align: top;
}

.file-list-table {
  margin: 20px 0;
}

.pagination {
  text-align: right;
  margin-top: 20px;
}
</style>
