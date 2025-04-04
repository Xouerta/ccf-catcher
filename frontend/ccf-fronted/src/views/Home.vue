<template>
  <div class="carousel-container">
    <el-carousel :interval="5000" height="400px" indicator-position="outside">
      <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
        <router-link :to="{ name: item.route }">
          <img :src="item.image" class="carousel-image" alt="轮播图"/>
          <div class="carousel-title">{{ item.title }}</div>
        </router-link>
      </el-carousel-item>
    </el-carousel>
  </div>

  <!-- 实时日志播报 -->
  <div class="log-display-container">
    <el-row class="log-display">
      <el-card shadow="hover">
        <template #header>
          <span>实时日志分析播报</span>
        </template>
        <el-scrollbar style="height: 300px">
          <div
              v-for="(log, index) in logs"
              :key="index"
              class="log-item"
              :class="{ 'error-log': log.level === 'error' }"
          >
            {{ log.message }}
          </div>
        </el-scrollbar>
      </el-card>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 轮播图配置（需替换为实际图片路径）
const carouselItems = ref([
  {
    title: '异常流量分析',
    image: '/src/assets/traffic.png',
    route: 'AbnormalTrafficAnalysis'
  },
  {
    title: '异常文件检测',
    image: '/src/assets/file.png',
    route: 'FileDetection'
  },
  {
    title: '修改用户密码',
    image: '/src/assets/password.png',
    route: 'PasswordModification'
  },
  {
    title: '日志检测',
    image: '/src/assets/logs.webp',
    route: 'LogMonitoring'
  }
])

// 模拟实时日志数据
const logs = ref([])
const logInterval = ref(null)

onMounted(() => {
  logInterval.value = setInterval(() => {
    const severity = Math.random() > 0.7 ? 'error' : 'info'
    logs.value.push({
      level: severity,
      message: `[${new Date().toLocaleTimeString()}] ${severity.toUpperCase()} - 模拟日志内容 ${logs.value.length + 1}`
    })
    if (logs.value.length > 100) logs.value.shift()
  }, 5000)
})

onUnmounted(() => clearInterval(logInterval.value))
</script>

<style scoped>
.carousel-container {
  width: 100%; /* 确保容器宽度为100% */
}

.el-carousel {
  width: 100%; /* 确保轮播图宽度为100% */
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-title {
  position: absolute;
  bottom: 20px;
  left: 20px;
  color: white;
  font-size: 24px;
  background-color: rgba(0,0,0,0.5);
  padding: 8px 16px;
}

.log-display-container {
  width: 100%; /* 确保日志展示容器宽度为100% */
  margin-top: 20px;
}

.log-display {
  width: 100%; /* 确保日志展示行宽度为100% */
}

.el-card {
  width: 100%; /* 确保卡片宽度为100% */
}

.log-item {
  margin: 8px 0;
  padding: 8px;
}

.error-log {
  background-color: #ffebee;
  color: #d50000;
}
</style>
