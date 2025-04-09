<template>
  <el-container class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header height="60px">
      <div class="header-content">
        <h3>智能网络活动综合分析管理平台</h3>
        <div class="user-info">
          <el-dropdown trigger="click">
            <span class="user-name">用户名称</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push({ name: 'PasswordModification' })">修改密码</el-dropdown-item>
                <el-dropdown-item divided @click="handlelogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主体内容 -->
    <el-container>
      <!-- 左侧导航栏 -->
      <el-aside width="200px">
        <el-menu
            default-active="$route.name"
            router
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
        >
          <el-menu-item index="Home" :route="{ name: 'Home' }">
            <span>主页面</span>
          </el-menu-item>
          <el-menu-item index="AbnormalTrafficAnalysis" :route="{ name: 'AbnormalTrafficAnalysis' }">
            <span>异常流量分析</span>
          </el-menu-item>
          <el-menu-item index="FileDetection" :route="{ name: 'FileDetection' }">
            <span>异常文件检测</span>
          </el-menu-item>
          <el-menu-item index="PasswordModification" :route="{ name: 'PasswordModification' }">
            <span>修改用户密码</span>
          </el-menu-item>
          <el-menu-item index="LogMonitoring" :route="{ name: 'LogMonitoring' }">
            <span>AI日志检测</span>
          </el-menu-item>
          <el-menu-item index="AIlog" :route="{ name: 'AIlog' }">
            <span>日志检测</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 子页面容器 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();
const handlelogout = () => {
  localStorage.removeItem('token');
  router.push({ name: 'Login' });
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
  color: #409eff;
}

/* 顶部导航栏 */
.el-header {
  background: linear-gradient(90deg, #187fe7, #1e90ff);
  background-size: 200% 100%;
  animation: gradientAnimation 4s ease infinite;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

@keyframes gradientAnimation {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.user-name {
  margin-right: 10px;
  color: black;
  border: 1px solid #ccc;
  padding: 5px 10px;
  border-radius: 4px;
}

/* 左侧导航栏 */
:deep(.el-aside) {
  background: linear-gradient(180deg, #1978d7, #4169e1);
  background-size: 100% 200%;
  animation: asideGradientAnimation 4s ease infinite;
}

@keyframes asideGradientAnimation {
  0% {
    background-position: 50% 0%;
  }
  50% {
    background-position: 50% 100%;
  }
  100% {
    background-position: 50% 0%;
  }
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
}
</style>
