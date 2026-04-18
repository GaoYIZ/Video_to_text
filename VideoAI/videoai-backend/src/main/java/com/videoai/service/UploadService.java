package com.videoai.service;

import com.videoai.model.dto.upload.UploadInitRequest;
import com.videoai.model.dto.upload.UploadMergeRequest;
import com.videoai.model.entity.UploadRecord;
import com.videoai.model.vo.UploadInitVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadService {

    UploadInitVO initUpload(Long userId, UploadInitRequest request);

    List<Integer> listUploadedChunks(String uploadId);

    void uploadChunk(Long userId, String uploadId, String fileMd5, Integer chunkIndex, MultipartFile chunkFile);

    Long mergeChunks(Long userId, UploadMergeRequest request);

    UploadRecord getByUploadId(String uploadId);
}
