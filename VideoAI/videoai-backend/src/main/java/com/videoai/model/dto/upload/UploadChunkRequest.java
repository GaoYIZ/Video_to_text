package com.videoai.model.dto.upload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadChunkRequest {

    @NotBlank(message = "uploadId 不能为空")
    private String uploadId;

    @NotBlank(message = "文件 md5 不能为空")
    private String fileMd5;

    @NotNull(message = "分片序号不能为空")
    @Min(value = 0, message = "分片序号非法")
    private Integer chunkIndex;

    @NotNull(message = "分片文件不能为空")
    private MultipartFile chunkFile;
}
