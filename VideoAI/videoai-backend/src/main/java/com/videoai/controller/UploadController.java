package com.videoai.controller;

import com.videoai.common.api.BaseResponse;
import com.videoai.common.api.ResultUtils;
import com.videoai.common.context.LoginUserContext;
import com.videoai.model.annotation.LoginRequired;
import com.videoai.model.dto.upload.UploadInitRequest;
import com.videoai.model.dto.upload.UploadMergeRequest;
import com.videoai.model.vo.UploadInitVO;
import com.videoai.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@LoginRequired
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/init")
    public BaseResponse<UploadInitVO> init(@Valid @org.springframework.web.bind.annotation.RequestBody UploadInitRequest request) {
        return ResultUtils.success(uploadService.initUpload(LoginUserContext.getUserId(), request));
    }

    @GetMapping("/chunks")
    public BaseResponse<List<Integer>> chunks(@RequestParam String uploadId) {
        return ResultUtils.success(uploadService.listUploadedChunks(uploadId));
    }

    @PostMapping("/chunk")
    public BaseResponse<Void> uploadChunk(@RequestParam String uploadId,
                                          @RequestParam String fileMd5,
                                          @RequestParam Integer chunkIndex,
                                          @RequestParam MultipartFile chunkFile) {
        uploadService.uploadChunk(LoginUserContext.getUserId(), uploadId, fileMd5, chunkIndex, chunkFile);
        return ResultUtils.success();
    }

    @PostMapping("/merge")
    public BaseResponse<Map<String, Long>> merge(@Valid @org.springframework.web.bind.annotation.RequestBody UploadMergeRequest request) {
        Long fileId = uploadService.mergeChunks(LoginUserContext.getUserId(), request);
        return ResultUtils.success(Map.of("fileId", fileId));
    }
}
