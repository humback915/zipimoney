package kr.zipimoney.global.external;

import com.fasterxml.jackson.databind.JsonNode;
import kr.zipimoney.global.exception.DomainException;
import kr.zipimoney.global.exception.DomainExceptionCode;
import kr.zipimoney.global.external.dto.KeywordDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KakaoMapsClient {

    private final RestClient restClient;
    private final String restApiKey;

    public KakaoMapsClient(@Value("${kakao.rest-api-key}") String restApiKey) {
        this.restApiKey = restApiKey;
        this.restClient = RestClient.create();
    }

    /**
     * 좌표 → 법정동코드 변환
     * x = 경도(lng), y = 위도(lat)
     */
    public CoordToRegionResult coordToRegionCode(double lat, double lng) {
        try {
            JsonNode response = restClient.get()
                    .uri("https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x={lng}&y={lat}",
                            lng, lat)
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode documents = response.get("documents");
            if (documents == null || documents.isEmpty()) {
                throw new DomainException(DomainExceptionCode.GEOCODE_FAILED);
            }

            for (JsonNode doc : documents) {
                if ("B".equals(doc.get("region_type").asText())) {
                    String code = doc.get("code").asText();
                    String lawdCd = code.substring(0, 5);
                    String regionName = doc.get("region_1depth_name").asText() + " "
                            + doc.get("region_2depth_name").asText();
                    return new CoordToRegionResult(lawdCd, regionName);
                }
            }

            throw new DomainException(DomainExceptionCode.GEOCODE_FAILED);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 좌표→법정동 API 오류", e);
            throw new DomainException(DomainExceptionCode.GEOCODE_FAILED);
        }
    }

    /**
     * 키워드 장소 검색 (단지명 → 좌표)
     */
    public CoordResult searchKeyword(String query) {
        try {
            JsonNode response = restClient.get()
                    .uri("https://dapi.kakao.com/v2/local/search/keyword.json?query={query}", query)
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode documents = response.get("documents");
            if (documents == null || documents.isEmpty()) {
                return null;
            }

            JsonNode first = documents.get(0);
            return new CoordResult(
                    Double.parseDouble(first.get("y").asText()),
                    Double.parseDouble(first.get("x").asText())
            );
        } catch (Exception e) {
            log.warn("카카오 키워드 검색 실패: query={}", query, e);
            return null;
        }
    }

    /**
     * 주소 검색
     */
    public List<AddressResult> searchAddress(String query) {
        try {
            JsonNode response = restClient.get()
                    .uri("https://dapi.kakao.com/v2/local/search/address.json?query={query}", query)
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode documents = response.get("documents");
            if (documents == null || documents.isEmpty()) {
                return List.of();
            }

            return java.util.stream.StreamSupport.stream(documents.spliterator(), false)
                    .map(doc -> new AddressResult(
                            doc.get("address_name").asText(),
                            Double.parseDouble(doc.get("y").asText()),
                            Double.parseDouble(doc.get("x").asText())
                    ))
                    .toList();
        } catch (Exception e) {
            log.warn("카카오 주소 검색 실패: query={}", query, e);
            return List.of();
        }
    }

    public record CoordToRegionResult(String lawdCd, String regionName) {}
    public record CoordResult(double lat, double lng) {}
    public record AddressResult(String addressName, double lat, double lng) {}
}
