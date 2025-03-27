<template>
  <el-card class="password-card">
    <template #header>
      <div class="card-header">
        <span>修改密码</span>
      </div>
    </template>

    <el-form
        ref="passwordForm"
        :model="form"
        :rules="rules"
        label-width="120px"
    >
      <!-- 邮箱 -->
      <el-form-item label="邮箱" prop="email">
        <el-input
            v-model="form.email"
            placeholder="请输入注册邮箱"
        />
      </el-form-item>

      <!-- 验证码 -->
      <el-form-item label="验证码" prop="code">
        <div class="code-input-container">
          <el-input v-model="form.code" placeholder="请输入验证码" />
          <el-button
              type="primary"
              :disabled="!form.email || sendCodeDisabled"
              @click="sendVerificationCode"
          >
            {{ sendCodeText }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 新密码 -->
      <el-form-item label="新密码" prop="newPassword">
        <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            @input="checkPasswordStrength"
        />
        <!-- 密码强度提示 -->
        <div class="strength-display">
          <el-alert
              :type="strengthLevel.type"
              :title="strengthLevel.message"
              :closable="false"
              show-icon
          />
        </div>
      </el-form-item>

      <!-- 确认密码 -->
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
        />
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button
            type="primary"
            @click="submitForm(passwordForm)"
            style="width: 100%"
        >
          确认修改
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const passwordForm = ref(null)
const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证码发送控制
const sendCodeDisabled = ref(false)
const sendCodeText = ref('发送验证码')
const sendCodeTimer = ref(null)

// 密码强度状态
const strengthLevel = ref({
  type: 'info',
  message: '请输入新密码'
})

// 表单验证规则
const rules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: ['blur', 'change']
    }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (rule, value) => value.length >= 8,
      message: '密码长度至少8位',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value) => value === form.newPassword,
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ]
})

// 发送验证码
const sendVerificationCode = async () => {
  if (!form.email) {
    ElMessage.warning('请输入邮箱')
    return
  }

  try {
    // 发送验证码到邮箱（假设存在发送验证码的接口）
    await axios.post('/api/send-verification-code', {
      email: form.email
    })
    ElMessage.success('验证码已发送，请注意查收')

    // 启动倒计时
    sendCodeDisabled.value = true
    sendCodeText.value = '60秒后重发'
    let count = 60
    sendCodeTimer.value = setInterval(() => {
      count--
      sendCodeText.value = `${count}秒后重发`
      if (count <= 0) {
        clearInterval(sendCodeTimer.value)
        sendCodeDisabled.value = false
        sendCodeText.value = '发送验证码'
      }
    }, 1000)
  } catch (error) {
    ElMessage.error('验证码发送失败，请重试')
  }
}

// 密码强度检测
const checkPasswordStrength = async () => {
  if (!form.newPassword) {
    strengthLevel.value = {
      type: 'info',
      message: '请输入新密码'
    }
    return
  }

  try {
    const res = await axios.post('/api/lookpassword', {
      password: form.newPassword
    })
    const { strength, message } = res.data // 假设接口返回强度信息

    strengthLevel.value = {
      type: strength === 'weak' ? 'danger'
          : strength === 'medium' ? 'warning'
              : 'success',
      message: message
    }
  } catch (error) {
    strengthLevel.value = {
      type: 'error',
      message: '密码强度检测失败'
    }
  }
}

// 提交表单
const submitForm = async (formEl) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      try {
        // 调用修改密码接口
        await axios.post('/user/updatePassword', {
          email: form.email,
          newPassword: form.newPassword,
          code: form.code
        })
        ElMessage.success('密码修改成功')
        // 清空表单
        Object.assign(form, {
          email: '',
          code: '',
          newPassword: '',
          confirmPassword: ''
        })
      } catch (error) {
        ElMessage.error('密码修改失败：' + error.response?.data?.message || '未知错误')
      }
    }
  })
}
</script>

<style scoped>
.password-card {
  margin: 20px;
  padding: 20px;
  width: 400px;
}

.code-input-container {
  display: flex;
  gap: 10px;
}

.strength-display {
  margin-top: 8px;
}

.el-alert {
  margin-top: 8px;
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
