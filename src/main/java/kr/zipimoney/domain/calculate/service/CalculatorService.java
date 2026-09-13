package kr.zipimoney.domain.calculate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.zipimoney.domain.calculate.dto.request.CalcRequest;
import kr.zipimoney.domain.calculate.dto.response.CalcResponse;
import kr.zipimoney.domain.calculate.dto.response.CalcResponse.BreakdownPoint;
import kr.zipimoney.domain.calculate.dto.response.CalcResponse.FunFacts;
import kr.zipimoney.domain.calculate.dto.response.HistoryResponse;
import kr.zipimoney.domain.calculate.entity.CalculationHistory;
import kr.zipimoney.domain.calculate.repository.CalculationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorService {

    private static final int MAX_MONTHS = 1200;
    private static final int SCAN_STEP = 6;

    private final CalculationHistoryRepository historyRepository;
    private final MemeTextGenerator memeTextGenerator;
    private final ObjectMapper objectMapper;

    public CalcResponse calculate(CalcRequest request) {
        long housePrice = request.getHousePrice();
        long annualIncome = request.getAnnualIncome();
        double takeHomeRatio = request.getTakeHomeRatio();
        double savingRate = request.getSavingRate();
        long currentAssets = request.getCurrentAssets();
        double savingsApr = request.getSavingsApr();
        double housePriceGrowth = request.getHousePriceGrowth();
        double loanLtv = request.getLoanLtv();
        Integer birthYear = request.getBirthYear();

        double monthlyTakeHome = (annualIncome / 12.0) * takeHomeRatio;
        double S = monthlyTakeHome * savingRate;
        double r = savingsApr / 12.0;
        double g = housePriceGrowth;

        long initialEquity = Math.round(housePrice * (1 - loanLtv));
        long G0 = initialEquity - currentAssets;

        // 즉시 구매 가능
        if (G0 <= 0) {
            FunFacts fun = computeFun(0, true, 0, birthYear, housePrice, request.getSigungu(), request.getExclusiveArea());
            return CalcResponse.builder()
                    .reachable(true)
                    .totalMonths(0)
                    .years(0)
                    .restMonths(0)
                    .monthlySaving(Math.round(S))
                    .requiredEquity(initialEquity)
                    .totalInterest(0)
                    .breakdown(List.of(BreakdownPoint.builder()
                            .month(0).assets(currentAssets).target(initialEquity).build()))
                    .fun(fun)
                    .build();
        }

        // 월 저축액이 0 이하
        if (S <= 0) {
            long reqEq = Math.round(requiredEquityAt(0, housePrice, g, loanLtv));
            FunFacts fun = computeFun(0, false, reqEq, birthYear, housePrice, request.getSigungu(), request.getExclusiveArea());
            return CalcResponse.builder()
                    .reachable(false)
                    .totalMonths(0)
                    .years(0)
                    .restMonths(0)
                    .monthlySaving(0)
                    .requiredEquity(reqEq)
                    .totalInterest(0)
                    .breakdown(List.of())
                    .fun(fun)
                    .build();
        }

        // 이율 0, 상승률 0
        if (g == 0 && r == 0) {
            int months = (int) Math.ceil((double) G0 / S);
            if (months > MAX_MONTHS) {
                long reqEq = initialEquity;
                return CalcResponse.builder()
                        .reachable(false)
                        .totalMonths(0).years(0).restMonths(0)
                        .monthlySaving(Math.round(S))
                        .requiredEquity(reqEq)
                        .totalInterest(0)
                        .breakdown(buildBreakdown(MAX_MONTHS, currentAssets, S, r, housePrice, g, loanLtv))
                        .fun(computeFun(0, false, reqEq, birthYear, housePrice, request.getSigungu(), request.getExclusiveArea()))
                        .build();
            }
            int years = months / 12;
            int restMonths = months % 12;
            long reqEq = initialEquity;
            return CalcResponse.builder()
                    .reachable(true)
                    .totalMonths(months)
                    .years(years)
                    .restMonths(restMonths)
                    .monthlySaving(Math.round(S))
                    .requiredEquity(reqEq)
                    .totalInterest(0)
                    .breakdown(buildBreakdown(months, currentAssets, S, r, housePrice, g, loanLtv))
                    .fun(computeFun(months, true, reqEq, birthYear, housePrice, request.getSigungu(), request.getExclusiveArea()))
                    .build();
        }

        // 6개월 단위 조탐색 + 이분 탐색
        int foundMonth = -1;

        for (int t = SCAN_STEP; t <= MAX_MONTHS; t += SCAN_STEP) {
            double diff = assetsAt(t, currentAssets, S, r) - requiredEquityAt(t, housePrice, g, loanLtv);
            if (diff >= 0) {
                int lo = t - SCAN_STEP + 1;
                int hi = t;
                while (lo < hi) {
                    int mid = (lo + hi) / 2;
                    double midDiff = assetsAt(mid, currentAssets, S, r) - requiredEquityAt(mid, housePrice, g, loanLtv);
                    if (midDiff >= 0) {
                        hi = mid;
                    } else {
                        lo = mid + 1;
                    }
                }
                foundMonth = lo;
                break;
            }
        }

        if (foundMonth == -1) {
            long reqEq = Math.round(requiredEquityAt(MAX_MONTHS, housePrice, g, loanLtv));
            return CalcResponse.builder()
                    .reachable(false)
                    .totalMonths(0).years(0).restMonths(0)
                    .monthlySaving(Math.round(S))
                    .requiredEquity(reqEq)
                    .totalInterest(0)
                    .breakdown(buildBreakdown(MAX_MONTHS, currentAssets, S, r, housePrice, g, loanLtv))
                    .fun(computeFun(0, false, reqEq, birthYear, housePrice, request.getSigungu(), request.getExclusiveArea()))
                    .build();
        }

        int totalMonths = foundMonth;
        int years = totalMonths / 12;
        int restMonths = totalMonths % 12;
        double finalAssets = assetsAt(totalMonths, currentAssets, S, r);
        double pureDeposit = currentAssets + S * totalMonths;
        long totalInterest = Math.max(0, Math.round(finalAssets - pureDeposit));
        long reqEq = Math.round(requiredEquityAt(totalMonths, housePrice, g, loanLtv));

        return CalcResponse.builder()
                .reachable(true)
                .totalMonths(totalMonths)
                .years(years)
                .restMonths(restMonths)
                .monthlySaving(Math.round(S))
                .requiredEquity(reqEq)
                .totalInterest(totalInterest)
                .breakdown(buildBreakdown(totalMonths, currentAssets, S, r, housePrice, g, loanLtv))
                .fun(computeFun(totalMonths, true, reqEq, birthYear, housePrice, request.getSigungu(), request.getExclusiveArea()))
                .build();
    }

    @Transactional
    public void saveCalculation(Long userId, CalcRequest request, CalcResponse result) {
        try {
            String inputsJson = objectMapper.writeValueAsString(request);

            CalculationHistory history = CalculationHistory.builder()
                    .userId(userId)
                    .complexId(request.getComplexId())
                    .lawdCd(request.getLawdCd() != null ? request.getLawdCd() : "00000")
                    .exclusiveArea(request.getExclusiveArea() != null
                            ? BigDecimal.valueOf(request.getExclusiveArea()) : null)
                    .housePrice(request.getHousePrice())
                    .totalMonths(result.getTotalMonths())
                    .reachable(result.isReachable())
                    .inputsJson(inputsJson)
                    .build();

            historyRepository.save(history);
        } catch (Exception e) {
            log.warn("계산 이력 저장 실패", e);
        }
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> getHistory(Long userId) {
        return historyRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(h -> HistoryResponse.builder()
                        .id(h.getId())
                        .lawdCd(h.getLawdCd())
                        .exclusiveArea(h.getExclusiveArea())
                        .housePrice(h.getHousePrice())
                        .totalMonths(h.getTotalMonths())
                        .reachable(h.isReachable())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();
    }

    // --- 내부 계산 함수 ---

    private static double assetsAt(int t, long currentAssets, double S, double r) {
        if (r == 0) return currentAssets + S * t;
        return currentAssets * Math.pow(1 + r, t) + S * (Math.pow(1 + r, t) - 1) / r;
    }

    private static double requiredEquityAt(int t, long housePrice, double g, double loanLtv) {
        return housePrice * Math.pow(1 + g, t / 12.0) * (1 - loanLtv);
    }

    private List<BreakdownPoint> buildBreakdown(int totalMonths, long currentAssets,
                                                 double S, double r, long housePrice, double g, double loanLtv) {
        List<BreakdownPoint> points = new ArrayList<>();
        int step = 3;
        int cap = Math.min(totalMonths, MAX_MONTHS);

        for (int m = 0; m <= cap; m += step) {
            points.add(BreakdownPoint.builder()
                    .month(m)
                    .assets(Math.round(assetsAt(m, currentAssets, S, r)))
                    .target(Math.round(requiredEquityAt(m, housePrice, g, loanLtv)))
                    .build());
        }

        if (cap % step != 0) {
            points.add(BreakdownPoint.builder()
                    .month(cap)
                    .assets(Math.round(assetsAt(cap, currentAssets, S, r)))
                    .target(Math.round(requiredEquityAt(cap, housePrice, g, loanLtv)))
                    .build());
        }

        return points;
    }

    private FunFacts computeFun(int totalMonths, boolean reachable, long requiredEquity,
                                Integer birthYear, long housePrice, String sigungu, Double area) {
        long base = housePrice > 0 ? housePrice : requiredEquity;
        long megaCoffee = base / 2000;
        long starbucksCoffee = base / 4500;
        long bbqChicken = base / 22000;

        int currentYear = Year.now().getValue();
        int currentMonth = java.time.MonthDay.now().getMonthValue();
        Integer ageWhenDone = null;
        if (birthYear != null && reachable) {
            ageWhenDone = currentYear - birthYear + (currentMonth + totalMonths - 1) / 12;
        }

        Integer age = birthYear != null ? currentYear - birthYear : null;
        String memeText = memeTextGenerator.generate(sigungu, area, totalMonths, reachable, age);

        return FunFacts.builder()
                .ageWhenDone(ageWhenDone)
                .megaCoffee(megaCoffee)
                .starbucksCoffee(starbucksCoffee)
                .bbqChicken(bbqChicken)
                .memeText(memeText)
                .build();
    }
}
