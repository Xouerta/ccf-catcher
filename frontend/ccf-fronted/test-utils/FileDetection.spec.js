// FileDetection.spec.js
import { mount } from '@vue/test-utils';
import FileDetection from '@/views/FileDetection.vue';
import mockData from '../mocks/mockData.js';

describe('FileDetection.vue', () => {
    it('显示分析结果', async () => {
        const wrapper = mount(FileDetection);
        // 模拟分析结果数据
        wrapper.vm.analysisResult = mockData.analysisResult;
        await wrapper.vm.$nextTick();

        await expect(wrapper.find('.result-item').exists()).toBe(true);
    });
});
