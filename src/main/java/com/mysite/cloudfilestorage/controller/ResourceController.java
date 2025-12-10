package com.mysite.cloudfilestorage.controller;

import com.mysite.cloudfilestorage.dto.DownloadResult;
import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.service.ResourceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResourceResponse getResource(@RequestParam(name = "path", defaultValue = "") String path) throws Exception {
        return resourceService.getResource(path);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeResource(@RequestParam(name = "path", defaultValue = "") String path) throws Exception {
        resourceService.removeResource(path);
    }

    @GetMapping("/download")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InputStreamResource> downloadResource(@RequestParam(name = "path", defaultValue = "") String path)
            throws Exception {
        DownloadResult downloadResult = resourceService.downloadResource(path);

        String headerValue = "attachment; filename=\"" + downloadResult.fileName() + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(downloadResult.inputStream()));
    }

    @GetMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    public ResourceResponse moveResource(@RequestParam("from") String from,
                                         @RequestParam(name = "to", defaultValue = "") String to)
            throws Exception {
        return resourceService.moveResource(from, to);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ResourceResponse> searchResource(@RequestParam("query") String query) throws Exception {
        return resourceService.searchResource(query);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceResponse> uploadResource(@RequestParam("path") String path,
                                                 @RequestParam("resource") List<MultipartFile> resource) throws Exception {
        return resourceService.uploadResource(path, resource);
    }
}
