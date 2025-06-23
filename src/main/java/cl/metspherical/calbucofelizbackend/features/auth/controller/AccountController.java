package cl.metspherical.calbucofelizbackend.features.auth.controller;

import cl.metspherical.calbucofelizbackend.common.security.utils.SecurityUtils;
import cl.metspherical.calbucofelizbackend.features.auth.dto.UserEditRequestDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.UserProfileResponseDTO;
import cl.metspherical.calbucofelizbackend.common.service.VisionSafeSearchService;
import cl.metspherical.calbucofelizbackend.common.service.ImageCompressionService;
import cl.metspherical.calbucofelizbackend.features.auth.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final VisionSafeSearchService visionSafeSearchService;
    private final ImageCompressionService imageCompressionService;
    private final AccountService accountService;   
    
    @PatchMapping(value = "/edit", consumes = {"multipart/form-data"})
    public ResponseEntity<UserProfileResponseDTO> editAccount(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "names", required = false) String names,
            @RequestParam(value = "lastNames", required = false) String lastNames,
            @RequestParam(value = "password", required = false) String password) throws IOException {

        UUID userId = SecurityUtils.getCurrentUserId();

        byte[] processedAvatar = null;

        if (avatar != null && !avatar.isEmpty()) {
            visionSafeSearchService.validateImages(List.of(avatar));

            processedAvatar = imageCompressionService.compressImage(avatar);
        }

        UserEditRequestDTO request = new UserEditRequestDTO(
                username,
                processedAvatar,
                description,
                names,
                lastNames,
                password
        );

        UserProfileResponseDTO response = accountService.updateUser(request,userId);

        return ResponseEntity.ok(response);
    }
}
