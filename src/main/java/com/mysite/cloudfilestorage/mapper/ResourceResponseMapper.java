package com.mysite.cloudfilestorage.mapper;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.mapper.config.DefaultMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMapperConfig.class)
public interface ResourceResponseMapper {

    @Mapping(target = "path", expression = "java(com.mysite.cloudfilestorage.util.PathUtil.getParentPathOfDirectory(objectName))")
    @Mapping(target = "name", expression = "java(com.mysite.cloudfilestorage.util.PathUtil.getDirectoryName(objectName))")
    @Mapping(target = "type", constant = "DIRECTORY")
    ResourceResponse toDirectoryResourceResponse(String objectName);

    @Mapping(source = "folderPath", target = "path")
    @Mapping(target = "type", constant = "FILE")
    ResourceResponse toFileResourceResponse(String folderPath, String name, Long size);
}
