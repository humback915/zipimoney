package kr.zipimoney.domain.consent.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsentResponse {

    String consentType;
    String termsVersion;
    boolean agreed;
    LocalDateTime agreedAt;
}
