package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("media_file")
@EqualsAndHashCode(callSuper = true)
public class MediaFile extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileNo;

    private Long userId;

    private String fileName;

    private String fileMd5;

    private Long fileSize;

    private String fileExt;

    private String mimeType;

    private String bucketName;

    private String objectName;

    private String storageType;

    private Integer uploadStatus;

    private Integer refCount;
}
