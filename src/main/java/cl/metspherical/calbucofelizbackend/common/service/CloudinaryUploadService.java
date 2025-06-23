package cl.metspherical.calbucofelizbackend.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Service responsible for uploading images to Cloudinary
 */
@Service
public class CloudinaryUploadService {

    private static final String IMAGE_RESOURCE_TYPE = "image";

    private final Cloudinary cloudinary;

    public CloudinaryUploadService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    /**
     * Uploads an image to Cloudinary and returns the URL
     * 
     * @param imageBytes The image bytes to upload
     * @return The URL of the uploaded image
     * @throws IOException if there's an error during upload
     */
    public String uploadImage(byte[] imageBytes) throws IOException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap(
                    "resource_type", IMAGE_RESOURCE_TYPE
            ));

            String publicId = (String) uploadResult.get("public_id");
            if (publicId == null) {
                throw new IOException("Failed to get public ID from Cloudinary upload result");
            }

            return cloudinary.url().generate(publicId);
        } catch (Exception e) {
            throw new IOException("Error uploading image to Cloudinary", e);
        }
    }
}
