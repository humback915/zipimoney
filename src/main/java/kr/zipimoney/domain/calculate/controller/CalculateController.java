package kr.zipimoney.domain.calculate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.zipimoney.domain.calculate.dto.request.CalcRequest;
import kr.zipimoney.domain.calculate.dto.response.CalcResponse;
import kr.zipimoney.domain.calculate.dto.response.HistoryResponse;
import kr.zipimoney.domain.calculate.service.CalculatorService;
import kr.zipimoney.global.response.ApiResponse;
import kr.zipimoney.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Calculate", description = "계산 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CalculateController {

    private final CalculatorService calculatorService;

    @Operation(summary = "내 집 마련 계산", description = "입력 조건으로 내 집 마련 소요 기간 계산")
    @PostMapping("/calculate")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CalcResponse> calculate(
            @CurrentUser Long userId,
            @Valid @RequestBody CalcRequest request) {

        CalcResponse result = calculatorService.calculate(request);

        // 로그인 사용자면 이력 저장
        if (userId != null) {
            calculatorService.saveCalculation(userId, request, result);
        }

        return ApiResponse.ok(result);
    }

    @Operation(summary = "계산 이력 조회", description = "최근 20건의 계산 이력")
    @GetMapping("/history")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<HistoryResponse>> getHistory(@CurrentUser Long userId) {
        return ApiResponse.ok(calculatorService.getHistory(userId));
    }
}
