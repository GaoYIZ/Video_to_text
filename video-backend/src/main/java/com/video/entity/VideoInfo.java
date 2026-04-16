package com.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("video_info")
public class VideoInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String originalName;
    private String videoPath;
    private String audioPath;
    private String transcript;
    private String summary;
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
