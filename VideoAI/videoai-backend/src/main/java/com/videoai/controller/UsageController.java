package com.videoai.controller;

import com.videoai.common.api.BaseResponse;
import com.videoai.common.api.ResultUtils;
import com.videoai.common.context.LoginUserContext;
import com.videoai.model.annotation.LoginRequired;
import com.videoai.model.vo.UsageOverviewVO;
import com.videoai.model.vo.UserQuotaVO;
import com.videoai.model.vo.UsageStatsVO;
import com.videoai.service.UsageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@LoginRequired
@RequestMapping("/api/usage")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/me")
    public BaseResponse<UsageOverviewVO> me() {
        return ResultUtils.success(usageService.overview(LoginUserContext.getUserId()));
    }

    @GetMapping("/quota")
    public BaseResponse<UserQuotaVO> quota() {
        return ResultUtils.success(usageService.getUserQuota(LoginUserContext.getUserId()));
    }

    @GetMapping("/stats")
    public BaseResponse<UsageStatsVO> stats(@RequestParam(defaultValue = "day") String period) {
        return ResultUtils.success(usageService.getUsageStats(LoginUserContext.getUserId(), period));
    }

    @GetMapping("/daily")
    public BaseResponse<UsageStatsVO> daily() {
        return ResultUtils.success(usageService.getDailyUsage(LoginUserContext.getUserId()));
    }

    @GetMapping("/monthly")
    public BaseResponse<UsageStatsVO> monthly() {
        return ResultUtils.success(usageService.getMonthlyUsage(LoginUserContext.getUserId()));
    }
}
