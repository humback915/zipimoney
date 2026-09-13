package kr.zipimoney.domain.calculate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CalcRequest {

    @NotNull(message = "집값은 필수입니다.")
    @Min(value = 0, message = "집값은 0 이상이어야 합니다.")
    Long housePrice;

    @NotNull(message = "연봉은 필수입니다.")
    @Min(value = 0, message = "연봉은 0 이상이어야 합니다.")
    Long annualIncome;

    double takeHomeRatio = 0.84;
    double savingRate;
    long currentAssets;
    double savingsApr = 0.03;
    double housePriceGrowth = 0.02;
    double loanLtv;

    Integer birthYear;

    // 선택적 필드 (이력 저장용)
    Long complexId;
    String lawdCd;
    Double exclusiveArea;
    String sigungu;
}
