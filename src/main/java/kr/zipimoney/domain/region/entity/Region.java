package kr.zipimoney.domain.region.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;

@Entity
@Table(name = "regions", indexes = {
        @Index(name = "idx_region_sido", columnList = "sido")
})
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Region {

    @Id
    @Column(length = 5)
    String lawdCd;

    @Column(length = 30, nullable = false)
    String sido;

    @Column(length = 50, nullable = false)
    String sigungu;

    @Column(precision = 10, scale = 7)
    BigDecimal centerLat;

    @Column(precision = 10, scale = 7)
    BigDecimal centerLng;

    @Builder
    private Region(String lawdCd, String sido, String sigungu,
                   BigDecimal centerLat, BigDecimal centerLng) {
        this.lawdCd = lawdCd;
        this.sido = sido;
        this.sigungu = sigungu;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
    }
}
