package com.mysite.cloudfilestorage.controller;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.service.ResourceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final ResourceService resourceService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ResourceResponse> getResource(@RequestParam(name = "path", defaultValue = "") String path) throws Exception {
        return resourceService.getResourceForDirectory(path);
    }
}
