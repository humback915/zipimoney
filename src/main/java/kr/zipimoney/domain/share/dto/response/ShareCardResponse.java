package kr.zipimoney.domain.share.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShareCardResponse {

    String shareKey;
    String sigungu;
    BigDecimal exclusiveArea;
    Integer totalMonths;
    boolean reachable;
    String memeText;
    String imageUrl;
    int viewCount;
}
