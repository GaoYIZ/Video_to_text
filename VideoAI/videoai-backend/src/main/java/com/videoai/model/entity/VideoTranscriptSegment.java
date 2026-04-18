package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("video_transcript_segment")
@EqualsAndHashCode(callSuper = true)
public class VideoTranscriptSegment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long transcriptId;

    private Integer segmentIndex;

    private Long startTimeMs;

    private Long endTimeMs;

    private String content;

    private String keywords;

    private Integer tokenCount;
}
