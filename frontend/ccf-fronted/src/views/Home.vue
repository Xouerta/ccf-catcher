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
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import apiClient from "@/api/axiosInstance.js"; // 引入API客户端

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

// 图表相关逻辑
const chartRef1 = ref(null); // 第一个图表容器的引用
const chartRef2 = ref(null); // 第二个图表容器的引用
const chartRef3 = ref(null); // 第三个图表容器的引用
const chartRef4 = ref(null); // 第四个图表容器的引用

// 图表数据（初始为空）
const chartData = ref([]);

// 实时日志数据
const logs = ref([]);
const ws = ref(null); // WebSocket实例

// 获取用户ID函数
const getUserId = async () => {
  try {
    const response = await apiClient.get('/user/info');
    const data = response.data;
    console.log('获取到的用户ID:', data.id);
    return data.id; // 返回用户ID
  } catch (error) {
    console.error('获取用户ID失败:', error);
    throw error;
  }
};

// WebSocket连接逻辑
const initWebSocket = async (userId) => {
  try {
    // 动态替换WebSocket URL中的{userId}
    ws.value = new WebSocket(
        `${import.meta.env.VITE_APP_WS_URL}/websocket/${userId}`
    );

    ws.value.addEventListener('open', () => {
      console.log('WebSocket连接已建立');
    });

    ws.value.addEventListener('message', (event) => {
      try {
        const logData = JSON.parse(event.data);
        if (logData.level && logData.message) {
          logs.value.push(logData);
          if (logs.value.length > 100) logs.value.shift();
        }
      } catch (error) {
        console.error('日志数据解析失败:', error);
      }
    });

    ws.value.addEventListener('error', (error) => {
      console.error('WebSocket连接异常:', error);
    });

    ws.value.addEventListener('close', () => {
      console.log('WebSocket连接已关闭');
    });
  } catch (error) {
    console.error('WebSocket初始化失败:', error);
  }
};

// 初始化图表
const initCharts = async () => {
  await nextTick(); // 确保DOM已渲染

  if (chartData.value.length === 0) {
    console.warn('chartData 为空，无法初始化图表');
    return;
  }

  const charts = [
    echarts.init(chartRef1.value),
    echarts.init(chartRef2.value),
    echarts.init(chartRef3.value),
    echarts.init(chartRef4.value),
  ];

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
        radius: ['50%', '70%'],
        center: ['50%', '60%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          position: 'center',
          formatter: `{@value}%`,
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
        data: item.data,
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2,
        },
        backgroundColor: '#f0f0f0',
      },
    ],
  }));

  charts.forEach((chartInstance, index) => {
    if (chartInstance) {
      chartInstance.setOption(options[index]);
    }
  });
};

// 定时更新图表数据
const updateChartData = async () => {
  try {
    // 请求接口获取数据
    const trafficResponse = await apiClient.get('/traffic/analyze');
    const filesResponse = await apiClient.get('/files/analyze');
    const logsResponse = await apiClient.get('/logs/analyze');
    const deepStudyLogResponse = await apiClient.get('/deepStudyLog/analyze');

    // 更新图表数据
    chartData.value = [
      {
        name: '异常流量分析',
        data: [
          {name: '正常', value: 100 - parseFloat(trafficResponse.data)},
          {name: '异常', value: parseFloat(trafficResponse.data)},
        ],
      },
      {
        name: '异常文件分析',
        data: [
          {name: '正常', value: (filesResponse.data.safeCount / filesResponse.data.totalFiles) * 100},
          {name: '异常', value: (filesResponse.data.maliciousCount / filesResponse.data.totalFiles) * 100},
        ],
      },
      {
        name: '日志分析',
        data: [
          {name: '日志准确度', value: parseFloat(logsResponse.data)}, // 假设返回的是一个百分比值
        ],
      },
      {
        name: 'AI日志分析',
        data: [
          {
            name: 'AI日志准确度',
            value: (deepStudyLogResponse.data.safeCount / deepStudyLogResponse.data.totalFiles) * 100
          },
        ],
      },
    ];

    // 确保DOM已渲染
    await nextTick();

    // 重新渲染图表
    const charts = [
      echarts.getInstanceByDom(chartRef1.value),
      echarts.getInstanceByDom(chartRef2.value),
      echarts.getInstanceByDom(chartRef3.value),
      echarts.getInstanceByDom(chartRef4.value),
    ];

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
          radius: ['50%', '70%'],
          center: ['50%', '60%'],
          avoidLabelOverlap: false,
          label: {
            show: true,
            position: 'center',
            formatter: `{@value}%`,
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
          data: item.data,
          itemStyle: {
            borderColor: '#fff',
            borderWidth: 2,
          },
          backgroundColor: '#f0f0f0',
        },
      ],
    }));

    charts.forEach((chartInstance, index) => {
      if (chartInstance) {
        chartInstance.setOption(options[index], true); // true 表示保留旧配置
      }
    });
  } catch (error) {
    console.error('更新图表数据失败:', error);
  }
};

// 生命周期钩子
onMounted(async () => {
  try {
    // 1. 获取用户ID
    const userId = await getUserId();

    // 2. 初始化WebSocket
    await initWebSocket(userId);

    // 3. 初始数据加载
    await updateChartData();

    // 4. 初始化图表
    await initCharts();

    // 5. 每五分钟定时更新图表数据
    setInterval(updateChartData, 5 * 60 * 1000); // 每5分钟调用一次
  } catch (error) {
    console.error('初始化失败:', error);
  }
});

onUnmounted(() => {
  if (ws.value && ws.value.readyState === WebSocket.OPEN) {
    ws.value.close();
  }
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
