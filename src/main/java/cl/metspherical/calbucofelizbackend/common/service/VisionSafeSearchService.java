package cl.metspherical.calbucofelizbackend.common.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Service
public class VisionSafeSearchService {

    private final ResourceLoader resourceLoader;
    private final String googleCredentialsPath;

    public VisionSafeSearchService(
            @Value("${google.vision.credentials-path}") String googleCredentialsPath,
            ResourceLoader resourceLoader
    ) {
        this.resourceLoader = resourceLoader;
        this.googleCredentialsPath = googleCredentialsPath;
    }

    /**
     * Analyzes an image using Google Cloud Vision API to verify if it contains inappropriate content.
     * Loads credentials from the configured JSON file.
     * 
     * @param file MultipartFile (uploaded image)
     * @return true if the image is safe, false if it contains sensitive content
     */
    public boolean isImageSafe(MultipartFile file) throws IOException {
        // Load credentials from the JSON file
        Resource credentialsResource = resourceLoader.getResource(googleCredentialsPath);
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsResource.getInputStream())
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
        
        // Create Vision API client with credentials
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings)) {
            ByteString imgBytes = ByteString.readFrom(file.getInputStream());
            Image img = Image.newBuilder().setContent(imgBytes).build();

            Feature feature = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(img)
                    .build();

            SafeSearchAnnotation annotation = vision.batchAnnotateImages(List.of(request))
                    .getResponses(0)
                    .getSafeSearchAnnotation();

            return Stream.of(
                    annotation.getAdult(),
                    annotation.getViolence(),
                    annotation.getRacy(),
                    annotation.getMedical()
            ).allMatch(likelihood -> likelihood != Likelihood.LIKELY && likelihood != Likelihood.VERY_LIKELY);
        }
    }

    /**
     * Validates multiple images for inappropriate content
     * 
     * @param images List of images to validate
     * @throws ResponseStatusException if any image contains inappropriate content or validation fails
     */
    public void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        if (images.size() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 10 images allowed");
        }

        for (MultipartFile image : images) {
            try {
                if (!isImageSafe(image)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Image contains inappropriate content and cannot be uploaded");
                }
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error validating image: " + e.getMessage());
            }
        }
    }
}