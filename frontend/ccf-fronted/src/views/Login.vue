<template>
  <el-container class="login-container">
    <!-- 左右分栏布局 -->
    <el-row :gutter="0" style="height: 100%">
      <!-- 左侧登录区域（70%宽度） -->
      <el-col
          :xs="24"
          :sm="12"
          :md="12"
          :lg="12"
          :xl="12"
          class="left-panel">
        <el-card class="login-card">
          <template #header>
            <div class="card-header">
              <span>登录</span>
            </div>
          </template>
          <el-form
              ref="loginForm"
              :model="form"
              :rules="rules"
              label-width="100px"
              class="form-container">
            <!-- 邮箱输入 -->
            <el-form-item label="邮箱" prop="email">
              <el-input
                  v-model="form.email"
                  placeholder="请输入邮箱"
                  class="input-style"/>
            </el-form-item>

            <!-- 密码输入 -->
            <el-form-item label="密码" prop="password">
              <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  class="input-style"/>
            </el-form-item>

            <!-- 登录按钮 -->
            <el-form-item>
              <el-button
                  type="primary"
                  @click="submitForm(loginForm)"
                  class="login-btn">
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧标题区域（30%宽度） -->
      <el-col
          :xs="24"
          :sm="12"
          :md="12"
          :lg="12"
          :xl="12"
          class="right-panel">
        <div class="title-container">
          <h1 class="main-title">智能网络分析管理平台</h1>
          <p class="sub-title">安全、高效、智能的网络行为分析解决方案</p>
        </div>
      </el-col>
    </el-row>
  </el-container>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import apiClient from "@/api/axiosInstance.js";

const router = useRouter();
const loginForm = ref(null);

const form = reactive({
  email: '',
  password: ''
});

// 邮箱格式验证
const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
  if (!emailRegex.test(value)) {
    callback(new Error('请输入有效的邮箱地址'));
  } else {
    callback();
  }
};

// 表单验证规则
const rules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
});

// 表单提交
const submitForm = (formEl) => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      apiClient.post('/user/login', {
        email: form.email,
        password: form.password
      }).then(response => {
        if (response.status === 200) {
          localStorage.setItem('token', response.data.token);
          ElMessage.success('登录成功');
          router.push({ name: 'Home' });
        }
      }).catch(error => {
        if (error.response?.status === 401) {
          ElMessage.error('邮箱或密码错误');
        } else {
          ElMessage.error('接口调用失败，请重试');
        }
      });
    } else {
      ElMessage.warning('请检查表单');
      return false;
    }
  });
};
</script>

<style scoped>
/* 主容器样式 */
.login-container {
  height: 100vh;
  background-color: #00a2e8; /* 天蓝色主题 */
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 左侧登录区域 */
.left-panel {
  background-color: #ffffff; /* 白色背景 */
  padding: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 100%;
  max-width: 480px;
}

.form-container {
  width: 100%;
  padding: 32px;
}

/* 右侧标题区域 */
.right-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0 40px;
  background: linear-gradient(90deg, #00a2e8 0%, #00c3ff 100%);
}

.title-container {
  text-align: center;
  color: #ffffff;
}

.main-title {
  font-size: 36px;
  font-weight: 600;
  margin-bottom: 20px;
}

.sub-title {
  font-size: 18px;
  color: #ffffff99;
}

/* 表单样式 */
.input-style {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  transition: border-color 0.2s;
}

.input-style:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
}

.login-btn {
  width: 100%;
  margin-top: 24px;
  height: 48px;
  border-radius: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .el-row {
    flex-direction: column;
  }

  .left-panel, .right-panel {
    width: 100%;
  }

  .right-panel {
    padding: 20px;
    background: #00a2e8;
  }
}
</style>
