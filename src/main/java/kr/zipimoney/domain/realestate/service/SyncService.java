package kr.zipimoney.domain.realestate.service;

import kr.zipimoney.domain.realestate.entity.AptComplex;
import kr.zipimoney.domain.realestate.entity.AptTrade;
import kr.zipimoney.domain.realestate.entity.SyncLog;
import kr.zipimoney.domain.realestate.enums.GeocodeStatus;
import kr.zipimoney.domain.realestate.enums.SyncStatus;
import kr.zipimoney.domain.realestate.repository.AptComplexRepository;
import kr.zipimoney.domain.realestate.repository.AptTradeRepository;
import kr.zipimoney.domain.realestate.repository.SyncLogRepository;
import kr.zipimoney.global.external.DataGoKrClient;
import kr.zipimoney.global.external.KakaoMapsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private static final int GEOCODE_BATCH_SIZE = 5;

    private final AptComplexRepository complexRepository;
    private final AptTradeRepository tradeRepository;
    private final SyncLogRepository syncLogRepository;
    private final DataGoKrClient dataGoKrClient;
    private final KakaoMapsClient kakaoMapsClient;

    public boolean needsSync(String lawdCd, String dealYmd) {
        return syncLogRepository.findByLawdCdAndDealYmd(lawdCd, dealYmd)
                .map(syncLog -> syncLog.getStatus() != SyncStatus.SUCCESS
                        || syncLog.getSyncedAt().isBefore(LocalDateTime.now().minusHours(24)))
                .orElse(true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncTrades(String lawdCd, String dealYmd) {
        log.info("거래 동기화 시작: lawdCd={}, dealYmd={}", lawdCd, dealYmd);

        try {
            List<DataGoKrClient.DealDto> deals = dataGoKrClient.fetchAllDeals(lawdCd, dealYmd);

            int insertedCount = 0;
            for (DataGoKrClient.DealDto deal : deals) {
                // 단지 upsert
                AptComplex complex = complexRepository
                        .findByLawdCdAndDongAndNameAndPropertyType(lawdCd, deal.dong(), deal.name(), deal.propertyType())
                        .orElseGet(() -> {
                            AptComplex newComplex = AptComplex.builder()
                                    .lawdCd(lawdCd)
                                    .dong(deal.dong())
                                    .name(deal.name())
                                    .builtYear(deal.buildYear() > 0 ? deal.buildYear() : null)
                                    .propertyType(deal.propertyType())
                                    .geocodeStatus(GeocodeStatus.PENDING)
                                    .build();
                            return complexRepository.save(newComplex);
                        });

                // 거래 insert (중복 체크: sourceHash)
                String sourceHash = computeSourceHash(lawdCd, deal);
                if (tradeRepository.existsBySourceHash(sourceHash)) {
                    continue;
                }

                LocalDate dealDate = LocalDate.of(deal.dealYear(), deal.dealMonth(),
                        Math.max(1, deal.dealDay()));

                AptTrade trade = AptTrade.builder()
                        .complexId(complex.getId())
                        .exclusiveArea(BigDecimal.valueOf(deal.area()))
                        .floor(deal.floor())
                        .dealAmount(deal.price())
                        .dealDate(dealDate)
                        .canceled(false)
                        .sourceHash(sourceHash)
                        .build();

                tradeRepository.save(trade);
                insertedCount++;
            }

            // 동기화 로그 기록
            SyncLog syncLog = syncLogRepository.findByLawdCdAndDealYmd(lawdCd, dealYmd)
                    .orElse(SyncLog.builder()
                            .lawdCd(lawdCd)
                            .dealYmd(dealYmd)
                            .build());

            syncLog.setStatus(SyncStatus.SUCCESS);
            syncLog.setRowCount(insertedCount);
            syncLog.setSyncedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);

            log.info("거래 동기화 완료: lawdCd={}, dealYmd={}, inserted={}", lawdCd, dealYmd, insertedCount);

        } catch (Exception e) {
            log.error("거래 동기화 실패: lawdCd={}, dealYmd={}", lawdCd, dealYmd, e);

            SyncLog syncLog = syncLogRepository.findByLawdCdAndDealYmd(lawdCd, dealYmd)
                    .orElse(SyncLog.builder()
                            .lawdCd(lawdCd)
                            .dealYmd(dealYmd)
                            .build());

            syncLog.setStatus(SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage() != null
                    ? e.getMessage().substring(0, Math.min(500, e.getMessage().length())) : "Unknown error");
            syncLog.setSyncedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);

            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void geocodeComplexes(String lawdCd) {
        List<AptComplex> pending = complexRepository
                .findAllByLawdCdAndGeocodeStatus(lawdCd, GeocodeStatus.PENDING);

        log.info("지오코딩 시작: lawdCd={}, pending={}", lawdCd, pending.size());

        for (int i = 0; i < pending.size(); i += GEOCODE_BATCH_SIZE) {
            List<AptComplex> batch = pending.subList(i,
                    Math.min(i + GEOCODE_BATCH_SIZE, pending.size()));

            List<CompletableFuture<Void>> futures = batch.stream()
                    .map(complex -> CompletableFuture.runAsync(() -> {
                        try {
                            KakaoMapsClient.CoordResult coord =
                                    kakaoMapsClient.searchKeyword(complex.getDong() + " " + complex.getName());
                            if (coord != null) {
                                complex.setLat(BigDecimal.valueOf(coord.lat()));
                                complex.setLng(BigDecimal.valueOf(coord.lng()));
                                complex.setGeocodeStatus(GeocodeStatus.SUCCESS);
                            } else {
                                complex.setGeocodeStatus(GeocodeStatus.FAILED);
                            }
                            complex.setGeocodedAt(LocalDateTime.now());
                        } catch (Exception e) {
                            log.warn("지오코딩 실패: complex={}", complex.getName(), e);
                            complex.setGeocodeStatus(GeocodeStatus.FAILED);
                            complex.setGeocodedAt(LocalDateTime.now());
                        }
                    }))
                    .toList();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }

        log.info("지오코딩 완료: lawdCd={}", lawdCd);
    }

    private String computeSourceHash(String lawdCd, DataGoKrClient.DealDto deal) {
        try {
            String raw = String.join("|",
                    lawdCd, deal.dong(), deal.name(),
                    String.valueOf(deal.area()), String.valueOf(deal.floor()),
                    String.valueOf(deal.price()),
                    String.format("%d-%02d-%02d", deal.dealYear(), deal.dealMonth(), deal.dealDay()));

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 해시 생성 실패", e);
        }
    }
}
