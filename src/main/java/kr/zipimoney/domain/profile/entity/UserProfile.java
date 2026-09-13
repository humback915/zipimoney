package kr.zipimoney.domain.profile.entity;

import jakarta.persistence.*;
import kr.zipimoney.domain.user.entity.User;
import kr.zipimoney.global.entity.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;

@Entity
@Table(name = "user_profiles")
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    Long userId;

    @Setter
    Integer birthYear;

    @Setter
    @Column(columnDefinition = "bytea")
    byte[] annualIncomeEnc;

    @Setter
    @Column(columnDefinition = "bytea")
    byte[] currentAssetsEnc;

    @Setter
    @Column(precision = 4, scale = 3)
    BigDecimal takeHomeRatio;

    @Setter
    @Column(precision = 4, scale = 3)
    BigDecimal savingRate;

    @Setter
    @Column(precision = 5, scale = 4)
    BigDecimal savingsApr;

    @Setter
    @Column(precision = 5, scale = 4)
    BigDecimal housePriceGrowth;

    @Setter
    @Column(precision = 4, scale = 3)
    BigDecimal loanLtv;

    @Setter
    @Column(length = 40)
    String jobCategory;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    User user;

    @Builder
    private UserProfile(Long userId, Integer birthYear, byte[] annualIncomeEnc,
                        byte[] currentAssetsEnc, BigDecimal takeHomeRatio, BigDecimal savingRate,
                        BigDecimal savingsApr, BigDecimal housePriceGrowth, BigDecimal loanLtv,
                        String jobCategory) {
        this.userId = userId;
        this.birthYear = birthYear;
        this.annualIncomeEnc = annualIncomeEnc;
        this.currentAssetsEnc = currentAssetsEnc;
        this.takeHomeRatio = takeHomeRatio == null ? new BigDecimal("0.840") : takeHomeRatio;
        this.savingRate = savingRate;
        this.savingsApr = savingsApr == null ? new BigDecimal("0.0300") : savingsApr;
        this.housePriceGrowth = housePriceGrowth == null ? new BigDecimal("0.0200") : housePriceGrowth;
        this.loanLtv = loanLtv == null ? new BigDecimal("0.000") : loanLtv;
        this.jobCategory = jobCategory;
    }
}
