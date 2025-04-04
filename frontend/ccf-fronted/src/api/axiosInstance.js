// src/api/axiosInstance.js
import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost:5173',
    timeout: 60000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 全局设置 Token（动态获取）
apiClient.defaults.headers.Authorization = `Bearer ${localStorage.getItem('token')}`;

// 添加拦截器处理 Token 过期（示例逻辑）
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                // 调用刷新 Token 的接口（示例）
                const newToken = await axios.post('/auth/refresh', {
                    refreshToken: localStorage.getItem('refreshToken'),
                });
                localStorage.setItem('token', newToken.data.accessToken);
                apiClient.defaults.headers.Authorization = `Bearer ${newToken.data.accessToken}`;
                return apiClient(originalRequest);
            } catch (err) {
                // 刷新失败，跳转登录页
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default apiClient;
