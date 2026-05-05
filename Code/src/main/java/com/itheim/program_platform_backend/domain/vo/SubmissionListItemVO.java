package com.itheim.program_platform_backend.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "提交记录列表项")
public class SubmissionListItemVO {

    @ApiModelProperty(value = "提交记录ID")
    private Long submissionId;

    @ApiModelProperty(value = "题目ID")
    private Long problemId;

    @ApiModelProperty(value = "题目标题")
    private String problemTitle;

    @ApiModelProperty(value = "题目分类")
    private String problemCategory;

    @ApiModelProperty(value = "评测结果：0-通过 1-编译错误 2-运行错误 3-超时")
    private Integer status;

    @ApiModelProperty(value = "提交时间")
    private LocalDateTime submitTime;
}
