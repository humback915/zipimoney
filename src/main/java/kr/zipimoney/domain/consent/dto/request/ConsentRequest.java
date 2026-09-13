package kr.zipimoney.domain.consent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsentRequest {

    @NotBlank(message = "동의 유형은 필수입니다.")
    String consentType;

    @NotBlank(message = "약관 버전은 필수입니다.")
    String termsVersion;

    @NotNull(message = "동의 여부는 필수입니다.")
    Boolean agreed;
}
