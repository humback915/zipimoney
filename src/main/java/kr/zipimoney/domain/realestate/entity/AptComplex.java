package kr.zipimoney.domain.realestate.entity;

import jakarta.persistence.*;
import kr.zipimoney.domain.realestate.enums.GeocodeStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "apt_complexes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_apt_complex_lawd_dong_name_type", columnNames = {"lawdCd", "dong", "name", "propertyType"})
        },
        indexes = {
                @Index(name = "idx_apt_complex_lawd", columnList = "lawdCd"),
                @Index(name = "idx_apt_complex_lat_lng", columnList = "lat, lng")
        })
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AptComplex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 5, nullable = false)
    String lawdCd;

    @Column(length = 50, nullable = false)
    String dong;

    @Column(length = 150, nullable = false)
    String name;

    Integer builtYear;

    @Setter
    @Column(precision = 10, scale = 7)
    BigDecimal lat;

    @Setter
    @Column(precision = 10, scale = 7)
    BigDecimal lng;

    @Column(length = 20, nullable = false)
    String propertyType = "apt";

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    GeocodeStatus geocodeStatus;

    @Setter
    LocalDateTime geocodedAt;

    @OneToMany(mappedBy = "complex", fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    List<AptTrade> trades = new ArrayList<>();

    @Builder
    private AptComplex(String lawdCd, String dong, String name, Integer builtYear,
                       BigDecimal lat, BigDecimal lng, GeocodeStatus geocodeStatus,
                       String propertyType) {
        this.lawdCd = lawdCd;
        this.dong = dong;
        this.name = name;
        this.builtYear = builtYear;
        this.lat = lat;
        this.lng = lng;
        this.geocodeStatus = geocodeStatus == null ? GeocodeStatus.PENDING : geocodeStatus;
        this.propertyType = propertyType == null ? "apt" : propertyType;
    }
}
