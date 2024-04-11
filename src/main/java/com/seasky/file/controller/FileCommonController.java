package com.seasky.file.controller;

import com.seasky.budget.entity.IdCmd;
import com.seasky.core.common.Response;
import com.seasky.core.common.ResponseCode;
import com.seasky.core.common.Result;
import com.seasky.core.util.ExceptionUtil;
import com.seasky.file.po.*;
import com.seasky.file.service.FileDetailService;
import com.seasky.file.service.FileStorageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.seasky.core.common.Response.ok;
@RequestMapping("/XSpring")
@Api(tags = "文件传输")
@RestController
@Slf4j
public class FileCommonController {
    @Resource
    private FileStorageUtil fileStorageUtil;
    @Resource
    private FileDetailService fileDetailService;
    @PostMapping("/uploadList")
    @ApiOperation("上传批量")
    public Result<FileInfo> uploadList(FileStorageEntity fileStorageEntity){
        ExceptionUtil.isNull(fileStorageEntity.getFileList(),"上传文件不能为空");
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (MultipartFile multipartFile : fileStorageEntity.getFileList()) {
            FileInfo upload = fileStorageUtil.upload(multipartFile, fileStorageEntity);
            fileInfoList.add(upload);
        }
        return Response.ok(ResponseCode.SUCCESS,fileInfoList);
    }

    @PostMapping("/download")
    @ApiOperation("根据url下载文件")
    public Result<Object> download(HttpServletResponse response, @RequestParam("url") String url) throws IOException{
        fileStorageUtil.download(url,response);
        return ok(ResponseCode.SUCCESS);
    }

    @PostMapping("/queryFileDetailByObjectId")
    @ApiOperation("根据文件所属对象id查询文件信息")
    public Result<FileDetail> queryFileDetailByObjectId(@RequestParam("id") Long id){
        return ok(ResponseCode.SUCCESS,fileDetailService.queryFileDetailByObjectId(id));
    }

    @PostMapping("/existsFile")
    @ApiOperation("根据url判断文件是否存在")
    public Result<Boolean> queryFileDetailByObjectId(@RequestParam("url") String url){
        boolean exists = fileStorageUtil.exists(url);
        return ok(ResponseCode.SUCCESS,exists);
    }

    @PostMapping("/updateBatchById")
    @ApiOperation("更新附件")
    public Result<Boolean> updateBatchById(@RequestBody List<FileDetail> list){
        return ok(ResponseCode.SUCCESS,fileDetailService.updateBatchById(list));
    }

    @PostMapping("/removeBatchByIds")
    @ApiOperation("根据文件id集合删除附件")
    public Result<Boolean> removeBatchByIds(@RequestBody IdCmd idCmd){
        return ok(ResponseCode.SUCCESS,fileDetailService.removeBatchByIds(idCmd.getIdList()));
    }

    @PostMapping("/initPart")
    @ApiOperation("初始化分片任务")
    public Result<FileInfo> initPart(@RequestBody FileTaskPartEntity fileTaskPartEntity){
        return ok(ResponseCode.SUCCESS,fileStorageUtil.initPart(fileTaskPartEntity));
    }

    @PostMapping("/checkPart")
    @ApiOperation("判断分片是否上传完成")
    public Result<Boolean> checkPart(@RequestBody FilePartCheckEntity filePartCheckEntity){
        return ok(ResponseCode.SUCCESS,fileStorageUtil.checkPart(filePartCheckEntity.getUrl(),filePartCheckEntity.getIndex()));
    }

    @PostMapping("/uploadPart")
    @ApiOperation("分片上传")
    public Result<Boolean> uploadPart(FilePartUploadEntity filePartUploadEntity){
        return ok(ResponseCode.SUCCESS,fileStorageUtil.uploadPart(filePartUploadEntity));
    }

    @PostMapping("/mergePart")
    @ApiOperation("合并分片")
    public Result<Boolean> mergePart(@RequestBody FilePartCheckEntity filePartCheckEntity){
        return ok(ResponseCode.SUCCESS,fileStorageUtil.mergePart(filePartCheckEntity.getUrl()));
    }

    @PostMapping("/cancelPart")
    @ApiOperation("取消分片上传")
    public Result<Boolean> cancelPart(@RequestBody FilePartCheckEntity filePartCheckEntity){
        return ok(ResponseCode.SUCCESS,fileStorageUtil.cancelPart(filePartCheckEntity.getUrl()));
    }
}
