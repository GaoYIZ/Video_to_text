package com.videoai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.videoai.model.entity.VideoSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VideoSummaryMapper extends BaseMapper<VideoSummary> {
}
