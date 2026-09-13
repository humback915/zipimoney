package kr.zipimoney.global.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeywordDocument {

    @JsonProperty("place_name")
    String placeName;

    @JsonProperty("address_name")
    String addressName;

    String x;
    String y;

    @JsonProperty("category_name")
    String categoryName;
}
