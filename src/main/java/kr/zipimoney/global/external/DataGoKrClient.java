package kr.zipimoney.global.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@Component
public class DataGoKrClient {

    private static final String BASE_URL = "https://apis.data.go.kr/1613000";
    private final RestClient restClient;
    private final String serviceKey;
    private final XmlMapper xmlMapper;

    public DataGoKrClient(@Value("${data-go-kr.service-key}") String serviceKey) {
        this.serviceKey = serviceKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15_000);   // 15초
        factory.setReadTimeout(60_000);      // 60초

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.findAndRegisterModules();
    }

    public List<DealDto> fetchAptDeals(String lawdCd, String dealYmd) {
        return fetchDeals(
                "RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev",
                lawdCd, dealYmd, "apt",
                item -> item.aptNm != null ? item.aptNm.trim() : ""
        );
    }

    public List<DealDto> fetchVillaDeals(String lawdCd, String dealYmd) {
        return fetchDeals(
                "RTMSDataSvcRHTrade/getRTMSDataSvcRHTrade",
                lawdCd, dealYmd, "villa",
                item -> {
                    if (item.mhouseNm != null && !item.mhouseNm.isBlank()) return item.mhouseNm.trim();
                    if (item.aptNm != null && !item.aptNm.isBlank()) return item.aptNm.trim();
                    return "";
                }
        );
    }

    public List<DealDto> fetchSingleFamilyDeals(String lawdCd, String dealYmd) {
        return fetchDeals(
                "RTMSDataSvcSHTrade/getRTMSDataSvcSHTrade",
                lawdCd, dealYmd, "house",
                item -> {
                    String type = item.houseType != null ? item.houseType.trim() : "단독주택";
                    String road = item.roadNm != null ? item.roadNm.trim() : "";
                    return road.isEmpty() ? type : road + " " + type;
                }
        );
    }

    public List<DealDto> fetchOfficetelDeals(String lawdCd, String dealYmd) {
        return fetchDeals(
                "RTMSDataSvcOffiTrade/getRTMSDataSvcOffiTrade",
                lawdCd, dealYmd, "officetel",
                item -> {
                    if (item.offiNm != null && !item.offiNm.isBlank()) return item.offiNm.trim();
                    if (item.aptNm != null && !item.aptNm.isBlank()) return item.aptNm.trim();
                    return "";
                }
        );
    }

    /**
     * 4개 API 병렬 호출 후 합침
     */
    public List<DealDto> fetchAllDeals(String lawdCd, String dealYmd) {
        CompletableFuture<List<DealDto>> aptFuture = CompletableFuture.supplyAsync(
                () -> fetchAptDeals(lawdCd, dealYmd));
        CompletableFuture<List<DealDto>> villaFuture = CompletableFuture.supplyAsync(
                () -> fetchVillaDeals(lawdCd, dealYmd));
        CompletableFuture<List<DealDto>> singleFuture = CompletableFuture.supplyAsync(
                () -> fetchSingleFamilyDeals(lawdCd, dealYmd));
        CompletableFuture<List<DealDto>> officeFuture = CompletableFuture.supplyAsync(
                () -> fetchOfficetelDeals(lawdCd, dealYmd));

        CompletableFuture.allOf(aptFuture, villaFuture, singleFuture, officeFuture).join();

        List<DealDto> all = new ArrayList<>();
        addSafely(all, aptFuture);
        addSafely(all, villaFuture);
        addSafely(all, singleFuture);
        addSafely(all, officeFuture);
        return all;
    }

    private void addSafely(List<DealDto> target, CompletableFuture<List<DealDto>> future) {
        try {
            target.addAll(future.get());
        } catch (Exception e) {
            log.warn("일부 거래 데이터 조회 실패", e);
        }
    }

    private List<DealDto> fetchDeals(String endpoint, String lawdCd, String dealYmd,
                                     String propertyType,
                                     Function<RawItem, String> nameExtractor) {
        try {
            String xml = restClient.get()
                    .uri(BASE_URL + "/" + endpoint
                            + "?serviceKey={key}&LAWD_CD={lawdCd}&DEAL_YMD={dealYmd}&pageNo=1&numOfRows=1000",
                            serviceKey, lawdCd, dealYmd)
                    .retrieve()
                    .body(String.class);

            if (xml == null || xml.isBlank()) {
                return Collections.emptyList();
            }

            XmlResponse response = xmlMapper.readValue(xml, XmlResponse.class);
            if (response.body == null || response.body.items == null || response.body.items.itemList == null) {
                return Collections.emptyList();
            }

            return response.body.items.itemList.stream()
                    .filter(item -> {
                        String cdealType = item.cdealType != null ? item.cdealType.trim() : "";
                        return cdealType.isEmpty();
                    })
                    .map(item -> {
                        String name = nameExtractor.apply(item);
                        String dong = item.umdNm != null ? item.umdNm.trim() : "";
                        double area = parseDouble(item.excluUseAr);
                        long price = parsePrice(item.dealAmount);
                        int floor = parseInt(item.floor);
                        int dealYear = parseInt(item.dealYear);
                        int dealMonth = parseInt(item.dealMonth);
                        int dealDay = parseInt(item.dealDay);
                        int buildYear = parseInt(item.buildYear);
                        String roadName = item.roadNm != null ? item.roadNm.trim() : "";
                        return new DealDto(name, dong, area, price, floor,
                                dealYear, dealMonth, dealDay, buildYear, roadName, propertyType);
                    })
                    .filter(d -> !d.name().isBlank() && d.price() > 0)
                    .toList();
        } catch (Exception e) {
            log.error("공공데이터 API 조회 실패: endpoint={}, lawdCd={}, dealYmd={}", endpoint, lawdCd, dealYmd, e);
            return Collections.emptyList();
        }
    }

    private static long parsePrice(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        String cleaned = raw.replace(",", "").trim();
        try {
            return Long.parseLong(cleaned) * 10000;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int parseInt(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // DTO
    public record DealDto(
            String name,
            String dong,
            double area,
            long price,
            int floor,
            int dealYear,
            int dealMonth,
            int dealDay,
            int buildYear,
            String roadName,
            String propertyType
    ) {}

    // XML mapping classes
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JacksonXmlRootElement(localName = "response")
    static class XmlResponse {
        @JacksonXmlProperty(localName = "body")
        XmlBody body;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class XmlBody {
        @JacksonXmlProperty(localName = "items")
        XmlItems items;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class XmlItems {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        List<RawItem> itemList;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class RawItem {
        String umdNm;
        String excluUseAr;
        String dealAmount;
        String floor;
        String buildYear;
        String dealYear;
        String dealMonth;
        String dealDay;
        String roadNm;
        String cdealType;
        String cdealDay;
        String aptNm;
        String mhouseNm;
        String houseType;
        String offiNm;
    }
}
