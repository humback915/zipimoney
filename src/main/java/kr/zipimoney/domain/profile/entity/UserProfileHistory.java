package kr.zipimoney.domain.profile.entity;

import jakarta.persistence.*;
import kr.zipimoney.domain.user.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile_histories", indexes = {
        @Index(name = "idx_profile_history_user_changed", columnList = "userId, changedAt DESC")
})
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long userId;

    @Column(columnDefinition = "bytea")
    byte[] annualIncomeEnc;

    @Column(columnDefinition = "bytea")
    byte[] currentAssetsEnc;

    @Column(precision = 4, scale = 3)
    BigDecimal savingRate;

    @Column(precision = 4, scale = 3)
    BigDecimal loanLtv;

    @Column(nullable = false)
    LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    User user;

    @PrePersist
    protected void onCreate() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }

    @Builder
    private UserProfileHistory(Long userId, byte[] annualIncomeEnc, byte[] currentAssetsEnc,
                               BigDecimal savingRate, BigDecimal loanLtv, LocalDateTime changedAt) {
        this.userId = userId;
        this.annualIncomeEnc = annualIncomeEnc;
        this.currentAssetsEnc = currentAssetsEnc;
        this.savingRate = savingRate;
        this.loanLtv = loanLtv;
        this.changedAt = changedAt;
    }
}
