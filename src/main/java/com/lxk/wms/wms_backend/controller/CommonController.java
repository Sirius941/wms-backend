package com.lxk.wms.wms_backend.controller;

import com.lxk.wms.wms_backend.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Tag(name = "通用接口")
@RestController
@RequestMapping("/common")
public class CommonController {

    // 在 application.yml 中配置 file.upload-path: D:/wms-uploads/
    @Value("${file.upload-path}")
    private String uploadPath;

    // 在 application.yml 中配置 server.port 等，或者直接硬编码域名用于返回
    // 这里假设本地开发
    private String domain = "http://localhost:8080";

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 1. 生成文件名
            // 获取原始后缀名，如 .png
            String originalFilename = file.getOriginalFilename();
            String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 使用 UUID 生成新文件名，防止重名
            String fileName = UUID.randomUUID().toString() + suffixName;

            // 2. 按日期分类存储 (可选，为了方便管理)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
            String datePath = sdf.format(new Date());

            // 3. 创建保存文件的目录
            File dest = new File(uploadPath + datePath + fileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }

            // 4. 保存文件
            file.transferTo(dest);

            // 5. 返回可访问的 URL
            // 注意：需要在 WebMvcConfig 中配置资源映射，把 /files/** 映射到 uploadPath
            String fileUrl = domain + "/files/" + datePath + fileName;

            return Result.success(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}