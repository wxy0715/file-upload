package com.wxy.file.po;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Api(tags = "初始化分片任务")
public class FileTaskPartEntity {
    @ApiModelProperty(value = "路径", required = false)
    private String path = "/";

    @ApiModelProperty(value = "关联对象id", required = false)
    private String objectId = null;

    @ApiModelProperty(value = "对象类型", required = false)
    private String objectType = "";

    @ApiModelProperty(value = "自定义属性,json格式", required = false)
    private String attr = "";

    @ApiModelProperty(value = "文件名称,使用byte[]、InputStream等方式上传，无法获取originalFilename属性时设置", required = true)
    private String originalFilename = "";

    @ApiModelProperty(value = "文件md5", required = true)
    private String md5 = "";

    @ApiModelProperty(value = "文件大小", required = true)
    private Long length;

    @ApiModelProperty(value = "存储平台", required = false)
    private String platform = "";
}
