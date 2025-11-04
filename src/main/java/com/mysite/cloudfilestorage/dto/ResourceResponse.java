package com.mysite.cloudfilestorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@Builder
public class ResourceResponse {
    private String path;
    private String name;
    private Long size;
    private ResourceType type;
}
