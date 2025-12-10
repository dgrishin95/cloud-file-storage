package com.mysite.cloudfilestorage.service.resource.move;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceModificationService {

    private final ResourceRemoveService removeService;
    private final ResourceMoveService moveService;

    public void removeResource(String path) throws Exception {
        removeService.removeResource(path);
    }

    public ResourceResponse moveResource(String from, String to) throws Exception {
        return moveService.moveResource(from, to);
    }
}
