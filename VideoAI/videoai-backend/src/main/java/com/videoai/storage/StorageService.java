package com.videoai.storage;

import com.videoai.model.entity.MediaFile;

import java.io.File;

public interface StorageService {

    StorageObject upload(File file, String objectName, String contentType);

    File downloadToLocal(MediaFile mediaFile);
}
