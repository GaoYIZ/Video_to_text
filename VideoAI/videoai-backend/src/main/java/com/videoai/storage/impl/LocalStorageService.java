package com.videoai.storage.impl;

import com.videoai.common.exception.BusinessException;
import com.videoai.config.VideoAiProperties;
import com.videoai.model.entity.MediaFile;
import com.videoai.storage.StorageObject;
import com.videoai.storage.StorageService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@Primary
public class LocalStorageService implements StorageService {

    private final VideoAiProperties videoAiProperties;

    public LocalStorageService(VideoAiProperties videoAiProperties) {
        this.videoAiProperties = videoAiProperties;
    }

    @Override
    public StorageObject upload(File file, String objectName, String contentType) {
        try {
            Path target = Path.of(videoAiProperties.getStorage().getLocalBasePath(), objectName);
            Files.createDirectories(target.getParent());
            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return StorageObject.builder()
                    .bucketName("local")
                    .objectName(objectName)
                    .storageType("LOCAL")
                    .size(file.length())
                    .build();
        } catch (IOException e) {
            throw new BusinessException("本地存储文件失败");
        }
    }

    @Override
    public File downloadToLocal(MediaFile mediaFile) {
        Path file = Path.of(videoAiProperties.getStorage().getLocalBasePath(), mediaFile.getObjectName());
        return file.toFile();
    }
}
