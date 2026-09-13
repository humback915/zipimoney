package kr.zipimoney.domain.profile.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileSaveRequest {

    Integer birthYear;
    Long annualIncome;
    Long currentAssets;

    @DecimalMin(value = "0.0", message = "실수령 비율은 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", message = "실수령 비율은 1 이하여야 합니다.")
    BigDecimal takeHomeRatio;

    @DecimalMin(value = "0.0", message = "저축률은 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", message = "저축률은 1 이하여야 합니다.")
    BigDecimal savingRate;

    BigDecimal savingsApr;
    BigDecimal housePriceGrowth;

    @DecimalMin(value = "0.0", message = "LTV는 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", message = "LTV는 1 이하여야 합니다.")
    BigDecimal loanLtv;

    String jobCategory;
}
