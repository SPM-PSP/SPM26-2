package com.itheim.program_platform_backend.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "提交记录查询参数")
public class SubmissionQueryDTO {

    @ApiModelProperty(value = "页码，默认1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页条数，默认10")
    private Integer size = 10;

    @ApiModelProperty(value = "评测结果：0-通过 1-编译错误 2-运行错误 3-超时")
    private Integer result;

    @ApiModelProperty(value = "题目关键词搜索（题目标题/描述）")
    private String keyword;

    @ApiModelProperty(value = "题目类别")
    private List<String> problemCategory;

    @ApiModelProperty(value = "排序方式：desc(默认),asc")
    private String sortOrder = "desc";
}
