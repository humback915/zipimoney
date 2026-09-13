package kr.zipimoney.domain.realestate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.zipimoney.domain.realestate.service.SyncService;
import kr.zipimoney.domain.region.repository.RegionRepository;
import kr.zipimoney.global.exception.DomainException;
import kr.zipimoney.global.exception.DomainExceptionCode;
import kr.zipimoney.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Tag(name = "Sync", description = "데이터 동기화 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cron")
public class SyncController {

    private final SyncService syncService;
    private final RegionRepository regionRepository;

    @Value("${cron.secret:}")
    private String cronSecret;

    @Operation(summary = "거래 데이터 동기화", description = "전 지역 거래 데이터 동기화 (크론 시크릿 필요)")
    @PostMapping("/sync-trades")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> syncTrades(
            @RequestHeader(value = "x-cron-secret", required = false) String secret) {

        if (cronSecret == null || cronSecret.isBlank() || !cronSecret.equals(secret)) {
            throw new DomainException(DomainExceptionCode.INVALID_CRON_SECRET);
        }

        String dealYmd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        regionRepository.findAll().forEach(region -> {
            try {
                syncService.syncTrades(region.getLawdCd(), dealYmd);
                syncService.geocodeComplexes(region.getLawdCd());
            } catch (Exception e) {
                log.error("동기화 실패: lawdCd={}", region.getLawdCd(), e);
            }
        });

        return ApiResponse.ok("동기화 완료");
    }
}
