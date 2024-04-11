package com.seasky.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seasky.file.po.FileDetail;
import org.dromara.x.file.storage.core.FileInfo;

public interface FileDetailMapper extends BaseMapper<FileDetail> {
    /**
     * 根据前端自定义md5查询文件
     */
    FileInfo selectByUserMetaMd5(String md5);
}
