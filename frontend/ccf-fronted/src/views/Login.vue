<template>
  <el-container class="login-container">
    <el-main>
      <el-card class="box-card">
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
            class="form-center"
        >
          <!-- 密码 -->
          <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                show-password
            />
          </el-form-item>
          <!-- 登录按钮 -->
          <el-form-item>
            <el-button
                type="primary"
                @click="submitForm(loginForm)"
                style="width: 100%"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>import axios from 'axios';
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

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
  if (!emailRegex.test(value)) {
    callback(new Error('请输入有效的邮箱地址'));
  } else {
    callback();
  }
};

const rules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
});

const submitForm = (formEl) => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      console.log("提交成功！",form);
      // 发送 JSON 格式请求（包裹在 loginRequestVO 对象中）
      apiClient.post('/user/login', {
          email: form.email,
          password: form.password

      })
          .then(response => {
            if (response.status === 200) {
              localStorage.setItem('token', response.data.token);
              ElMessage.success('登录成功');
              router.push({ name: 'Home' });
            }
          })
          .catch(error => {
            console.error('登录错误:', error.response?.data); // 添加详细错误日志
            if (error.response) {
              if (error.response.status === 401) {
                ElMessage.error('邮箱或密码错误');
              } else {
                ElMessage.error('接口调用失败，请重试');
              }
            } else {
              ElMessage.error('网络连接异常');
            }
          });
    } else {
      ElMessage.warning('请检查表单');
      return false;
    }
  });
};

</script>

<style scoped>.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.box-card {
  width: 480px; /* 扩大卡片宽度 */
  border-radius: 8px;
}

.card-header {
  font-size: 24px; /* 加大标题 */
  text-align: center;
  padding-bottom: 20px; /* 增加标题间距 */
}

.el-form {
  width: 100%;
  max-width: 480px;
  padding: 32px; /* 加大表单内边距 */
}

/* 表单居中 */
.form-center {
  display: flex;
  flex-direction: column;
  align-items: center; /* 水平居中 */
}

/* 输入框样式覆盖 */
::v-deep .el-input__inner {
  height: 48px;
  padding: 0 16px;
  font-size: 16px;
  border-radius: 8px;
  border: 1px solid #dcdfe6; /* 默认边框 */
  transition: border-color 0.2s;
  width: 300px; /* 调整输入框宽度 */
}

/* 激活状态样式优化 */
::v-deep .el-input.is-focus .el-input__inner {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
}

/* 登录按钮宽度调整 */
.el-button {
  width: 300px; /* 调整按钮宽度 */
}

.el-form-item {
  margin-bottom: 24px;
}
</style>
