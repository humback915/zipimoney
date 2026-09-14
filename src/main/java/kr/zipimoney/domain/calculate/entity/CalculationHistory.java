package kr.zipimoney.domain.calculate.entity;

import jakarta.persistence.*;
import kr.zipimoney.domain.user.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculation_histories", indexes = {
        @Index(name = "idx_calc_history_user_created", columnList = "userId, createdAt DESC"),
        @Index(name = "idx_calc_history_lawd_created", columnList = "lawdCd, createdAt DESC")
})
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CalculationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long userId;

    Long complexId;

    @Column(length = 5, nullable = false)
    String lawdCd;

    @Column(precision = 7, scale = 2)
    BigDecimal exclusiveArea;

    @Column(nullable = false)
    Long housePrice;

    Integer totalMonths;

    @Column(nullable = false)
    boolean reachable;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    String inputsJson;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    User user;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Builder
    private CalculationHistory(Long userId, Long complexId, String lawdCd,
                               BigDecimal exclusiveArea, Long housePrice, Integer totalMonths,
                               boolean reachable, String inputsJson) {
        this.userId = userId;
        this.complexId = complexId;
        this.lawdCd = lawdCd;
        this.exclusiveArea = exclusiveArea;
        this.housePrice = housePrice;
        this.totalMonths = totalMonths;
        this.reachable = reachable;
        this.inputsJson = inputsJson;
    }
}
