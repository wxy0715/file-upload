package com.wxy.file.po;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Api(tags = "文件分片效验")
public class FilePartCheckEntity {
    @ApiModelProperty(value = "主文件url")
    private String url;

    @ApiModelProperty(value = "索引值")
    private Integer index;
}
