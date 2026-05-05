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
@ApiModel(description = "提交记录详情")
public class SubmissionDetailVO {

    @ApiModelProperty(value = "提交记录ID")
    private Long submissionId;

    @ApiModelProperty(value = "题目ID")
    private Long problemId;

    @ApiModelProperty(value = "题目标题")
    private String problemTitle;

    @ApiModelProperty(value = "编程语言")
    private String language;

    @ApiModelProperty(value = "代码内容")
    private String code;

    @ApiModelProperty(value = "评测结果：0-通过 1-编译错误 2-运行错误 3-超时")
    private Integer result;

    @ApiModelProperty(value = "通过测试用例数")
    private Integer passCount;

    @ApiModelProperty(value = "总测试用例数")
    private Integer totalCount;

    @ApiModelProperty(value = "运行时间（毫秒）")
    private Integer runTime;

    @ApiModelProperty(value = "内存占用（KB）")
    private Integer memory;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

    @ApiModelProperty(value = "提交时间")
    private LocalDateTime submitTime;
}
