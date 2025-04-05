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

  <!-- 添加四个环形图容器 -->
  <div class="chart-container">
    <h2>关键指标概览</h2>
    <div class="chart-grid">
      <div ref="chartRef1" class="chart"></div>
      <div ref="chartRef2" class="chart"></div>
      <div ref="chartRef3" class="chart"></div>
      <div ref="chartRef4" class="chart"></div>
    </div>
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
import {ref, onMounted, onUnmounted} from 'vue';
import * as echarts from 'echarts'; // 引入 ECharts

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
    image: '/src/assets/log.png',
    route: 'LogMonitoring'
  }
]);

// 模拟实时日志数据
const logs = ref([]);
const logInterval = ref(null);

onMounted(() => {
  logInterval.value = setInterval(() => {
    const severity = Math.random() > 0.7 ? 'error' : 'info';
    logs.value.push({
      level: severity,
      message: `[${new Date().toLocaleTimeString()}] ${severity.toUpperCase()} - 模拟日志内容 ${logs.value.length + 1}`
    });
    if (logs.value.length > 100) logs.value.shift();
  }, 5000);
});

onUnmounted(() => clearInterval(logInterval.value));

// 图表相关逻辑
const chartRef1 = ref(null); // 第一个图表容器的引用
const chartRef2 = ref(null); // 第二个图表容器的引用
const chartRef3 = ref(null); // 第三个图表容器的引用
const chartRef4 = ref(null); // 第四个图表容器的引用

// 准确率数据（满分是100）
const chartData = ref([
  {name: '异常流量判断率', value: 99.87},
  {name: '文件上传判断率', value: 99.51},
  {name: '密码强弱判断准确率', value: 98.97},
  {name: '日志准确度', value: 98.97},
]);

onMounted(() => {
  // 初始化四个 ECharts 实例
  const charts = [
    echarts.init(chartRef1.value),
    echarts.init(chartRef2.value),
    echarts.init(chartRef3.value),
    echarts.init(chartRef4.value),
  ];

  // 配置图表选项
  const options = chartData.value.map((item, index) => ({
    title: {
      text: item.name,
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'bold',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}%',
    },
    series: [
      {
        name: item.name,
        type: 'pie',
        radius: ['50%', '70%'], // 设置为环形图
        center: ['50%', '60%'], // 调整图表中心位置
        avoidLabelOverlap: false,
        label: {
          show: true,
          position: 'center',
          formatter: `{@value}%`, // 显示百分比
          fontSize: 16,
          fontWeight: 'bold',
          color: '#333',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold',
          },
        },
        data: [{name: item.name, value: item.value}],
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2,
        },
        backgroundColor: '#f0f0f0', // 背景颜色
      },
    ],
  }));

  // 设置每个图表的选项并渲染
  charts.forEach((chartInstance, index) => {
    chartInstance.setOption(options[index]);
  });
});
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
  background-color: rgba(0, 0, 0, 0.5);
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

.chart-container {
  margin-top: 20px;
  text-align: center;
}

.chart-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.chart {
  width: 200px; /* 每个图表的宽度 */
  height: 200px; /* 每个图表的高度 */
}
</style>
