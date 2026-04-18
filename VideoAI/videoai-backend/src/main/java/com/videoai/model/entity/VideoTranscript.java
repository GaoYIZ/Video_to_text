package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("video_transcript")
@EqualsAndHashCode(callSuper = true)
public class VideoTranscript extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long userId;

    private Long fileId;

    private String language;

    private Long durationMs;

    private Integer wordCount;

    private String transcriptText;

    private String segmentJson;

    private Integer status;
}
