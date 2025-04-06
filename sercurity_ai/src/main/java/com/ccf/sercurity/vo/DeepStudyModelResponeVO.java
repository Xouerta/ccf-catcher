package com.ccf.sercurity.vo;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record DeepStudyModelResponeVO(
        @Schema(name = "模型准确率", example = "0.77")
        Double accuracy,

        @Schema(name = "混淆矩阵", example = "[[13,23],[0,64]]")
        @JsonProperty("confusion_matrix")
        int[][] confusionMatrix,

        @Schema(name = "F1分数", example = "0.847")
        @JsonProperty("f1_score")
        Double f1Score,

        @Schema(name = "精确率", example = "0.735")
        Double precision,

        @Schema(name = "召回率", example = "1.0")
        Double recall,

        @Schema(name = "ROC AUC值", example = "0.7803")
        @JsonProperty("roc_auc")
        Double rocAuc
) {
}