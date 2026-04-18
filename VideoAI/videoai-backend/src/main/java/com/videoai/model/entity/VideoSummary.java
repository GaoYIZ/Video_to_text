package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("video_summary")
@EqualsAndHashCode(callSuper = true)
public class VideoSummary extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long fileId;

    private String title;

    private String summary;

    private String outlineJson;

    private String keywordsJson;

    private String highlightsJson;

    private String qaSuggestionsJson;

    private String modelName;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Long costTimeMs;
}
