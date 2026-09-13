package kr.zipimoney.domain.profile.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileResponse {

    Integer birthYear;
    Long annualIncome;
    Long currentAssets;
    BigDecimal takeHomeRatio;
    BigDecimal savingRate;
    BigDecimal savingsApr;
    BigDecimal housePriceGrowth;
    BigDecimal loanLtv;
    String jobCategory;
}
