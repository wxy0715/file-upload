package com.seasky.file.po;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Api(tags = "分片上传")
public class FilePartUploadEntity {

    @ApiModelProperty(value = "url", required = true)
    private String url = "";


    @ApiModelProperty(value = "索引", required = true)
    private Integer index;

    @JSONField(serialize = false)
    @ApiModelProperty(value = "文件", required = true)
    private MultipartFile file;
}
