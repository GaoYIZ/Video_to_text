package com.videoai.storage.impl;

import com.videoai.common.exception.BusinessException;
import com.videoai.config.MinioProperties;
import com.videoai.model.entity.MediaFile;
import com.videoai.storage.StorageObject;
import com.videoai.storage.StorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

@Service
@ConditionalOnProperty(prefix = "videoai.minio", name = "enabled", havingValue = "true")
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioStorageService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @Override
    public StorageObject upload(File file, String objectName, String contentType) {
        try {
            ensureBucket();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .stream(Files.newInputStream(file.toPath()), file.length(), -1)
                            .contentType(contentType)
                            .build()
            );
            return StorageObject.builder()
                    .bucketName(minioProperties.getBucket())
                    .objectName(objectName)
                    .storageType("MINIO")
                    .size(file.length())
                    .build();
        } catch (Exception e) {
            throw new BusinessException("上传 MinIO 失败");
        }
    }

    @Override
    public File downloadToLocal(MediaFile mediaFile) {
        File tempFile = new File(System.getProperty("java.io.tmpdir"), mediaFile.getObjectName().replace("/", "_"));
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(mediaFile.getBucketName()).object(mediaFile.getObjectName()).build());
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            inputStream.transferTo(outputStream);
            return tempFile;
        } catch (Exception e) {
            throw new BusinessException("下载 MinIO 文件失败");
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
        }
    }
}
