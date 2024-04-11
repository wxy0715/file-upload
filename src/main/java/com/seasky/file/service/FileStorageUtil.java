package com.seasky.file.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.seasky.budget.util.BudgetObjectUtil;
import com.seasky.core.util.ExceptionUtil;
import com.seasky.core.util.StreamUtils;
import com.seasky.core.util.date.DateTool;
import com.seasky.core.util.date.enums.DateStyle;
import com.seasky.file.mapper.FileDetailMapper;
import com.seasky.file.po.FilePartUploadEntity;
import com.seasky.file.po.FileStorageEntity;
import com.seasky.file.po.FileTaskPartEntity;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.ProgressListener;
import org.dromara.x.file.storage.core.constant.Constant;
import org.dromara.x.file.storage.core.platform.FileStorage;
import org.dromara.x.file.storage.core.platform.MultipartUploadSupportInfo;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.dromara.x.file.storage.core.upload.FilePartInfoList;
import org.dromara.x.file.storage.core.upload.UploadPretreatment;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * 文件存储
 * @author wangxingyu
 */
@Component
@Slf4j
public class FileStorageUtil {
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private FileDetailMapper fileDetailMapper;

    /**
     * 上传文件
     */
    public FileInfo upload(MultipartFile file, FileStorageEntity fileStorageEntity) {
        return getFileInfo(fileStorageEntity, fileStorageService.of(file));
    }

    /**
     * 上传文件
     */
    public FileInfo upload(byte[] file, FileStorageEntity fileStorageEntity) {
        return getFileInfo(fileStorageEntity, fileStorageService.of(file));
    }

    /**
     * 生成上传文件信息
     */
    private FileInfo getFileInfo(FileStorageEntity fileStorageEntity, UploadPretreatment of) {
        UploadPretreatment uploadPretreatment = of
                .setHashCalculatorMd5() //计算 MD5
                .setPath(fileStorageEntity.getPath())
                .setObjectId(fileStorageEntity.getObjectId())
                .setObjectType(fileStorageEntity.getObjectType());
        // 文件名设置
        if (BudgetObjectUtil.isNotEmpty(fileStorageEntity.getOriginalFilename())) {
            uploadPretreatment.setOriginalFilename(fileStorageEntity.getOriginalFilename());
        } else {
            uploadPretreatment.setOriginalFilename(of.getOriginalFilename());
        }
        uploadPretreatment.setSaveFilename(DateTool.dateToString(DateTool.getCurrentDate(), DateStyle.yyyyMMddHHmmssSSS.getValue()) +"_"+of.getOriginalFilename());
        // 缩略图设置
        uploadPretreatment.setSaveThFilename(uploadPretreatment.getSaveFilename());
        uploadPretreatment.setThumbnailSuffix(".jpg");
        //uploadPretreatment.thumbnail(th -> th.size(200,200));
        FileInfo fileInfo = uploadPretreatment
                .upload();
        ExceptionUtil.isNull(fileInfo,"上传失败");
        return fileInfo;
    }

    /**
     * 下载文件
     * @param url url
     * @param response 输出流
     */
    public void download(String url, HttpServletResponse response) throws IOException {
        // 根据url查询origin_name
        FileInfo fileInfo = fileStorageService.getFileInfoByUrl(url);
        ExceptionUtil.isNull(fileInfo,"该文件已被删除!");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode(fileInfo.getOriginalFilename(), "UTF-8").replaceAll("\\+", "%20");
        response.addHeader("content-disposition","attachment;filename="+fileName);
        fileStorageService.download(url)
                .setProgressListener(new ProgressListener() {
                    @Override
                    public void start() {
                        log.info("下载开始");
                    }
                    @Override
                    public void progress(long progressSize,Long allSize) {
                        log.info("已下载 " + progressSize + " 总大小" + allSize);
                    }
                    @Override
                    public void finish() {
                        log.info("下载结束");
                    }
                })
                .setHashCalculatorMd5() //计算 MD5
                .outputStream(response.getOutputStream());
    }

    /**
     * 下载缩略图
     * @param url          url
     * @param response 输出流
     */
    public void downloadTh(String url, HttpServletResponse response) throws IOException {
        fileStorageService.downloadTh(url)
                .setProgressListener(new ProgressListener() {
                    @Override
                    public void start() {
                        log.info("下载开始");
                    }
                    @Override
                    public void progress(long progressSize,Long allSize) {
                        log.info("已下载 " + progressSize + " 总大小" + allSize);
                    }
                    @Override
                    public void finish() {
                        log.info("下载结束");
                    }
                })
                .setHashCalculatorMd5() //计算 MD5
                .setHashCalculatorSha256() //计算 SHA256
                .setHashCalculator(Constant.Hash.MessageDigest.SHA512) //指定哈希名称
                .outputStream(response.getOutputStream());
    }

    /**
     * 初始化任务
     */
    public FileInfo initPart(FileTaskPartEntity fileTaskPartEntity) {
        ExceptionUtil.isNull(fileTaskPartEntity.getLength(),"文件大小不能为空");
        ExceptionUtil.isNull(fileTaskPartEntity.getOriginalFilename(),"文件名不能为空");
        // 判断存储平台是否支持
        FileStorage fileStorage;
        if (BudgetObjectUtil.isNotEmpty(fileTaskPartEntity.getPlatform())) {
            fileStorage = fileStorageService.getFileStorage(fileTaskPartEntity.getPlatform());
        } else {
            fileStorage = fileStorageService.getFileStorage();
        }
        MultipartUploadSupportInfo supportInfo = fileStorageService.isSupportMultipartUpload(fileStorage);
        ExceptionUtil.isTrue(!supportInfo.getIsSupport(), "当前存储平台【"+fileStorage.getPlatform()+"】不支持此功能");
        // 根据md5查询匹配的文件信息
        FileInfo fileInfo = fileDetailMapper.selectByUserMetaMd5(fileTaskPartEntity.getMd5());
        if (fileInfo != null) {
            return fileInfo;
        }
        // 初始化一个任务
       fileInfo = fileStorageService
                .initiateMultipartUpload()
                .setPath(fileTaskPartEntity.getPath())
                .setObjectId(fileTaskPartEntity.getObjectId())
                .setObjectType(fileTaskPartEntity.getObjectType())
                .setOriginalFilename(fileTaskPartEntity.getOriginalFilename())
                .setSaveFilename(DateTool.dateToString(DateTool.getCurrentDate(), DateStyle.yyyyMMddHHmmssSSS.getValue()) +"_"+fileTaskPartEntity.getOriginalFilename())
                .setSize(fileTaskPartEntity.getLength())
                .putUserMetadata("md5", fileTaskPartEntity.getMd5())
                .init();
        log.info("手动分片上传文件初始化成功：{}", fileInfo);
        return fileInfo;
    }

    /**
     * 上传分片
     */
    public boolean uploadPart(FilePartUploadEntity filePartUploadEntity) {
        ExceptionUtil.isNull(filePartUploadEntity.getUrl(),"url不能为空");
        ExceptionUtil.isNull(filePartUploadEntity.getIndex(),"索引不能为空");
        MultipartFile file = filePartUploadEntity.getFile();
        // 获取MultipartFile的字节
        byte[] bytes = null;
        try (InputStream in = file.getInputStream()){
            bytes = IoUtil.readBytes(in);
        } catch (IOException e) {
            log.error("获取文件流失败", e);
            ExceptionUtil.error("获取文件流失败");
        }
        ExceptionUtil.isTrue(bytes == null || bytes.length == 0,"请传入文件流");
        // 验证分片是否上传完成
        boolean flag = checkPart(filePartUploadEntity.getUrl(), filePartUploadEntity.getIndex());
        if (flag) {
            log.info("分片已存在");
            return true;
        }
        FileInfo fileInfo = fileStorageService.getFileInfoByUrl(filePartUploadEntity.getUrl());
        FilePartInfo filePartInfo = fileStorageService
                .uploadPart(fileInfo, filePartUploadEntity.getIndex(), bytes, (long) bytes.length)
                .setHashCalculatorMd5()
                .setHashCalculatorSha256()
                .upload();
        log.info("分片上传成功：{}", filePartInfo);
        return true;
    }

    /**
     * 校验文件分片是否上传完成
     */
    public boolean checkPart(String url, Integer index) {
        // 查询主表信息
        FileInfo fileInfo = fileStorageService.getFileInfoByUrl(url);
        ExceptionUtil.isNull(fileInfo,"该任务不存在,请重新上传!");
        // 判断是否主文件已经上传完成
        if (fileInfo.getUploadStatus() == 2) {
            return true;
        }
        // 获取上传完成的分片信息
        FilePartInfoList partList = fileStorageService.listParts(fileInfo).listParts();
        FilePartInfo first = StreamUtils.findFirst(partList.getList(), info -> info.getPartNumber().equals(index));
        return first != null;
    }

    /**
     * 合并分片
     */
    public Boolean mergePart(String url) {
        // 查询主表信息
        FileInfo fileInfo = fileStorageService.getFileInfoByUrl(url);
        ExceptionUtil.isNull(fileInfo,"该任务不存在,请重新上传!");
        // 判断是否主文件已经上传完成
        if (fileInfo.getUploadStatus() == 2) {
            return true;
        }
        fileStorageService
                .completeMultipartUpload(fileInfo)
                .setProgressListener(new ProgressListener() {
                    @Override
                    public void start() {
                        log.info("文件合并开始");
                    }
                    @Override
                    public void progress(long progressSize, Long allSize) {
                        if (allSize == null) {
                            log.info("文件已合并 " + progressSize + " 总大小未知");
                        } else {
                            log.info("文件已合并 " + progressSize + " 总大小" + allSize + " " + (progressSize * 10000 / allSize * 0.01) + "%");
                        }
                    }
                    @Override
                    public void finish() {
                        log.info("文件合并结束");
                    }
                })
                .complete();
        log.info("合并文件成功：{}", fileInfo);
        return true;
    }

    /**
     * 取消分片
     */
    public Boolean cancelPart(String url) {
        // 查询主表信息
        FileInfo fileInfo = fileStorageService.getFileInfoByUrl(url);
        ExceptionUtil.isNull(fileInfo,"该任务不存在,请重新上传!");
        // 判断是否主文件已经上传完成
        if (fileInfo.getUploadStatus() == 2) {
            ExceptionUtil.error("文件已合并成功,不能取消");
        }
        FilePartInfoList partList = fileStorageService.listParts(fileInfo).listParts();
        for (FilePartInfo info : partList.getList()) {
            log.info("列举已上传的分片：{}", info);
        }
        fileStorageService.abortMultipartUpload(fileInfo).abort();
        try {
            partList = null;
            partList = fileStorageService.listParts(fileInfo).listParts();
        } catch (Exception e) {
        }
        ExceptionUtil.isTrue(BudgetObjectUtil.isNotArray(partList), "手动分片上传文件取消失败！");
        log.info("手动分片上传文件取消成功：{}", fileInfo);
        return true;
    }

    /**
     * 根据url判断文件是否存在
     */
    public boolean exists(String url) {
        return fileStorageService.exists(url);
    }
}