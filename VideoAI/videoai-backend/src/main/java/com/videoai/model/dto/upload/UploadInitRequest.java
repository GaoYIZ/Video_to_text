package com.videoai.model.dto.upload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadInitRequest {

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @NotBlank(message = "文件 md5 不能为空")
    private String fileMd5;

    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于 0")
    private Long fileSize;

    @NotNull(message = "分片大小不能为空")
    @Min(value = 1, message = "分片大小必须大于 0")
    private Integer chunkSize;

    @NotNull(message = "分片总数不能为空")
    @Min(value = 1, message = "分片总数必须大于 0")
    private Integer totalChunks;

    private String mimeType;
}
