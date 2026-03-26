package com.example.city_noise_system.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class FileUploadUtil {

    // 上传目录
    private String UPLOAD_DIR;
    
    // 构造函数，初始化上传目录为绝对路径
    public FileUploadUtil() {
        // 获取应用运行目录的绝对路径
        this.UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
        System.out.println("文件上传目录: " + UPLOAD_DIR);
    }

    // 允许的文件类型
    private static final String[] ALLOWED_EXTENSIONS = { ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp" };

    public String uploadImage(MultipartFile file) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        boolean isAllowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("不支持的文件类型");
        }

        // 创建上传目录
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                throw new IOException("无法创建上传目录");
            }
        }

        // 生成唯一文件名
        String filename = generateFilename(extension);

        // 保存文件
        Path filePath = Paths.get(UPLOAD_DIR, filename);
        Files.copy(file.getInputStream(), filePath);

        return "/uploads/" + filename;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String generateFilename(String extension) {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String timeStr = new SimpleDateFormat("HHmmss").format(new Date());
        String randomStr = UUID.randomUUID().toString().substring(0, 8);
        return dateStr + "_" + timeStr + "_" + randomStr + extension;
    }
}