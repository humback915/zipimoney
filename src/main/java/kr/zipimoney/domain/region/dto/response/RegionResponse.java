package kr.zipimoney.domain.region.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegionResponse {

    String lawdCd;
    String sido;
    String sigungu;
    BigDecimal centerLat;
    BigDecimal centerLng;
}
