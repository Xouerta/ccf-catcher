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
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import apiClient from "@/api/axiosInstance.js";

const passwordForm = ref(null);
const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
});

// 验证码发送控制
const sendCodeDisabled = ref(false);
const sendCodeText = ref('发送验证码');
const sendCodeTimer = ref(null);

// 密码强度状态
const strengthLevel = ref({
  type: 'info',
  message: '请输入新密码'
});

// 表单验证规则
const rules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  newPassword: [
    {required: true, message: '请输入新密码', trigger: 'blur'},
    {
      validator: (rule, value) => value.length >= 8,
      message: '密码长度至少8位',
      trigger: 'blur'
    },
    {
      async validator(rule, value) {
        if (!value) return new Error('请输入密码');
        const res = await apiClient.post('/user/checkPassword', {
          checkPasswordRequestVO: {password: value}
        });
        if (res.data.isWeak) {
          return new Error('密码属于弱密码，请选择更复杂的密码');
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {required: true, message: '请确认新密码', trigger: 'blur'},
    {
      validator: (rule, value) => value === form.newPassword,
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ]
});

// 发送验证码
const sendVerificationCode = async () => {
  if (!form.email) {
    ElMessage.warning('请输入邮箱');
    return;
  }

  try {
    // 调用新验证码接口
    await apiClient.get('/user/code', {
      params: {
        email: form.email,
        type: false // type参数为布尔值，false表示修改密码场景
      }
    });
    ElMessage.success('验证码已发送，请注意查收');

    // 启动倒计时
    sendCodeDisabled.value = true;
    sendCodeText.value = '60秒后重发';
    let count = 60;
    sendCodeTimer.value = setInterval(() => {
      count--;
      sendCodeText.value = `${count}秒后重发`;
      if (count <= 0) {
        clearInterval(sendCodeTimer.value);
        sendCodeDisabled.value = false;
        sendCodeText.value = '发送验证码';
      }
    }, 1000);
  } catch (error) {
    ElMessage.error('验证码发送失败，请重试');
  }
};

// 密码强度检测
const checkPasswordStrength = async () => {
  if (!form.newPassword) {
    strengthLevel.value = {
      type: 'info',
      message: '请输入新密码'
    };
    return;
  }

  try {
    const res = await apiClient.post('/user/checkPassword', {
        password: form.newPassword
    });

    // 安全访问 isWeak 字段，确保数据存在
    const isWeak = res.data?.isWeak; // 使用可选链
    if (typeof isWeak !== 'boolean') {
      throw new Error('接口返回数据格式不正确，缺少 isWeak 字段或类型错误');
    }

    strengthLevel.value = {
      type: isWeak ? 'danger' : 'success',
      message: isWeak
          ? '密码强度不足，属于弱密码'
          : '密码强度良好'
    };
  } catch (error) {
    // 打印详细错误信息
    console.error('密码强度检测失败:', error);
    strengthLevel.value = {
      type: 'error',
      message: '密码强度低'
    };
  }
};


// 提交表单
const submitForm = async (formEl) => {
  if (!formEl) return;
  formEl.validate(async (valid) => {
    if (valid) {
      try {
        // 调用新修改密码接口
        await apiClient.post('/user/updatePassword', {
          updatePasswordRequestVO: {
            email: form.email,
            newPassword: form.newPassword,
            code: form.code
          }
        });
        ElMessage.success('密码修改成功');
        // 清空表单
        Object.assign(form, {
          email: '',
          code: '',
          newPassword: '',
          confirmPassword: ''
        });
      } catch (error) {
        ElMessage.error(
            error.response?.data?.message || '密码修改失败，请检查输入内容'
        );
      }
    }
  });
};
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
