package com.videoai.controller;

import com.videoai.common.api.BaseResponse;
import com.videoai.common.api.ResultUtils;
import com.videoai.common.context.LoginUserContext;
import com.videoai.model.annotation.LoginRequired;
import com.videoai.model.dto.workbench.WorkbenchAiActionRequest;
import com.videoai.model.dto.workbench.WorkbenchFileConvertRequest;
import com.videoai.model.dto.workbench.WorkbenchLinkConvertRequest;
import com.videoai.model.vo.WorkbenchAiActionVO;
import com.videoai.model.vo.WorkbenchConvertVO;
import com.videoai.model.vo.WorkbenchMonitorVO;
import com.videoai.model.vo.WorkbenchOverviewVO;
import com.videoai.service.WorkbenchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@LoginRequired
@RequestMapping("/api/workbench")
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    public WorkbenchController(WorkbenchService workbenchService) {
        this.workbenchService = workbenchService;
    }

    @GetMapping("/overview")
    public BaseResponse<WorkbenchOverviewVO> overview() {
        return ResultUtils.success(workbenchService.overview(LoginUserContext.getUserId()));
    }

    @GetMapping("/monitor")
    public BaseResponse<WorkbenchMonitorVO> monitor() {
        return ResultUtils.success(workbenchService.monitor(LoginUserContext.getUserId()));
    }

    @PostMapping("/convert/file")
    public BaseResponse<WorkbenchConvertVO> convertFile(@Valid @RequestBody WorkbenchFileConvertRequest request) {
        return ResultUtils.success(workbenchService.convertFile(LoginUserContext.getUserId(), request));
    }

    @PostMapping("/convert/link")
    public BaseResponse<WorkbenchConvertVO> convertLink(@Valid @RequestBody WorkbenchLinkConvertRequest request) {
        return ResultUtils.success(workbenchService.convertLink(LoginUserContext.getUserId(), request));
    }

    @PostMapping("/ai/action")
    public BaseResponse<WorkbenchAiActionVO> aiAction(@Valid @RequestBody WorkbenchAiActionRequest request) {
        return ResultUtils.success(workbenchService.aiAction(LoginUserContext.getUserId(), request));
    }
}
