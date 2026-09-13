package kr.zipimoney.domain.realestate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.zipimoney.domain.realestate.dto.response.ComplexResponse;
import kr.zipimoney.domain.realestate.service.DealService;
import kr.zipimoney.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Deal", description = "실거래 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DealController {

    private final DealService dealService;

    @Operation(summary = "실거래 목록 조회", description = "법정동코드+년월로 단지별 실거래 조회")
    @GetMapping("/deals")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ComplexResponse>> getDeals(
            @RequestParam String lawdCd,
            @RequestParam String ymd) {
        return ApiResponse.ok(dealService.getGroupedDeals(lawdCd, ymd));
    }

    @Operation(summary = "단지 상세 조회", description = "특정 단지의 실거래 상세")
    @GetMapping("/complex")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ComplexResponse> getComplex(
            @RequestParam String lawdCd,
            @RequestParam String name,
            @RequestParam String ymd) {
        return ApiResponse.ok(dealService.getComplexDeals(lawdCd, name, ymd));
    }
}
