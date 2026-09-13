package kr.zipimoney.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieUtil {

    private static final String ACCESS_COOKIE = "hc_access";
    private static final String REFRESH_COOKIE = "hc_refresh";
    private static final int ACCESS_MAX_AGE = 3600;       // 1 hour
    private static final int REFRESH_MAX_AGE = 2592000;   // 30 days

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public ResponseCookie createAccessCookie(String token) {
        return buildCookie(ACCESS_COOKIE, token, ACCESS_MAX_AGE);
    }

    public ResponseCookie createRefreshCookie(String token) {
        return buildCookie(REFRESH_COOKIE, token, REFRESH_MAX_AGE);
    }

    public ResponseCookie clearAccessCookie() {
        return buildCookie(ACCESS_COOKIE, "", 0);
    }

    public ResponseCookie clearRefreshCookie() {
        return buildCookie(REFRESH_COOKIE, "", 0);
    }

    private ResponseCookie buildCookie(String name, String value, int maxAge) {
        boolean secure = !"default".equals(activeProfile) && !"local".equals(activeProfile);
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
