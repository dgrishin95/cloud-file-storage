package com.mysite.cloudfilestorage.controller;

import com.mysite.cloudfilestorage.dto.DownloadResult;
import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResourceResponse getResource(@RequestParam("path") String path) throws Exception {
        return resourceService.getResource(path);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeResource(@RequestParam("path") String path) throws Exception {
        resourceService.removeResource(path);
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadResource(@RequestParam("path") String path) throws Exception {
        DownloadResult downloadResult = resourceService.downloadResource(path);

        String headerValue = "attachment; filename=\"" + downloadResult.fileName() + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(downloadResult.inputStream()));
    }

    @GetMapping("/move")
    public ResourceResponse moveResource(@RequestParam("from") String from, @RequestParam("to") String to) throws Exception {
        return resourceService.moveResource(from, to);
    }
}
