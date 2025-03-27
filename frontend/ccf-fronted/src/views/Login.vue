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
        >
          <!-- 用户名 -->
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item label="密码" prop="password">
            <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                show-password
            />
          </el-form-item>

          <!-- 邮箱 -->
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
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

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loginForm = ref(null)

// 表单数据
const form = reactive({
  username: '',
  password: '',
  email: ''
})

// 表单验证规则
const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailRegex.test(value)) {
    callback(new Error('请输入有效的邮箱地址'))
  } else {
    callback()
  }
}

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ]
})

// 提交表单
const submitForm = (formEl) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      // 模拟登录验证（替换为真实接口）
      if (form.username === 'admin' && form.password === '123456') {
        ElMessage.success('登录成功')
        router.push({ name: 'Home' }) // 跳转主页面
      } else {
        ElMessage.error('用户名或密码错误')
      }
    } else {
      ElMessage.warning('请检查表单')
      return false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.box-card {
  width: 400px;
  border-radius: 8px;
}

.card-header {
  font-size: 20px;
  text-align: center;
}

.el-form {
  width: 80%;
  margin: 0 auto;
}
</style>
