package kr.zipimoney.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KakaoLoginRequest {

    @NotBlank(message = "인가 코드는 필수입니다.")
    String code;

    @NotBlank(message = "리다이렉트 URI는 필수입니다.")
    String redirectUri;
}
