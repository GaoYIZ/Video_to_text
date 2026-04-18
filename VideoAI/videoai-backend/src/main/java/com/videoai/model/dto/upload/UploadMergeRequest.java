package com.videoai.model.dto.upload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadMergeRequest {

    @NotBlank(message = "uploadId 不能为空")
    private String uploadId;

    @NotBlank(message = "文件 md5 不能为空")
    private String fileMd5;

    @NotBlank(message = "文件名不能为空")
    private String fileName;
}
