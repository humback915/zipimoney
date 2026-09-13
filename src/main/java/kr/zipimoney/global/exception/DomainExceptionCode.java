package kr.zipimoney.global.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DomainExceptionCode {

    // Auth
    KAKAO_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "카카오 토큰 교환에 실패했습니다."),
    KAKAO_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 조회에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    USER_WITHDRAWN(HttpStatus.FORBIDDEN, "탈퇴한 사용자입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // Profile
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."),
    ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "암호화에 실패했습니다."),
    DECRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "복호화에 실패했습니다."),

    // Consent
    INVALID_CONSENT_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 동의 유형입니다."),

    // Region
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "지역을 찾을 수 없습니다."),
    GEOCODE_FAILED(HttpStatus.BAD_GATEWAY, "지오코딩에 실패했습니다."),

    // Real Estate
    DEAL_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "거래 데이터 조회에 실패했습니다."),
    SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "동기화에 실패했습니다."),
    COMPLEX_NOT_FOUND(HttpStatus.NOT_FOUND, "단지를 찾을 수 없습니다."),

    // Share
    SHARE_CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "공유 카드를 찾을 수 없습니다."),

    // Cron
    INVALID_CRON_SECRET(HttpStatus.FORBIDDEN, "유효하지 않은 크론 시크릿입니다."),

    // General
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    HttpStatus status;
    String message;
}
