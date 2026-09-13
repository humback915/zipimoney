package kr.zipimoney.domain.calculate.service;

import kr.zipimoney.domain.calculate.dto.request.CalcRequest;
import kr.zipimoney.domain.calculate.dto.response.CalcResponse;
import kr.zipimoney.domain.calculate.repository.CalculationHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CalculatorServiceTest {

    private CalculatorService calculatorService;

    @Mock
    private CalculationHistoryRepository historyRepository;

    private final MemeTextGenerator memeTextGenerator = new MemeTextGenerator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorService(historyRepository, memeTextGenerator, objectMapper);
    }

    @Test
    @DisplayName("자산이 집값보다 많으면 즉시 구매 가능 (totalMonths=0)")
    void calculate_immediatelyReachable() {
        CalcRequest request = new CalcRequest();
        ReflectionTestUtils.setField(request, "housePrice", 500_000_000L);
        ReflectionTestUtils.setField(request, "annualIncome", 60_000_000L);
        ReflectionTestUtils.setField(request, "takeHomeRatio", 0.84);
        ReflectionTestUtils.setField(request, "savingRate", 0.5);
        ReflectionTestUtils.setField(request, "currentAssets", 600_000_000L);
        ReflectionTestUtils.setField(request, "savingsApr", 0.03);
        ReflectionTestUtils.setField(request, "housePriceGrowth", 0.02);
        ReflectionTestUtils.setField(request, "loanLtv", 0.0);

        CalcResponse result = calculatorService.calculate(request);

        assertThat(result.isReachable()).isTrue();
        assertThat(result.getTotalMonths()).isEqualTo(0);
    }

    @Test
    @DisplayName("저축률이 0이면 도달 불가")
    void calculate_zeroSavingRate_unreachable() {
        CalcRequest request = new CalcRequest();
        ReflectionTestUtils.setField(request, "housePrice", 500_000_000L);
        ReflectionTestUtils.setField(request, "annualIncome", 60_000_000L);
        ReflectionTestUtils.setField(request, "takeHomeRatio", 0.84);
        ReflectionTestUtils.setField(request, "savingRate", 0.0);
        ReflectionTestUtils.setField(request, "currentAssets", 100_000_000L);
        ReflectionTestUtils.setField(request, "savingsApr", 0.03);
        ReflectionTestUtils.setField(request, "housePriceGrowth", 0.02);
        ReflectionTestUtils.setField(request, "loanLtv", 0.0);

        CalcResponse result = calculatorService.calculate(request);

        assertThat(result.isReachable()).isFalse();
    }

    @Test
    @DisplayName("일반적인 조건에서 도달 가능한 경우 양의 totalMonths 반환")
    void calculate_normalCase_reachable() {
        CalcRequest request = new CalcRequest();
        ReflectionTestUtils.setField(request, "housePrice", 500_000_000L);
        ReflectionTestUtils.setField(request, "annualIncome", 60_000_000L);
        ReflectionTestUtils.setField(request, "takeHomeRatio", 0.84);
        ReflectionTestUtils.setField(request, "savingRate", 0.5);
        ReflectionTestUtils.setField(request, "currentAssets", 100_000_000L);
        ReflectionTestUtils.setField(request, "savingsApr", 0.03);
        ReflectionTestUtils.setField(request, "housePriceGrowth", 0.02);
        ReflectionTestUtils.setField(request, "loanLtv", 0.0);

        CalcResponse result = calculatorService.calculate(request);

        assertThat(result.isReachable()).isTrue();
        assertThat(result.getTotalMonths()).isGreaterThan(0);
        assertThat(result.getYears()).isGreaterThanOrEqualTo(0);
        assertThat(result.getMonthlySaving()).isGreaterThan(0);
        assertThat(result.getBreakdown()).isNotEmpty();
        assertThat(result.getFun()).isNotNull();
        assertThat(result.getFun().getMemeText()).isNotBlank();
    }

    @Test
    @DisplayName("LTV 적용 시 필요 자기자본이 줄어들어 더 빨리 도달")
    void calculate_withLtv_fasterReach() {
        CalcRequest noLtv = new CalcRequest();
        ReflectionTestUtils.setField(noLtv, "housePrice", 500_000_000L);
        ReflectionTestUtils.setField(noLtv, "annualIncome", 60_000_000L);
        ReflectionTestUtils.setField(noLtv, "takeHomeRatio", 0.84);
        ReflectionTestUtils.setField(noLtv, "savingRate", 0.5);
        ReflectionTestUtils.setField(noLtv, "currentAssets", 100_000_000L);
        ReflectionTestUtils.setField(noLtv, "savingsApr", 0.03);
        ReflectionTestUtils.setField(noLtv, "housePriceGrowth", 0.02);
        ReflectionTestUtils.setField(noLtv, "loanLtv", 0.0);

        CalcRequest withLtv = new CalcRequest();
        ReflectionTestUtils.setField(withLtv, "housePrice", 500_000_000L);
        ReflectionTestUtils.setField(withLtv, "annualIncome", 60_000_000L);
        ReflectionTestUtils.setField(withLtv, "takeHomeRatio", 0.84);
        ReflectionTestUtils.setField(withLtv, "savingRate", 0.5);
        ReflectionTestUtils.setField(withLtv, "currentAssets", 100_000_000L);
        ReflectionTestUtils.setField(withLtv, "savingsApr", 0.03);
        ReflectionTestUtils.setField(withLtv, "housePriceGrowth", 0.02);
        ReflectionTestUtils.setField(withLtv, "loanLtv", 0.5);

        CalcResponse resultNoLtv = calculatorService.calculate(noLtv);
        CalcResponse resultWithLtv = calculatorService.calculate(withLtv);

        if (resultNoLtv.isReachable() && resultWithLtv.isReachable()) {
            assertThat(resultWithLtv.getTotalMonths()).isLessThanOrEqualTo(resultNoLtv.getTotalMonths());
        }
    }

    @Test
    @DisplayName("이자율과 상승률 모두 0인 단순 계산")
    void calculate_zeroRates_simpleCalculation() {
        CalcRequest request = new CalcRequest();
        ReflectionTestUtils.setField(request, "housePrice", 300_000_000L);
        ReflectionTestUtils.setField(request, "annualIncome", 60_000_000L);
        ReflectionTestUtils.setField(request, "takeHomeRatio", 0.84);
        ReflectionTestUtils.setField(request, "savingRate", 0.5);
        ReflectionTestUtils.setField(request, "currentAssets", 0L);
        ReflectionTestUtils.setField(request, "savingsApr", 0.0);
        ReflectionTestUtils.setField(request, "housePriceGrowth", 0.0);
        ReflectionTestUtils.setField(request, "loanLtv", 0.0);

        CalcResponse result = calculatorService.calculate(request);

        assertThat(result.isReachable()).isTrue();
        assertThat(result.getTotalInterest()).isEqualTo(0);
    }
}
