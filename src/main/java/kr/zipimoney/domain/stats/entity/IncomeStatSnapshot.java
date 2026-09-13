package kr.zipimoney.domain.stats.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "income_stat_snapshots",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_income_stat_period_lawd", columnNames = {"period", "lawdCd"})
        })
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IncomeStatSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 7, nullable = false)
    String period;

    @Column(length = 5)
    String lawdCd;

    @Column(nullable = false)
    int sampleSize;

    Long incomeP25;

    Long incomeP50;

    Long incomeP75;

    Integer avgMonths;

    @Column(precision = 5, scale = 4)
    BigDecimal unreachableRatio;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Builder
    private IncomeStatSnapshot(String period, String lawdCd, int sampleSize,
                               Long incomeP25, Long incomeP50, Long incomeP75,
                               Integer avgMonths, BigDecimal unreachableRatio) {
        this.period = period;
        this.lawdCd = lawdCd;
        this.sampleSize = sampleSize;
        this.incomeP25 = incomeP25;
        this.incomeP50 = incomeP50;
        this.incomeP75 = incomeP75;
        this.avgMonths = avgMonths;
        this.unreachableRatio = unreachableRatio;
    }
}
