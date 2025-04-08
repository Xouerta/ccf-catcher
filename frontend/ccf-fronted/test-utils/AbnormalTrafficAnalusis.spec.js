// @/tests/unit/AbnormalTrafficAnalysis.spec.js
import { mount } from '@vue/test-utils';
import AbnormalTrafficAnalysis from '@/views/AbnormalTrafficAnalysis.vue';
import axiosClient from '@/api/axiosInstance.js'; // 假设apiClient是axios的封装
import mockData from '../mocks/trafficMockData.js'; // 引入mock数据

// 模拟API响应
jest.mock('@/api/axiosInstance.js', () => ({
    get: jest.fn(),
}));

describe('AbnormalTrafficAnalysis.vue', () => {
    let wrapper;

    beforeEach(() => {
        // 重置mock状态
        axiosClient.get.mockClear();
        wrapper = mount(AbnormalTrafficAnalysis);
    });

    // 测试初始加载数据
    it('加载初始数据', async () => {
        // 预设API返回值
        axiosClient.get.mockResolvedValue({
            data: {
                list: mockData.tableData.slice(0, 10),
                total: mockData.total,
            },
        });

        // 等待异步加载完成
        await wrapper.vm.$nextTick();

        // 断言数据是否正确绑定
        expect(axiosClient.get).toHaveBeenCalledWith('/traffic/list', {
            params: { page: 1, size: 10 },
            headers: { Authorization: 'Bearer ' + localStorage.getItem('token') },
        });
        expect(wrapper.vm.tableData).toEqual(mockData.tableData.slice(0, 10));
        expect(wrapper.vm.total).toBe(mockData.total);
    });

    // 测试分页大小改变
    it('改变分页大小', async () => {
        // 模拟API返回新分页数据
        axiosClient.get.mockResolvedValue({
            data: { list: mockData.tableData.slice(0, 20), total: mockData.total },
        });

        // 触发分页大小改变事件
        const pageSizeSelect = wrapper.find('.el-pagination__sizes select');
        await pageSizeSelect.setValue('20');

        // 断言分页参数和API调用
        expect(wrapper.vm.pageSize).toBe(20);
        expect(wrapper.vm.currentPage).toBe(1);
        expect(axiosClient.get).toHaveBeenCalledWith('/traffic/list', {
            params: { page: 1, size: 20 },
        });
    });

    // 测试页码跳转
    it('跳转到第2页', async () => {
        axiosClient.get.mockResolvedValue({
            data: { list: mockData.tableData.slice(10, 20), total: mockData.total },
        });

        // 触发页码改变事件
        const pageInput = wrapper.find('.el-pager li:nth-child(2)');
        await pageInput.trigger('click');

        expect(wrapper.vm.currentPage).toBe(2);
        expect(axiosClient.get).toHaveBeenCalledWith('/traffic/list', {
            params: { page: 2, size: 10 },
        });
    });

    // 测试API错误处理
    it('处理API错误', async () => {
        const errorMessage = '接口调用失败';
        axiosClient.get.mockRejectedValue(new Error(errorMessage));

        // 捕获控制台错误
        console.error = jest.fn();

        await wrapper.vm.$nextTick();

        expect(console.error).toHaveBeenCalledWith('数据加载失败:', expect.any(Error));
        expect(wrapper.vm.tableData).toEqual([]);
        expect(wrapper.vm.total).toBe(0);
    });

    // 测试表格时间戳渲染
    it('正确渲染时间戳', () => {
        const timestamp = 1710000000000; // 示例时间戳
        const row = { timestamp };
        const formattedTime = new Date(timestamp).toLocaleString();

        const renderedTime = wrapper.vm.$options.renderTime(row);
        expect(renderedTime).toBe(formattedTime);
    });
});
