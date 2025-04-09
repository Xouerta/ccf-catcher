package com.ccf.sercurity.vo;

public record AnalysisFileResultVO(
        long maliciousCount,
        long safeCount,
        long totalFiles
) {
}