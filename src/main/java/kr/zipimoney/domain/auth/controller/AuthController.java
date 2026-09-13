package kr.zipimoney.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.zipimoney.domain.auth.dto.request.KakaoLoginRequest;
import kr.zipimoney.domain.auth.dto.response.AuthResponse;
import kr.zipimoney.domain.auth.dto.response.UserResponse;
import kr.zipimoney.domain.auth.service.AuthService;
import kr.zipimoney.domain.auth.service.KakaoOAuthService;
import kr.zipimoney.global.response.ApiResponse;
import kr.zipimoney.global.security.CurrentUser;
import kr.zipimoney.global.security.JwtCookieUtil;
import kr.zipimoney.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final JwtCookieUtil jwtCookieUtil;

    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드로 로그인/회원가입")
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<AuthResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request) {

        AuthResponse response = kakaoOAuthService.handleKakaoLogin(
                request.getCode(), request.getRedirectUri());

        String accessToken = jwtProvider.generateAccessToken(response.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(response.getUserId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookieUtil.createAccessCookie(accessToken).toString())
                .header(HttpHeaders.SET_COOKIE, jwtCookieUtil.createRefreshCookie(refreshToken).toString())
                .body(ApiResponse.ok(response));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 액세스 토큰 갱신")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @CookieValue(name = "hc_refresh", required = false) String refreshToken) {

        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Void>builder()
                            .error(new ApiResponse.Error("INVALID_TOKEN", "유효하지 않은 리프레시 토큰입니다."))
                            .build());
        }

        Long userId = jwtProvider.extractUserId(refreshToken);
        String newAccessToken = jwtProvider.generateAccessToken(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookieUtil.createAccessCookie(newAccessToken).toString())
                .body(ApiResponse.ok());
    }

    @Operation(summary = "로그아웃", description = "인증 쿠키 삭제")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookieUtil.clearAccessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, jwtCookieUtil.clearRefreshCookie().toString())
                .body(ApiResponse.ok());
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자 정보")
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> me(@CurrentUser Long userId) {
        return ApiResponse.ok(authService.getCurrentUser(userId));
    }

    @Operation(summary = "회원 탈퇴", description = "계정 비활성화 (소프트 삭제)")
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(@CurrentUser Long userId) {
        authService.withdraw(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookieUtil.clearAccessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, jwtCookieUtil.clearRefreshCookie().toString())
                .body(ApiResponse.ok());
    }
}
