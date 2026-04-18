package com.videoai.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.videoai.common.constant.RedisKeyConstants;
import com.videoai.common.enums.ErrorCode;
import com.videoai.common.exception.BusinessException;
import com.videoai.common.util.IdUtils;
import com.videoai.common.util.JsonUtils;
import com.videoai.mapper.MediaFileMapper;
import com.videoai.mapper.UploadRecordMapper;
import com.videoai.model.dto.upload.UploadInitRequest;
import com.videoai.model.dto.upload.UploadMergeRequest;
import com.videoai.model.entity.MediaFile;
import com.videoai.model.entity.UploadRecord;
import com.videoai.model.enums.UploadStatusEnum;
import com.videoai.model.vo.UploadInitVO;
import com.videoai.service.UploadService;
import com.videoai.storage.StorageObject;
import com.videoai.storage.StorageService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Service
public class UploadServiceImpl implements UploadService {

    private final UploadRecordMapper uploadRecordMapper;
    private final MediaFileMapper mediaFileMapper;
    private final StorageService storageService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    public UploadServiceImpl(UploadRecordMapper uploadRecordMapper, MediaFileMapper mediaFileMapper,
                             StorageService storageService, RedisTemplate<String, Object> redisTemplate,
                             RedissonClient redissonClient) {
        this.uploadRecordMapper = uploadRecordMapper;
        this.mediaFileMapper = mediaFileMapper;
        this.storageService = storageService;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadInitVO initUpload(Long userId, UploadInitRequest request) {
        MediaFile mediaFile = mediaFileMapper.selectOne(Wrappers.<MediaFile>lambdaQuery()
                .eq(MediaFile::getFileMd5, request.getFileMd5()));
        if (mediaFile != null) {
            UploadRecord uploadRecord = new UploadRecord();
            uploadRecord.setUploadId(IdUtils.uploadId());
            uploadRecord.setUserId(userId);
            uploadRecord.setFileName(request.getFileName());
            uploadRecord.setFileMd5(request.getFileMd5());
            uploadRecord.setFileSize(request.getFileSize());
            uploadRecord.setChunkSize(request.getChunkSize());
            uploadRecord.setTotalChunks(request.getTotalChunks());
            uploadRecord.setStatus(UploadStatusEnum.FAST_HIT.getCode());
            uploadRecord.setMediaFileId(mediaFile.getId());
            uploadRecord.setUploadedChunksJson(JsonUtils.toJson(buildAllChunks(request.getTotalChunks())));
            uploadRecordMapper.insert(uploadRecord);
            return UploadInitVO.builder()
                    .uploadId(uploadRecord.getUploadId())
                    .fastUpload(true)
                    .fileId(mediaFile.getId())
                    .uploadedChunks(buildAllChunks(request.getTotalChunks()))
                    .message("文件已存在，秒传成功")
                    .build();
        }

        UploadRecord uploadRecord = uploadRecordMapper.selectOne(Wrappers.<UploadRecord>lambdaQuery()
                .eq(UploadRecord::getUserId, userId)
                .eq(UploadRecord::getFileMd5, request.getFileMd5())
                .in(UploadRecord::getStatus, UploadStatusEnum.INIT.getCode(), UploadStatusEnum.UPLOADING.getCode()));
        if (uploadRecord == null) {
            uploadRecord = new UploadRecord();
            uploadRecord.setUploadId(IdUtils.uploadId());
            uploadRecord.setUserId(userId);
            uploadRecord.setFileName(request.getFileName());
            uploadRecord.setFileMd5(request.getFileMd5());
            uploadRecord.setFileSize(request.getFileSize());
            uploadRecord.setChunkSize(request.getChunkSize());
            uploadRecord.setTotalChunks(request.getTotalChunks());
            uploadRecord.setStatus(UploadStatusEnum.INIT.getCode());
            uploadRecord.setUploadedChunksJson("[]");
            uploadRecordMapper.insert(uploadRecord);
        }
        return UploadInitVO.builder()
                .uploadId(uploadRecord.getUploadId())
                .fastUpload(false)
                .uploadedChunks(listUploadedChunks(uploadRecord.getUploadId()))
                .message("初始化成功")
                .build();
    }

    @Override
    public List<Integer> listUploadedChunks(String uploadId) {
        String key = RedisKeyConstants.UPLOAD_CHUNK.formatted(uploadId);
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members != null && !members.isEmpty()) {
            return members.stream()
                    .map(item -> Integer.parseInt(String.valueOf(item)))
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }
        UploadRecord uploadRecord = getByUploadId(uploadId);
        return JsonUtils.toList(uploadRecord.getUploadedChunksJson(), Integer.class);
    }

    @Override
    public void uploadChunk(Long userId, String uploadId, String fileMd5, Integer chunkIndex, MultipartFile chunkFile) {
        UploadRecord uploadRecord = getByUploadId(uploadId);
        if (!Objects.equals(uploadRecord.getUserId(), userId) || !Objects.equals(uploadRecord.getFileMd5(), fileMd5)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传参数不匹配");
        }
        try {
            Path dir = Path.of("data", "temp", "chunks", uploadId);
            Files.createDirectories(dir);
            Path chunkPath = dir.resolve(chunkIndex + ".part");
            chunkFile.transferTo(chunkPath);
            String key = RedisKeyConstants.UPLOAD_CHUNK.formatted(uploadId);
            redisTemplate.opsForSet().add(key, chunkIndex);
            redisTemplate.expire(key, Duration.ofHours(24));
            redisTemplate.opsForValue().set(RedisKeyConstants.UPLOAD_PROGRESS.formatted(fileMd5),
                    listUploadedChunks(uploadId).size() + "/" + uploadRecord.getTotalChunks(),
                    Duration.ofHours(24));
            uploadRecord.setStatus(UploadStatusEnum.UPLOADING.getCode());
            uploadRecord.setUploadedChunksJson(JsonUtils.toJson(listUploadedChunks(uploadId)));
            uploadRecordMapper.updateById(uploadRecord);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "分片上传失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long mergeChunks(Long userId, UploadMergeRequest request) {
        UploadRecord uploadRecord = getByUploadId(request.getUploadId());
        if (!Objects.equals(uploadRecord.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        List<Integer> uploadedChunks = listUploadedChunks(request.getUploadId());
        if (uploadedChunks.size() < uploadRecord.getTotalChunks()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仍有分片未上传完成");
        }
        String lockKey = RedisKeyConstants.VIDEO_FILE_LOCK.formatted(request.getFileMd5());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            MediaFile exists = mediaFileMapper.selectOne(Wrappers.<MediaFile>lambdaQuery()
                    .eq(MediaFile::getFileMd5, request.getFileMd5()));
            if (exists != null) {
                uploadRecord.setStatus(UploadStatusEnum.FAST_HIT.getCode());
                uploadRecord.setMediaFileId(exists.getId());
                uploadRecord.setUploadedChunksJson(JsonUtils.toJson(uploadedChunks));
                uploadRecordMapper.updateById(uploadRecord);
                return exists.getId();
            }
            File mergedFile = mergeChunkFiles(uploadRecord, request.getFileName());
            String ext = FileUtil.extName(request.getFileName());
            String objectName = "video/" + request.getFileMd5() + (StrUtil.isBlank(ext) ? "" : "." + ext);
            StorageObject storageObject = storageService.upload(mergedFile, objectName, "application/octet-stream");
            MediaFile mediaFile = new MediaFile();
            mediaFile.setFileNo(IdUtils.fileNo());
            mediaFile.setUserId(userId);
            mediaFile.setFileName(request.getFileName());
            mediaFile.setFileMd5(request.getFileMd5());
            mediaFile.setFileSize(uploadRecord.getFileSize());
            mediaFile.setFileExt(ext);
            mediaFile.setMimeType("application/octet-stream");
            mediaFile.setBucketName(storageObject.getBucketName());
            mediaFile.setObjectName(storageObject.getObjectName());
            mediaFile.setStorageType(storageObject.getStorageType());
            mediaFile.setUploadStatus(UploadStatusEnum.MERGED.getCode());
            mediaFile.setRefCount(1);
            mediaFileMapper.insert(mediaFile);

            uploadRecord.setStatus(UploadStatusEnum.MERGED.getCode());
            uploadRecord.setMediaFileId(mediaFile.getId());
            uploadRecord.setUploadedChunksJson(JsonUtils.toJson(uploadedChunks));
            uploadRecordMapper.updateById(uploadRecord);
            return mediaFile.getId();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public UploadRecord getByUploadId(String uploadId) {
        UploadRecord uploadRecord = uploadRecordMapper.selectOne(Wrappers.<UploadRecord>lambdaQuery()
                .eq(UploadRecord::getUploadId, uploadId));
        if (uploadRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "上传记录不存在");
        }
        return uploadRecord;
    }

    private File mergeChunkFiles(UploadRecord uploadRecord, String fileName) {
        try {
            Path dir = Path.of("data", "temp", "chunks", uploadRecord.getUploadId());
            List<File> parts = Files.list(dir)
                    .map(Path::toFile)
                    .sorted(Comparator.comparingInt(file -> Integer.parseInt(file.getName().replace(".part", ""))))
                    .toList();
            File mergedFile = Path.of("data", "temp", "merged", uploadRecord.getUploadId() + "-" + fileName).toFile();
            File parent = mergedFile.getParentFile();
            if (parent != null && !parent.exists()) {
                FileUtil.mkdir(parent);
            }
            try (FileOutputStream outputStream = new FileOutputStream(mergedFile)) {
                for (File part : parts) {
                    try (FileInputStream inputStream = new FileInputStream(part)) {
                        inputStream.transferTo(outputStream);
                    }
                }
            }
            return mergedFile;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "分片合并失败");
        }
    }

    private List<Integer> buildAllChunks(Integer totalChunks) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < totalChunks; i++) {
            result.add(i);
        }
        return result;
    }
}
