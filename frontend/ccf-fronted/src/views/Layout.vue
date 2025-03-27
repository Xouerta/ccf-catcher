<template>
  <el-container class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header height="60px">
      <div class="header-content">
        <h3>流量分析管理平台</h3>
        <div class="user-info">
          <el-dropdown trigger="click">
            <span class="user-name">用户名称</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push({ name: 'PasswordModification' })">修改密码</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
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
import {createRouter as $router, useRouter} from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-header {
  background-color: #545c64;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
}

.el-aside {
  background-color: #545c64;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
}
</style>
