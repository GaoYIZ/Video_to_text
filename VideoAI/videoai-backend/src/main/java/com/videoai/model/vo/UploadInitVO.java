package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UploadInitVO {

    private String uploadId;

    private Boolean fastUpload;

    private Long fileId;

    private List<Integer> uploadedChunks;

    private String message;
}
