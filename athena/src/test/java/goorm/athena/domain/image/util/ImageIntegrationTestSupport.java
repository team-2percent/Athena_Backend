package goorm.athena.domain.image.util;

import goorm.athena.domain.image.service.ImageCommandService;
import goorm.athena.domain.image.service.ImageQueryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.util.IntegrationServiceTestSupport;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.nio.file.Path;

public abstract class ImageIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected ImageCommandService imageCommandService;

    @Autowired
    protected ImageGroupCommandService imageGroupCommandService;

    @Autowired
    protected ImageQueryService imageQueryService;

    @TempDir
    Path tempDir;

    @BeforeEach
    protected void setUp(){
        Field imagePathField = ReflectionUtils.findField(ImageCommandService.class, "baseImageUrl");
        imagePathField.setAccessible(true);
        ReflectionUtils.setField(imagePathField, imageCommandService, tempDir.toAbsolutePath().toString());
    }

    protected ImageGroup setupImageGroup(){
        return imageGroupCommandService.createImageGroup(Type.PROJECT);
    }

    protected MultipartFile createMockFile(String filename, String contentType) {
        return new MockMultipartFile(filename, filename, contentType, "dummy_image".getBytes());
    }

}
