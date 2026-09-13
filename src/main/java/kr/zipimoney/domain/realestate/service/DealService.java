package kr.zipimoney.domain.realestate.service;

import kr.zipimoney.domain.realestate.dto.response.ComplexResponse;
import kr.zipimoney.domain.realestate.dto.response.DealResponse;
import kr.zipimoney.domain.realestate.entity.AptComplex;
import kr.zipimoney.domain.realestate.entity.AptTrade;
import kr.zipimoney.domain.realestate.repository.AptComplexRepository;
import kr.zipimoney.domain.realestate.repository.AptTradeRepository;
import kr.zipimoney.global.external.DataGoKrClient;
import kr.zipimoney.global.external.KakaoMapsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService {

    private final AptComplexRepository complexRepository;
    private final AptTradeRepository tradeRepository;
    private final DataGoKrClient dataGoKrClient;
    private final KakaoMapsClient kakaoMapsClient;
    private final SyncService syncService;

    @Cacheable(value = "deals", key = "#lawdCd + '-' + #dealYmd")
    @Transactional(readOnly = true)
    public List<ComplexResponse> getGroupedDeals(String lawdCd, String dealYmd) {
        // DB 우선 시도
        try {
            boolean needsSync = syncService.needsSync(lawdCd, dealYmd);

            if (!needsSync) {
                List<ComplexResponse> dbResult = getDealsFromDB(lawdCd, dealYmd);
                if (dbResult != null && !dbResult.isEmpty()) {
                    return dbResult;
                }
            }

            // 동기화 필요 시 실행
            if (needsSync) {
                try {
                    syncService.syncTrades(lawdCd, dealYmd);
                    syncService.geocodeComplexes(lawdCd);

                    List<ComplexResponse> dbResult = getDealsFromDB(lawdCd, dealYmd);
                    if (dbResult != null && !dbResult.isEmpty()) {
                        return dbResult;
                    }
                } catch (Exception e) {
                    log.warn("DB 동기화 실패, API 직접 조회로 폴백", e);
                }
            }
        } catch (Exception e) {
            log.warn("DB 조회 실패, API 직접 조회로 폴백", e);
        }

        // 폴백: API 직접 조회
        return getDealsFromAPI(lawdCd, dealYmd);
    }

    @Transactional(readOnly = true)
    public ComplexResponse getComplexDeals(String lawdCd, String name, String dealYmd) {
        List<ComplexResponse> all = getGroupedDeals(lawdCd, dealYmd);
        return all.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private List<ComplexResponse> getDealsFromDB(String lawdCd, String dealYmd) {
        int year = Integer.parseInt(dealYmd.substring(0, 4));
        int month = Integer.parseInt(dealYmd.substring(4, 6));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<AptComplex> complexes = complexRepository.findAllByLawdCd(lawdCd);

        List<ComplexResponse> result = new ArrayList<>();
        for (AptComplex complex : complexes) {
            List<AptTrade> trades = tradeRepository
                    .findAllByComplexIdAndDealDateBetweenAndCanceledFalse(
                            complex.getId(), startDate, endDate);

            if (trades.isEmpty()) continue;

            long avgPrice = trades.stream()
                    .mapToLong(AptTrade::getDealAmount)
                    .sum() / trades.size();

            List<DealResponse> dealResponses = trades.stream()
                    .sorted(Comparator.comparing(AptTrade::getDealDate).reversed())
                    .map(t -> DealResponse.builder()
                            .name(complex.getName())
                            .dong(complex.getDong())
                            .area(t.getExclusiveArea().doubleValue())
                            .price(t.getDealAmount())
                            .floor(t.getFloor() != null ? t.getFloor() : 0)
                            .dealYear(t.getDealDate().getYear())
                            .dealMonth(t.getDealDate().getMonthValue())
                            .dealDay(t.getDealDate().getDayOfMonth())
                            .buildYear(complex.getBuiltYear() != null ? complex.getBuiltYear() : 0)
                            .roadName("")
                            .build())
                    .toList();

            result.add(ComplexResponse.builder()
                    .name(complex.getName())
                    .dong(complex.getDong())
                    .propertyType(complex.getPropertyType())
                    .lat(complex.getLat() != null ? complex.getLat().doubleValue() : 0)
                    .lng(complex.getLng() != null ? complex.getLng().doubleValue() : 0)
                    .avgPrice(avgPrice)
                    .deals(dealResponses)
                    .buildYear(complex.getBuiltYear() != null ? complex.getBuiltYear() : 0)
                    .build());
        }

        return result.isEmpty() ? null : result;
    }

    private List<ComplexResponse> getDealsFromAPI(String lawdCd, String dealYmd) {
        List<DataGoKrClient.DealDto> deals = dataGoKrClient.fetchAllDeals(lawdCd, dealYmd);

        // 단지별 그루핑
        Map<String, List<DataGoKrClient.DealDto>> groups = deals.stream()
                .collect(Collectors.groupingBy(d -> d.propertyType() + "|" + d.dong() + "|" + d.name()));

        List<ComplexResponse> result = new ArrayList<>();

        for (Map.Entry<String, List<DataGoKrClient.DealDto>> entry : groups.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 3);
            String pType = parts[0];
            String dong = parts.length > 1 ? parts[1] : "";
            String name = parts.length > 2 ? parts[2] : "";
            List<DataGoKrClient.DealDto> groupDeals = entry.getValue();

            // 좌표 검색
            KakaoMapsClient.CoordResult coord = kakaoMapsClient.searchKeyword(dong + " " + name);

            long avgPrice = groupDeals.stream().mapToLong(DataGoKrClient.DealDto::price).sum() / groupDeals.size();

            List<DealResponse> dealResponses = groupDeals.stream()
                    .sorted(Comparator.comparingInt(DataGoKrClient.DealDto::dealYear).reversed()
                            .thenComparingInt(DataGoKrClient.DealDto::dealMonth).reversed()
                            .thenComparingInt(DataGoKrClient.DealDto::dealDay).reversed())
                    .map(d -> DealResponse.builder()
                            .name(d.name())
                            .dong(d.dong())
                            .area(d.area())
                            .price(d.price())
                            .floor(d.floor())
                            .dealYear(d.dealYear())
                            .dealMonth(d.dealMonth())
                            .dealDay(d.dealDay())
                            .buildYear(d.buildYear())
                            .roadName(d.roadName())
                            .build())
                    .toList();

            result.add(ComplexResponse.builder()
                    .name(name)
                    .dong(dong)
                    .propertyType(pType)
                    .lat(coord != null ? coord.lat() : 0)
                    .lng(coord != null ? coord.lng() : 0)
                    .avgPrice(avgPrice)
                    .deals(dealResponses)
                    .buildYear(groupDeals.isEmpty() ? 0 : groupDeals.getFirst().buildYear())
                    .build());
        }

        return result;
    }
}
