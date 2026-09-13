package kr.zipimoney.domain.realestate.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DealResponse {

    String name;
    String dong;
    double area;
    long price;
    int floor;
    int dealYear;
    int dealMonth;
    int dealDay;
    int buildYear;
    String roadName;
}
