package com.mysite.cloudfilestorage.dto;

import java.io.InputStream;

public record DownloadResult(String fileName, InputStream inputStream) {
}
