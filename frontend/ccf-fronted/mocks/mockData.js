export default {
    // 分析结果详情
    analysisResult: {
        id: '123',
        originalName: "report.pdf",
        fileSize: 123456,
        contentType: "application/pdf",
        uploadTime: "2024-03-15 14:30:00",
        confidence: "95%",
        detectionTime: "2024-03-15 14:32:18",
        malicious: true,
        filePath: "/uploads/report_20240315.pdf",
        storedName: "report_20240315_123.pdf",
        details: "检测到潜在恶意宏代码"
    },
    // 文件列表
    files: [
        {
            originalName: "document.docx",
            uploadTime: "2024-03-14 09:15:00",
            malicious: false
        },
        {
            originalName: "log.txt",
            uploadTime: "2024-03-14 11:20:00",
            malicious: true
        }
    ],
    // 分页配置
    total: 20,
    currentPage: 1,
    pageSize: 10
};
