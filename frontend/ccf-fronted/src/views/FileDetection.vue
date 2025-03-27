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
        multiple
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
      <div class="progress-text">
        {{ uploadProgress.message }}
      </div>
    </div>

    <!-- 分析结果 -->
    <div v-if="analysisResult" class="result-card">
      <el-card class="result-card-content">
        <div class="result-header">检测结果</div>
        <div class="result-item">
          <span class="label">文件名：</span>
          <span class="value">{{ analysisResult.fileName }}</span>
        </div>
        <div class="result-item">
          <span class="label">检测状态：</span>
          <span
              class="value"
              :style="{
              color: analysisResult.status === '恶意文件' ? 'red' : 'green'
            }"
          >
            {{ analysisResult.status }}
          </span>
        </div>
        <div class="result-item">
          <span class="label">详细信息：</span>
          <span class="value">{{ analysisResult.details }}</span>
        </div>
      </el-card>
    </div>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const chunkSize = 5 * 1024 * 1024 // 每个分片5MB
const uploadProgress = ref({
  show: false,
  percent: 0,
  status: 'success',
  message: ''
})
const analysisResult = ref(null)

// 文件上传处理
const handleUpload = async ({ file }) => {
  const fileId = Date.now().toString() // 生成唯一文件标识
  const chunks = Math.ceil(file.size / chunkSize)
  let uploadedChunks = loadUploadedChunks(fileId) || []

  uploadProgress.value = {
    show: true,
    percent: (uploadedChunks.length / chunks) * 100,
    message: `正在上传：${uploadedChunks.length}/${chunks} 块`
  }

  for (let i = 0; i < chunks; i++) {
    if (uploadedChunks.includes(i)) continue // 已上传的跳过

    const chunk = file.slice(
        i * chunkSize,
        (i + 1) * chunkSize > file.size ? file.size : (i + 1) * chunkSize
    )

    try {
      await uploadChunk(fileId, i, chunk, chunks)
      uploadedChunks.push(i)
      saveUploadedChunks(fileId, uploadedChunks)
      updateProgress(i + 1, chunks)
    } catch (error) {
      ElMessage.error('上传失败，请检查网络')
      return
    }
  }

  // 模拟分析结果
  await new Promise(resolve => setTimeout(resolve, 1500))
  analysisResult.value = {
    fileName: file.name,
    status: Math.random() > 0.5 ? '安全文件' : '恶意文件',
    details: '检测到可疑行为：...（模拟数据）'
  }
}

// 分片上传
const uploadChunk = async (fileId, chunkIndex, chunk, totalChunks) => {
  // 模拟上传请求（替换为真实接口）
  const formData = new FormData()
  formData.append('fileId', fileId)
  formData.append('chunkIndex', chunkIndex)
  formData.append('chunk', chunk)
  formData.append('totalChunks', totalChunks)

  // 这里替换为真实请求：
  // await axios.post('/api/upload-chunk', formData)
  return new Promise(resolve => setTimeout(resolve, 500))
}

// 更新进度条
const updateProgress = (currentChunk, totalChunks) => {
  uploadProgress.value.percent = (currentChunk / totalChunks) * 100
  uploadProgress.value.message = `正在上传：${currentChunk}/${totalChunks} 块`
}

// 保存已上传分片
const saveUploadedChunks = (fileId, chunks) => {
  localStorage.setItem(fileId, JSON.stringify(chunks))
}

// 加载已上传分片
const loadUploadedChunks = (fileId) => {
  const stored = localStorage.getItem(fileId)
  return stored ? JSON.parse(stored) : []
}

// 上传前验证
const beforeUpload = (file) => {
  const isLt2G = file.size / 1024 / 1024 / 1024 < 2
  if (!isLt2G) {
    ElMessage.warning('文件大小不能超过 2GB!')
    return false
  }
  return true
}
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
</style>
