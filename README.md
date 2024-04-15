# 文件服务
## 优势

一个接口完成各种平台上传下载等功能,支持断点续传,分片,秒传

**不需要后端业务介入原理:**

```
新增业务:
先上传文件获取返回的文件id->业务保存成功后端返回业务主键id->前端保存成功后调用(/XSpring/updateBatchByObjectId
)完成附件绑定业务操作

修改业务:
调用(/XSpring/queryFileDetailByObjectId)获取业务已经绑定的文件集合->
上传文件获取返回的文件id->业务保存成功后端返回业务主键id->前端保存成功后调用(/XSpring/updateBatchByObjectId
)完成附件绑定业务操作

查看业务:
调用(/XSpring/queryFileDetailByObjectId)
```

## 文档介绍

基于X File Storage:https://x-file-storage.xuyanwu.cn

基于X File Storage封装一层通用上传接口,前端只需对接此套服务接口即可

## 接口文档

http://apipost.seaskysh.com/docs/preview/f1fe1216cf4c5961/037d532d87e52590?target_id=ccea78af-7de9-4a57-9583-4ee89c7fc33b

## 服务启动访问地址

![image-20240415150138554](https://wxy-md.oss-cn-shanghai.aliyuncs.com/image-20240415150138554.png)

## 配置

![image-20240415150226003](https://wxy-md.oss-cn-shanghai.aliyuncs.com/image-20240415150226003.png)

## 前端分片代码示例

基于node16和vite完成

![image-20240415150316842](https://wxy-md.oss-cn-shanghai.aliyuncs.com/image-20240415150316842.png)

## json数据查询

SELECT
hash_info->>'$.md5' AS md5
FROM file_detail;





