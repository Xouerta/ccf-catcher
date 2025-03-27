<template>
  <div class="error-page">
    <el-container>
      <el-main>
        <div class="error-content">
          <el-image :src="notFoundImage" fit="contain" class="error-image" />
          <h1>404</h1>
          <p>抱歉，您访问的页面不存在。</p>
          <el-button type="primary" @click="goHome">返回首页</el-button>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
import notFoundImage from '@/assets/404.webp';

const router = useRouter();

const goHome = () => {
  router.push('/');
};
export default {
  name: 'NotFound',
  setup() {
    return {
      notFoundImage,
    };
  },
};
</script>

<style scoped>
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f4f4f4;
}

.error-content {
  text-align: center;
}

.error-image {
  width: 300px;
  margin-bottom: 20px;
}

h1 {
  font-size: 72px;
  color: #409eff;
  margin: 10px 0;
}

p {
  font-size: 18px;
  color: #666;
  margin-bottom: 20px;
}
</style>
