package com.seasky.file.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seasky.file.mapper.FileDetailMapper;
import com.seasky.file.po.FileDetail;
import lombok.SneakyThrows;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.hash.HashInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 文件记录表 服务实现类
 * @author wxy
 */
@Service
public class FileDetailService extends ServiceImpl<FileDetailMapper, FileDetail> implements FileRecorder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private IFilePartDetailService filePartDetailService;

    /**
     * 保存文件信息到数据库
     */
    @SneakyThrows
    @Override
    public boolean save(FileInfo info) {
        FileDetail detail = toFileDetail(info);
        boolean b = save(detail);
        if (b) {
            info.setId(detail.getId());
        }
        return b;
    }

    /**
     * 将 FileInfo 转为 FileDetail
     */
    public FileDetail toFileDetail(FileInfo info) throws JsonProcessingException {
        FileDetail detail = BeanUtil.copyProperties(
                info, FileDetail.class, "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo");

        // 这里手动获 元数据 并转成 json 字符串，方便存储在数据库中
        detail.setMetadata(valueToJson(info.getMetadata()));
        detail.setUserMetadata(valueToJson(info.getUserMetadata()));
        detail.setThMetadata(valueToJson(info.getThMetadata()));
        detail.setThUserMetadata(valueToJson(info.getThUserMetadata()));
        // 这里手动获 取附加属性字典 并转成 json 字符串，方便存储在数据库中
        detail.setAttr(valueToJson(info.getAttr()));
        // 这里手动获 哈希信息 并转成 json 字符串，方便存储在数据库中
        detail.setHashInfo(valueToJson(info.getHashInfo()));
        return detail;
    }

    /**
     * 更新文件记录，可以根据文件 ID 或 URL 来更新文件记录，
     * 主要用在手动分片上传文件-完成上传，作用是更新文件信息
     */
    @SneakyThrows
    @Override
    public void update(FileInfo info) {
        FileDetail detail = toFileDetail(info);
        QueryWrapper<FileDetail> qw = new QueryWrapper<FileDetail>()
                .eq(detail.getUrl() != null, FileDetail.COL_URL, detail.getUrl())
                .eq(detail.getId() != null, FileDetail.COL_ID, detail.getId());
        update(detail, qw);
    }

    /**
     * 根据 url 查询文件信息
     */
    @SneakyThrows
    @Override
    public FileInfo getByUrl(String url) {
        return toFileInfo(getOne(new QueryWrapper<FileDetail>().eq(FileDetail.COL_URL, url)));
    }

    /**
     * 将 FileDetail 转为 FileInfo
     */
    public FileInfo toFileInfo(FileDetail detail) throws JsonProcessingException {
        FileInfo info = BeanUtil.copyProperties(
                detail, FileInfo.class, "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo");

        // 这里手动获取数据库中的 json 字符串 并转成 元数据，方便使用
        info.setMetadata(jsonToMetadata(detail.getMetadata()));
        info.setUserMetadata(jsonToMetadata(detail.getUserMetadata()));
        info.setThMetadata(jsonToMetadata(detail.getThMetadata()));
        info.setThUserMetadata(jsonToMetadata(detail.getThUserMetadata()));
        // 这里手动获取数据库中的 json 字符串 并转成 附加属性字典，方便使用
        info.setAttr(jsonToDict(detail.getAttr()));
        // 这里手动获取数据库中的 json 字符串 并转成 哈希信息，方便使用
        info.setHashInfo(jsonToHashInfo(detail.getHashInfo()));
        return info;
    }


    /**
     * 根据 url 删除文件信息
     */
    @Override
    public boolean delete(String url) {
        remove(new QueryWrapper<FileDetail>().eq(FileDetail.COL_URL, url));
        return true;
    }

    /**
     * 保存文件分片信息
     * @param filePartInfo 文件分片信息
     */
    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        filePartDetailService.saveFilePart(filePartInfo);
    }

    /**
     * 删除文件分片信息
     */
    @Override
    public void deleteFilePartByUploadId(String uploadId) {
        filePartDetailService.deleteFilePartByUploadId(uploadId);
    }

    /**
     * 根据关联对象id获取文件集合
     * @param id 对象id
     * @return List<FileDetail> 文件集合
     */
    public List<FileDetail> queryFileDetailByObjectId(Long id) {
        QueryWrapper<FileDetail> fileDetailPoQueryWrapper = new QueryWrapper<>();
        fileDetailPoQueryWrapper.eq(FileDetail.COL_OBJECT_ID,id);
        return baseMapper.selectList(fileDetailPoQueryWrapper);
    }

    /**
     * 将指定值转换成 json 字符串
     */
    public String valueToJson(Object value) throws JsonProcessingException {
        if (value == null) return null;
        return objectMapper.writeValueAsString(value);
    }

    /**
     * 将 json 字符串转换成元数据对象
     */
    public Map<String, String> jsonToMetadata(String json) throws JsonProcessingException {
        if (StrUtil.isBlank(json)) return null;
        return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
    }

    /**
     * 将 json 字符串转换成字典对象
     */
    public Dict jsonToDict(String json) throws JsonProcessingException {
        if (StrUtil.isBlank(json)) return null;
        return objectMapper.readValue(json, Dict.class);
    }

    /**
     * 将 json 字符串转换成哈希信息对象
     */
    public HashInfo jsonToHashInfo(String json) throws JsonProcessingException {
        if (StrUtil.isBlank(json)) return null;
        return objectMapper.readValue(json, HashInfo.class);
    }
}
