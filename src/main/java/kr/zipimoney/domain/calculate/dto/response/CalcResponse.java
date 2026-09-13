package kr.zipimoney.domain.calculate.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CalcResponse {

    boolean reachable;
    int totalMonths;
    int years;
    int restMonths;
    long monthlySaving;
    long requiredEquity;
    long totalInterest;
    List<BreakdownPoint> breakdown;
    FunFacts fun;

    @Getter
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BreakdownPoint {
        int month;
        long assets;
        long target;
    }

    @Getter
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FunFacts {
        Integer ageWhenDone;
        long megaCoffee;
        long starbucksCoffee;
        long bbqChicken;
        String memeText;
    }
}
