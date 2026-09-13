package kr.zipimoney.global.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KakaoUserInfo {

    long id;

    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class KakaoAccount {
        String name;
        String email;
        String gender;

        @JsonProperty("age_range")
        String ageRange;
    }
}
