package com.mysite.cloudfilestorage.dto;

import java.io.InputStream;

public record UploadResourceData(String key, InputStream inputStream, String folderPath, String name, Long size) {
}
