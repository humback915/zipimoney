package kr.zipimoney.domain.share.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShareCreateRequest {

    @NotBlank(message = "시군구는 필수입니다.")
    String sigungu;

    BigDecimal exclusiveArea;
    Integer totalMonths;

    @NotNull(message = "도달 가능 여부는 필수입니다.")
    Boolean reachable;

    @NotBlank(message = "밈 텍스트는 필수입니다.")
    String memeText;
}
