package kr.zipimoney.domain.region.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.zipimoney.domain.region.dto.response.GeocodeResponse;
import kr.zipimoney.domain.region.dto.response.RegionResponse;
import kr.zipimoney.domain.region.dto.response.SearchResultResponse;
import kr.zipimoney.domain.region.service.RegionService;
import kr.zipimoney.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Region", description = "지역 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "전체 지역 목록", description = "법정동코드 전체 목록 (캐시)")
    @GetMapping("/region")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<RegionResponse>> getAllRegions() {
        return ApiResponse.ok(regionService.getAllRegions());
    }

    @Operation(summary = "좌표 → 법정동코드", description = "카카오 API로 좌표를 법정동코드로 변환")
    @GetMapping(value = "/geocode", params = {"lat", "lng"})
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<GeocodeResponse> geocode(
            @RequestParam double lat,
            @RequestParam double lng) {
        return ApiResponse.ok(regionService.geocode(lat, lng));
    }

    @Operation(summary = "텍스트 → 좌표+법정동코드", description = "지역명을 좌표와 법정동코드로 변환")
    @GetMapping(value = "/geocode", params = "q")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<GeocodeResponse> geocodeByText(@RequestParam String q) {
        return ApiResponse.ok(regionService.geocodeByText(q));
    }

    @Operation(summary = "지역·단지 검색", description = "시군구명/단지명 통합 검색")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<SearchResultResponse>> search(
            @RequestParam String q,
            @RequestParam(required = false) String lawdCd) {
        return ApiResponse.ok(regionService.search(q, lawdCd));
    }
}
