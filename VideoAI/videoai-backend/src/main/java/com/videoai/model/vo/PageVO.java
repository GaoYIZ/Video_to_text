package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageVO<T> {

    private Long total;

    private Long current;

    private Long pageSize;

    private List<T> records;
}
