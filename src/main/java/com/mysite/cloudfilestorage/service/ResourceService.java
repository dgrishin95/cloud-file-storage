package com.mysite.cloudfilestorage.service;

import com.mysite.cloudfilestorage.dto.DownloadResult;
import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.service.resource.download.ResourceDownloadService;
import com.mysite.cloudfilestorage.service.resource.move.ResourceModificationService;
import com.mysite.cloudfilestorage.service.resource.query.ResourceQueryService;
import com.mysite.cloudfilestorage.service.resource.upload.ResourceUploadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceQueryService queryService;
    private final ResourceModificationService modificationService;
    private final ResourceDownloadService downloadService;
    private final ResourceUploadService uploadService;

    public ResourceResponse getResource(String path) throws Exception {
        return queryService.getResource(path);
    }

    public void removeResource(String path) throws Exception {
        modificationService.removeResource(path);
    }

    public DownloadResult downloadResource(String path) throws Exception {
        return downloadService.downloadResource(path);
    }

    public ResourceResponse moveResource(String from, String to) throws Exception {
        return modificationService.moveResource(from, to);
    }

    public List<ResourceResponse> searchResource(String query) throws Exception {
        return queryService.searchResource(query);
    }

    public List<ResourceResponse> uploadResource(String path, List<MultipartFile> resource) throws Exception {
        return uploadService.uploadResource(path, resource);
    }
}
