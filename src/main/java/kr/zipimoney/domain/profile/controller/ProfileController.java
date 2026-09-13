package kr.zipimoney.domain.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.zipimoney.domain.profile.dto.request.ProfileSaveRequest;
import kr.zipimoney.domain.profile.dto.response.ProfileResponse;
import kr.zipimoney.domain.profile.service.ProfileService;
import kr.zipimoney.global.response.ApiResponse;
import kr.zipimoney.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "프로필 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "프로필 조회", description = "현재 사용자의 프로필 (복호화)")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ProfileResponse> getProfile(@CurrentUser Long userId) {
        return ApiResponse.ok(profileService.getProfile(userId));
    }

    @Operation(summary = "프로필 저장", description = "프로필 저장/수정 (금액 필드 암호화)")
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ProfileResponse> saveProfile(
            @CurrentUser Long userId,
            @Valid @RequestBody ProfileSaveRequest request) {
        return ApiResponse.ok(profileService.saveProfile(userId, request));
    }
}
