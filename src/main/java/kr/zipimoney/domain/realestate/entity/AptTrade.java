package kr.zipimoney.domain.realestate.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "apt_trades", indexes = {
        @Index(name = "idx_apt_trade_complex_date", columnList = "complexId, dealDate DESC")
})
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AptTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long complexId;

    @Column(precision = 7, scale = 2)
    BigDecimal exclusiveArea;

    Integer floor;

    @Column(nullable = false)
    Long dealAmount;

    @Column(nullable = false)
    LocalDate dealDate;

    @Column(nullable = false)
    boolean canceled;

    @Column(length = 64, unique = true, nullable = false)
    String sourceHash;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complexId", insertable = false, updatable = false)
    AptComplex complex;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Builder
    private AptTrade(Long complexId, BigDecimal exclusiveArea, Integer floor,
                     Long dealAmount, LocalDate dealDate, boolean canceled, String sourceHash) {
        this.complexId = complexId;
        this.exclusiveArea = exclusiveArea;
        this.floor = floor;
        this.dealAmount = dealAmount;
        this.dealDate = dealDate;
        this.canceled = canceled;
        this.sourceHash = sourceHash;
    }
}
