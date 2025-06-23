package cl.metspherical.calbucofelizbackend.common.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for image compression and format conversion
 */
@Service
public class ImageCompressionService {

    private static final float DEFAULT_COMPRESSION_QUALITY = 0.8f;
    private static final int MAX_WIDTH = 1080;
    private static final int MAX_HEIGHT = 1080;

    /**
     * Compresses and converts an image to the optimal format:
     * - GIF: Preserves as GIF (supports animation)
     * - All other formats: Converts to JPEG with compression for maximum space savings
     * Supported formats: JPEG, PNG, GIF, BMP, TIFF, WebP (via TwelveMonkeys ImageIO)
     * 
     * @param file The image file to compress
     * @return byte array of the compressed image
     * @throws IOException if there's an error during compression
     */
    public byte[] compressImage(MultipartFile file) throws IOException {
        // Validate input
        if (file.isEmpty()) {
            throw new IOException("Image file is empty");
        }
        
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IOException("Image file too large. Maximum size allowed is 10MB");
        }

        // GIF: Preserve as GIF (supports animation)
        if (isGifFormat(file)) {
            return file.getBytes();
        }
        
        // All other formats: Convert to JPEG for maximum compression
        return convertToJPEG(file);
    }
    
    /**
     * Detects if the image file is a GIF (to preserve animation)
     * All other formats will be converted to JPEG for optimal compression
     */
    private boolean isGifFormat(MultipartFile file) {
        // First check Content-Type
        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("gif")) {
            return true;
        }
        
        // Fallback: check filename extension
        String filename = file.getOriginalFilename();
        if (filename != null) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < filename.length() - 1) {
                String extension = filename.substring(dotIndex + 1).toLowerCase();
                return "gif".equals(extension);
            }
        }
        
        return false;
    }

    /**
     * Converts an image to JPEG format with compression and mobile optimization
     */
    private byte[] convertToJPEG(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Unable to read image file: " + file.getOriginalFilename());
        }

        // Resize and convert to RGB in one step for efficiency  
        BufferedImage processedImage = processImageForJPEG(originalImage);

        // Compress and write JPEG
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(DEFAULT_COMPRESSION_QUALITY);
            }

            writer.write(null, new IIOImage(processedImage, null, null), param);
            return outputStream.toByteArray();
            
        } finally {
            writer.dispose();
        }
    }

    /**
     * Processes image for JPEG: resizes for mobile and converts to RGB in one efficient operation
     */
    private BufferedImage processImageForJPEG(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate target dimensions
        int targetWidth = originalWidth;
        int targetHeight = originalHeight;
        
        if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
            double ratio = Math.min((double) MAX_WIDTH / originalWidth, (double) MAX_HEIGHT / originalHeight);
            targetWidth = (int) (originalWidth * ratio);
            targetHeight = (int) (originalHeight * ratio);
        }
        
        // Create RGB image with target dimensions
        BufferedImage processedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = processedImage.createGraphics();
        
        // High-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // White background for transparency
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);
        
        // Draw resized image
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        
        return processedImage;
    }

    /**
     * Compresses multiple images efficiently
     */
    public List<byte[]> compressImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }

        List<byte[]> processedImages = new ArrayList<>(images.size()); // Pre-allocate
        
        for (MultipartFile image : images) {
            try {
                processedImages.add(compressImage(image));
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error compressing image: " + e.getMessage());
            }
        }
        
        return processedImages;
    }
}
