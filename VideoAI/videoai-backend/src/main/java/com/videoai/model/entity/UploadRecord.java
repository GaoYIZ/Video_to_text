package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("upload_record")
@EqualsAndHashCode(callSuper = true)
public class UploadRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String uploadId;

    private Long userId;

    private String fileName;

    private String fileMd5;

    private Long fileSize;

    private Integer chunkSize;

    private Integer totalChunks;

    private String uploadedChunksJson;

    private Integer status;

    private Long mediaFileId;
}
