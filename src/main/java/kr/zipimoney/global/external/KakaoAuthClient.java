package kr.zipimoney.global.external;

import kr.zipimoney.global.exception.DomainException;
import kr.zipimoney.global.exception.DomainExceptionCode;
import kr.zipimoney.global.external.dto.KakaoTokenResponse;
import kr.zipimoney.global.external.dto.KakaoUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class KakaoAuthClient {

    private final RestClient restClient;
    private final String restApiKey;

    public KakaoAuthClient(@Value("${kakao.rest-api-key}") String restApiKey) {
        this.restApiKey = restApiKey;
        this.restClient = RestClient.create();
    }

    public KakaoTokenResponse exchangeCode(String code, String redirectUri) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", restApiKey);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        try {
            return restClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
        } catch (Exception e) {
            log.error("카카오 토큰 교환 실패", e);
            throw new DomainException(DomainExceptionCode.KAKAO_TOKEN_EXCHANGE_FAILED);
        }
    }

    public KakaoUserInfo fetchUserInfo(String kakaoAccessToken) {
        try {
            return restClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoUserInfo.class);
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new DomainException(DomainExceptionCode.KAKAO_USER_INFO_FAILED);
        }
    }
}
