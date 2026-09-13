package kr.zipimoney.domain.auth.service;

import kr.zipimoney.domain.auth.dto.response.AuthResponse;
import kr.zipimoney.global.external.KakaoAuthClient;
import kr.zipimoney.global.external.dto.KakaoTokenResponse;
import kr.zipimoney.global.external.dto.KakaoUserInfo;
import kr.zipimoney.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final AuthService authService;
    private final JwtProvider jwtProvider;

    public AuthResponse handleKakaoLogin(String code, String redirectUri) {
        // 1. 인가 코드 → 카카오 토큰
        KakaoTokenResponse tokenResponse = kakaoAuthClient.exchangeCode(code, redirectUri);

        // 2. 카카오 토큰 → 사용자 정보
        KakaoUserInfo userInfo = kakaoAuthClient.fetchUserInfo(tokenResponse.getAccessToken());

        // 3. DB 유저 생성/조회
        String kakaoId = String.valueOf(userInfo.getId());
        String name = null;
        String email = null;
        String gender = null;
        String ageRange = null;

        if (userInfo.getKakaoAccount() != null) {
            name = userInfo.getKakaoAccount().getName();
            email = userInfo.getKakaoAccount().getEmail();
            gender = userInfo.getKakaoAccount().getGender();
            ageRange = userInfo.getKakaoAccount().getAgeRange();
        }

        AuthService.LoginResult loginResult = authService.loginOrRegister(
                kakaoId, name, email, gender, ageRange);

        // 4. JWT 발급
        String accessToken = jwtProvider.generateAccessToken(loginResult.userId());
        String refreshToken = jwtProvider.generateRefreshToken(loginResult.userId());

        return AuthResponse.builder()
                .userId(loginResult.userId())
                .isNew(loginResult.isNew())
                .accessToken(accessToken)
                .build();
    }
}
