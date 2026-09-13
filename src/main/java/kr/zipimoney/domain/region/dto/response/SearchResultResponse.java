package kr.zipimoney.domain.region.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchResultResponse {

    String type;    // "region" or "complex"
    String label;
    String code;
    String name;
    Double lat;
    Double lng;
}
