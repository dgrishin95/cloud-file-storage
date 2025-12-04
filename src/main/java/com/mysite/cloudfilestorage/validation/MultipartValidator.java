package com.mysite.cloudfilestorage.validation;

import com.mysite.cloudfilestorage.exception.minio.InvalidRequestBodyException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class MultipartValidator {

    private final PathValidator pathValidator;

    public void validateUploadedResource(List<MultipartFile> resource) {
        if (resource == null || resource.isEmpty()) {
            throw new InvalidRequestBodyException("Invalid request body");
        }

        validateUploadedFiles(resource);
    }

    private void validateUploadedFiles(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file.getSize() == 0 || pathValidator.isInvalidPathComponent(file.getOriginalFilename())) {
                throw new InvalidRequestBodyException("Invalid request body");
            }
        }
    }

    public InputStream validateInputStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (Exception exception) {
            throw new InvalidRequestBodyException("Invalid request body");
        }
    }
}
