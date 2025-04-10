import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { layout: false } // 不使用公共布局
  },
  {
    path: '/',
    redirect: '/home',
    component: () => import('@/views/Layout.vue'),
    meta: { requiresAuth: true }, // 需要登录验证
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '主页面' }
      },
      {
        path: 'abnormal-traffic',
        name: 'AbnormalTrafficAnalysis',
        component: () => import('@/views/AbnormalTrafficAnalysis.vue'),
        meta: { title: '异常流量分析' }
      },
      {
        path: 'file-detection',
        name: 'FileDetection',
        component: () => import('@/views/FileDetection.vue'),
        meta: { title: '异常文件检测' }
      },
      {
        path: 'password',
        name: 'PasswordModification',
        component: () => import('@/views/PasswordModification.vue'),
        meta: { title: '修改用户密码' }
      },
      {
        path: 'log-monitor',
        name: 'LogMonitoring',
        component: () => import('@/views/LogMonitoring.vue'),
        meta: { title: '日志检测' }
      },
      {
        path: 'ai-log',
        name: 'AIlog',
        component: () => import('@/views/AIlog.vue'),
        meta: { title: 'AI日志检测' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

// 2. 添加路由守卫
const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_BASE_URL), // 关键修改
  routes,
  scrollBehavior() { return { top: 0 }; }
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
