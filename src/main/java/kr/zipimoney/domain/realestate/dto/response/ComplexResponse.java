package kr.zipimoney.domain.realestate.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComplexResponse {

    String name;
    String dong;
    String propertyType;
    double lat;
    double lng;
    long avgPrice;
    List<DealResponse> deals;
    int buildYear;
}
