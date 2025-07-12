package cl.metspherical.calbucofelizbackend.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryUploadServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private com.cloudinary.Uploader uploader;

    @Mock
    private com.cloudinary.Url url;

    @InjectMocks
    private CloudinaryUploadService cloudinaryUploadService;

    private byte[] testImageBytes;
    private Map<String, Object> mockUploadResult;

    @BeforeEach
    void setUp() {
        testImageBytes = "test image data".getBytes();
        mockUploadResult = new HashMap<>();
        mockUploadResult.put("public_id", "test_public_id");
        mockUploadResult.put("url", "https://res.cloudinary.com/test/image/upload/test_public_id.jpg");

        // Set up the mocked Cloudinary instance
        ReflectionTestUtils.setField(cloudinaryUploadService, "cloudinary", cloudinary);
    }

    @Test
    void shouldUploadImageSuccessfully() throws IOException {
        // Given
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockUploadResult);
        when(cloudinary.url()).thenReturn(url);
        when(url.generate("test_public_id")).thenReturn("https://res.cloudinary.com/test/image/upload/test_public_id.jpg");

        // When
        String result = cloudinaryUploadService.uploadImage(testImageBytes);

        // Then
        assertThat(result).isEqualTo("https://res.cloudinary.com/test/image/upload/test_public_id.jpg");
        verify(cloudinary).uploader();
        verify(uploader).upload(testImageBytes, ObjectUtils.asMap("resource_type", "image"));
        verify(cloudinary).url();
        verify(url).generate("test_public_id");
    }

    @Test
    void shouldThrowExceptionWhenUploadFails() {
        // Given
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new RuntimeException("Upload failed"));

        // When & Then
        assertThatThrownBy(() -> cloudinaryUploadService.uploadImage(testImageBytes))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Error uploading image to Cloudinary");
    }

    @Test
    void shouldThrowExceptionWhenPublicIdIsNull() {
        // Given
        Map<String, Object> resultWithoutPublicId = new HashMap<>();
        resultWithoutPublicId.put("url", "https://res.cloudinary.com/test/image/upload/test.jpg");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(resultWithoutPublicId);

        // When & Then
        assertThatThrownBy(() -> cloudinaryUploadService.uploadImage(testImageBytes))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to get public ID from Cloudinary upload result");
    }
}