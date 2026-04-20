package com.videoai.service;

import com.videoai.model.dto.workbench.WorkbenchAiActionRequest;
import com.videoai.model.dto.workbench.WorkbenchFileConvertRequest;
import com.videoai.model.dto.workbench.WorkbenchLinkConvertRequest;
import com.videoai.model.vo.WorkbenchAiActionVO;
import com.videoai.model.vo.WorkbenchConvertVO;
import com.videoai.model.vo.WorkbenchMonitorVO;
import com.videoai.model.vo.WorkbenchOverviewVO;

public interface WorkbenchService {

    WorkbenchOverviewVO overview(Long userId);

    WorkbenchMonitorVO monitor(Long userId);

    WorkbenchConvertVO convertFile(Long userId, WorkbenchFileConvertRequest request);

    WorkbenchConvertVO convertLink(Long userId, WorkbenchLinkConvertRequest request);

    WorkbenchAiActionVO aiAction(Long userId, WorkbenchAiActionRequest request);
}
