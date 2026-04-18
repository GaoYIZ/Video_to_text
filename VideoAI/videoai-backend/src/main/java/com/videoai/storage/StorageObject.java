package com.videoai.storage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageObject {

    private String bucketName;

    private String objectName;

    private String storageType;

    private Long size;
}
