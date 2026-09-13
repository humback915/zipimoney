package kr.zipimoney.global.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegionDocument {

    @JsonProperty("region_type")
    String regionType;

    String code;

    @JsonProperty("address_name")
    String addressName;

    @JsonProperty("region_1depth_name")
    String region1depthName;

    @JsonProperty("region_2depth_name")
    String region2depthName;

    @JsonProperty("region_3depth_name")
    String region3depthName;

    double x;
    double y;
}
