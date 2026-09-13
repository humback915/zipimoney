package kr.zipimoney.domain.calculate.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HistoryResponse {

    Long id;
    String lawdCd;
    BigDecimal exclusiveArea;
    long housePrice;
    Integer totalMonths;
    boolean reachable;
    LocalDateTime createdAt;
}
