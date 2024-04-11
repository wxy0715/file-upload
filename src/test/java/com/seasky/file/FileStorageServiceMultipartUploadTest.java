package com.seasky.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.ProgressListener;
import org.dromara.x.file.storage.core.constant.Constant;
import org.dromara.x.file.storage.core.platform.FileStorage;
import org.dromara.x.file.storage.core.platform.MultipartUploadSupportInfo;
import org.dromara.x.file.storage.core.platform.UpyunUssFileStorage;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.dromara.x.file.storage.core.upload.FilePartInfoList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
@SpringBootTest
class FileStorageServiceMultipartUploadTest {

    @Autowired
    private FileStorageService fileStorageService;

    public File getFile() throws IOException {
        String url = "https://app.xuyanwu.cn/BadApple/video/Bad%20Apple.mp4";
        File file = new File(System.getProperty("java.io.tmpdir"), "Bad Apple.mp4");
        System.out.println("测试手动分片上传文件路径：" + file.getAbsolutePath());
        if (!file.exists()) {
            log.info("测试手动分片上传文件不存在，正在下载中");
            FileUtil.writeFromStream(new URL(url).openStream(), file);
            log.info("测试手动分片上传文件下载完成");
        }
        return file;
    }

    /**
     * 测试手动分片上传
     */
    @Test
    public void upload() throws IOException {
        String defaultPlatform = fileStorageService.getDefaultPlatform();
        MultipartUploadSupportInfo supportInfo = fileStorageService.isSupportMultipartUpload(defaultPlatform);
        if (!supportInfo.getIsSupport()) {
            log.info("手动分片上传文件结束，当前存储平台【{}】不支持此功能", defaultPlatform);
            return;
        }
        File file = getFile();
        int partSize = 5 * 1024 * 1024; // 每个分片大小 5MB
        FileInfo fileInfo = fileStorageService
                .initiateMultipartUpload()
                .setPath("/")
                .setOriginalFilename("BadApple5.mp4")
                .setSaveFilename("BadApple5.mp4")
                .setSize(file.length())
                .init();

        log.info("手动分片上传文件初始化成功：{}", fileInfo);

        try (BufferedInputStream in = FileUtil.getInputStream(file)) {
            for (int partNumber = 1; ; partNumber++) {
                byte[] bytes = IoUtil.readBytes(in, partSize); // 每个分片大小
                if (bytes == null || bytes.length == 0) break;
                FilePartInfo filePartInfo = fileStorageService
                        .uploadPart(fileInfo, partNumber, bytes, (long) bytes.length)
                        .setHashCalculatorMd5()
                        .setHashCalculatorSha256()
                        .upload();
                log.info("手动分片上传-分片上传成功：{}", filePartInfo);
            }
        }

//        if (supportInfo.getIsSupportListParts()) {
//            FilePartInfoList partList = fileStorageService.listParts(fileInfo).listParts();
//            for (FilePartInfo info : partList.getList()) {
//                log.info("手动分片上传-列举已上传的分片：{}", info);
//            }
//        } else {
//            log.info("手动分片上传-列举已上传的分片：当前存储平台暂不支持此功能");
//        }
//        fileStorageService
//                .completeMultipartUpload(fileInfo)
//                .setProgressListener(new ProgressListener() {
//                    @Override
//                    public void start() {
//                        System.out.println("文件合并开始");
//                    }
//                    @Override
//                    public void progress(long progressSize, Long allSize) {
//                        if (allSize == null) {
//                            System.out.println("文件已合并 " + progressSize + " 总大小未知");
//                        } else {
//                            System.out.println("文件已合并 " + progressSize + " 总大小" + allSize + " "
//                                    + (progressSize * 10000 / allSize * 0.01) + "%");
//                        }
//                    }
//
//                    @Override
//                    public void finish() {
//                        System.out.println("文件合并结束");
//                    }
//                })
//                .complete();
//        log.info("手动分片上传文件完成成功：{}", fileInfo);
    }

    /**
     * 测试手动分片上传后取消
     */
    @Test
    public void abort() throws IOException {
        String defaultPlatform = fileStorageService.getDefaultPlatform();
        MultipartUploadSupportInfo supportInfo = fileStorageService.isSupportMultipartUpload(defaultPlatform);
        if (!supportInfo.getIsSupportAbort()) {
            log.info("手动分片上传文件结束，当前存储平台【{}】不支持此功能", defaultPlatform);
            return;
        }
        File file = getFile();
        FileInfo fileInfo = fileStorageService
                .initiateMultipartUpload()
                .setPath("/test")
                .setSaveFilename("BadApple.mp4")
                .init();
        log.info("手动分片上传文件初始化成功：{}", fileInfo);
        try (BufferedInputStream in = FileUtil.getInputStream(file)) {
            for (int partNumber = 1; ; partNumber++) {
                byte[] bytes = IoUtil.readBytes(in, 5 * 1024 * 1024); // 每个分片大小 5MB
                if (bytes == null || bytes.length == 0) break;
                System.out.println("分片 " + partNumber + " 上传开始");
                fileStorageService
                        .uploadPart(fileInfo, partNumber, bytes, (long) bytes.length)
                        .upload();
                System.out.println("分片 " + partNumber + " 上传完成");
            }
        }
        FilePartInfoList partList = fileStorageService.listParts(fileInfo).listParts();
        for (FilePartInfo info : partList.getList()) {
            log.info("手动分片上传-列举已上传的分片：{}", info);
        }
        fileStorageService.abortMultipartUpload(fileInfo).abort();
        log.info("手动分片上传文件已取消，正在验证：{}", fileInfo);
        try {
            partList = null;
            partList = fileStorageService.listParts(fileInfo).listParts();
        } catch (Exception e) {
        }
        Assert.isNull(partList, "手动分片上传文件取消失败！");
        log.info("手动分片上传文件取消成功：{}", fileInfo);
    }
}
