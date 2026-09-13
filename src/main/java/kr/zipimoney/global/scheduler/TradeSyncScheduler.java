package kr.zipimoney.global.scheduler;

import kr.zipimoney.domain.realestate.service.SyncService;
import kr.zipimoney.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeSyncScheduler {

    private final SyncService syncService;
    private final RegionRepository regionRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void syncAllRegions() {
        log.info("=== 일일 거래 데이터 동기화 시작 ===");

        String dealYmd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        regionRepository.findAll().forEach(region -> {
            try {
                if (syncService.needsSync(region.getLawdCd(), dealYmd)) {
                    syncService.syncTrades(region.getLawdCd(), dealYmd);
                    syncService.geocodeComplexes(region.getLawdCd());
                    log.info("동기화 완료: {}", region.getLawdCd());
                }
            } catch (Exception e) {
                log.error("동기화 실패: lawdCd={}", region.getLawdCd(), e);
            }
        });

        log.info("=== 일일 거래 데이터 동기화 종료 ===");
    }
}
