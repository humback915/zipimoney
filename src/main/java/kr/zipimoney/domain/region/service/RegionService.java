package kr.zipimoney.domain.region.service;

import kr.zipimoney.domain.region.dto.response.GeocodeResponse;
import kr.zipimoney.domain.region.dto.response.RegionResponse;
import kr.zipimoney.domain.region.dto.response.SearchResultResponse;
import kr.zipimoney.domain.region.entity.Region;
import kr.zipimoney.domain.region.repository.RegionRepository;
import kr.zipimoney.global.exception.DomainException;
import kr.zipimoney.global.exception.DomainExceptionCode;
import kr.zipimoney.global.external.KakaoMapsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;
    private final KakaoMapsClient kakaoMapsClient;

    @Cacheable(value = "regions")
    @Transactional(readOnly = true)
    public List<RegionResponse> getAllRegions() {
        return regionRepository.findAllByOrderBySidoAscSigunguAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegionResponse getRegionByCode(String lawdCd) {
        Region region = regionRepository.findByLawdCd(lawdCd)
                .orElseThrow(() -> new DomainException(DomainExceptionCode.REGION_NOT_FOUND));
        return toResponse(region);
    }

    public GeocodeResponse geocode(double lat, double lng) {
        KakaoMapsClient.CoordToRegionResult result = kakaoMapsClient.coordToRegionCode(lat, lng);
        return GeocodeResponse.builder()
                .lat(lat)
                .lng(lng)
                .lawdCd(result.lawdCd())
                .regionName(result.regionName())
                .build();
    }

    public GeocodeResponse geocodeByText(String query) {
        // 주소 검색 시도
        List<KakaoMapsClient.AddressResult> addressResults = kakaoMapsClient.searchAddress(query);
        if (!addressResults.isEmpty()) {
            KakaoMapsClient.AddressResult first = addressResults.getFirst();
            // 좌표로 법정동코드 조회
            try {
                KakaoMapsClient.CoordToRegionResult region =
                        kakaoMapsClient.coordToRegionCode(first.lat(), first.lng());
                return GeocodeResponse.builder()
                        .lat(first.lat())
                        .lng(first.lng())
                        .lawdCd(region.lawdCd())
                        .regionName(region.regionName())
                        .build();
            } catch (Exception e) {
                log.warn("주소 검색 좌표→법정동 변환 실패: query={}", query, e);
            }
        }

        // 키워드 검색 폴백
        KakaoMapsClient.CoordResult coord = kakaoMapsClient.searchKeyword(query);
        if (coord != null) {
            try {
                KakaoMapsClient.CoordToRegionResult region =
                        kakaoMapsClient.coordToRegionCode(coord.lat(), coord.lng());
                return GeocodeResponse.builder()
                        .lat(coord.lat())
                        .lng(coord.lng())
                        .lawdCd(region.lawdCd())
                        .regionName(region.regionName())
                        .build();
            } catch (Exception e) {
                log.warn("키워드 검색 좌표→법정동 변환 실패: query={}", query, e);
            }
        }

        throw new DomainException(DomainExceptionCode.GEOCODE_FAILED);
    }

    @Transactional(readOnly = true)
    public List<SearchResultResponse> search(String query, String lawdCd) {
        List<SearchResultResponse> results = new ArrayList<>();

        // 1. 지역 검색 (DB)
        List<Region> regions = regionRepository.findBySigunguContaining(query);
        for (Region region : regions) {
            results.add(SearchResultResponse.builder()
                    .type("region")
                    .label(region.getSido() + " " + region.getSigungu())
                    .code(region.getLawdCd())
                    .lat(region.getCenterLat() != null ? region.getCenterLat().doubleValue() : null)
                    .lng(region.getCenterLng() != null ? region.getCenterLng().doubleValue() : null)
                    .build());
        }

        // 2. 단지 검색 (카카오 키워드)
        try {
            KakaoMapsClient.CoordResult coord = kakaoMapsClient.searchKeyword(query + " 아파트");
            if (coord != null) {
                results.add(SearchResultResponse.builder()
                        .type("complex")
                        .label(query)
                        .name(query)
                        .lat(coord.lat())
                        .lng(coord.lng())
                        .build());
            }
        } catch (Exception e) {
            log.warn("단지 키워드 검색 실패: query={}", query, e);
        }

        return results;
    }

    private RegionResponse toResponse(Region region) {
        return RegionResponse.builder()
                .lawdCd(region.getLawdCd())
                .sido(region.getSido())
                .sigungu(region.getSigungu())
                .centerLat(region.getCenterLat())
                .centerLng(region.getCenterLng())
                .build();
    }
}
