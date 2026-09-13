package kr.zipimoney.domain.region.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeocodeResponse {

    double lat;
    double lng;
    String lawdCd;
    String regionName;
}
