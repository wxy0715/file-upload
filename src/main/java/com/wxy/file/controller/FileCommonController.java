package com.wxy.file.controller;

import com.wxy.file.po.*;
import com.wxy.file.result.DataResult;
import com.wxy.file.service.FileDetailService;
import com.wxy.file.service.FileStorageUtil;
import com.wxy.file.util.ExceptionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/XSpring")
@Api(tags = "文件传输")
@RestController
@Slf4j
public class FileCommonController {
    @Resource
    private FileStorageUtil fileStorageUtil;
    @Resource
    private FileDetailService fileDetailService;

    @PostMapping("/upload")
    @ApiOperation("上传")
    public DataResult<FileInfo> upload(@RequestParam("file") MultipartFile file, FileStorageEntity fileStorageEntity){
        return DataResult.success(fileStorageUtil.upload(file,fileStorageEntity));
    }

    @PostMapping("/uploadList")
    @ApiOperation("上传批量")
    public DataResult<List<FileInfo>> uploadList(FileStorageEntity fileStorageEntity){
        ExceptionUtil.isNull(fileStorageEntity.getFileList(),"上传文件不能为空");
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (MultipartFile multipartFile : fileStorageEntity.getFileList()) {
            FileInfo upload = fileStorageUtil.upload(multipartFile, fileStorageEntity);
            fileInfoList.add(upload);
        }
        return DataResult.success(fileInfoList);
    }

    @PostMapping("/download")
    @ApiOperation("根据url下载文件")
    public DataResult<Object> download(HttpServletResponse response, @RequestParam("url") String url) throws IOException{
        fileStorageUtil.download(url,response);
        return DataResult.success();
    }

    @PostMapping("/queryFileDetailByObjectId")
    @ApiOperation("根据文件所属对象id查询文件信息")
    public DataResult<List<FileDetail>> queryFileDetailByObjectId(@RequestParam("id") Long id){
        return DataResult.success(fileDetailService.queryFileDetailByObjectId(id));
    }

    @PostMapping("/existsFile")
    @ApiOperation("根据url判断文件是否存在")
    public DataResult<Boolean> queryFileDetailByObjectId(@RequestParam("url") String url){
        boolean exists = fileStorageUtil.exists(url);
        return DataResult.success(exists);
    }

    @PostMapping("/updateBatchByObjectId")
    @ApiOperation("通过objectId更新附件")
    public DataResult<Boolean> updateBatchByObjectId(@RequestBody FileUpdateEntity updateEntity){
        return DataResult.success(fileStorageUtil.updateBatchByObjectId(updateEntity));
    }
    @PostMapping("/removeBatchByIds")
    @ApiOperation("根据文件id集合删除附件")
    public DataResult<Boolean> removeBatchByIds(@RequestBody IdCmd idCmd){
        return DataResult.success(fileDetailService.removeBatchByIds(idCmd.getIdList()));
    }

    @PostMapping("/initPart")
    @ApiOperation("初始化分片任务")
    public DataResult<FileInfo> initPart(@RequestBody FileTaskPartEntity fileTaskPartEntity){
        return DataResult.success(fileStorageUtil.initPart(fileTaskPartEntity));
    }

    @PostMapping("/checkPart")
    @ApiOperation("判断分片是否上传完成")
    public DataResult<Boolean> checkPart(@RequestBody FilePartCheckEntity filePartCheckEntity){
        return DataResult.success(fileStorageUtil.checkPart(filePartCheckEntity.getUrl(),filePartCheckEntity.getIndex()));
    }

    @PostMapping("/uploadPart")
    @ApiOperation("分片上传")
    public DataResult<Boolean> uploadPart(FilePartUploadEntity filePartUploadEntity){
        return DataResult.success(fileStorageUtil.uploadPart(filePartUploadEntity));
    }

    @PostMapping("/mergePart")
    @ApiOperation("合并分片")
    public DataResult<Boolean> mergePart(@RequestBody FilePartCheckEntity filePartCheckEntity){
        return DataResult.success(fileStorageUtil.mergePart(filePartCheckEntity.getUrl()));
    }

    @PostMapping("/cancelPart")
    @ApiOperation("取消分片上传")
    public DataResult<Boolean> cancelPart(@RequestBody FilePartCheckEntity filePartCheckEntity){
        return DataResult.success(fileStorageUtil.cancelPart(filePartCheckEntity.getUrl()));
    }
}
