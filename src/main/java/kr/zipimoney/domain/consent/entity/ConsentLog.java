package kr.zipimoney.domain.consent.entity;

import jakarta.persistence.*;
import kr.zipimoney.domain.consent.enums.ConsentType;
import kr.zipimoney.domain.user.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "consent_logs", indexes = {
        @Index(name = "idx_consent_user_type_agreed", columnList = "userId, consentType, agreedAt DESC")
})
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ConsentType consentType;

    @Column(length = 20)
    String termsVersion;

    @Column(nullable = false)
    boolean agreed;

    @Column(nullable = false)
    LocalDateTime agreedAt;

    @Column(length = 45)
    String ipAddress;

    @Column(length = 300)
    String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    User user;

    @PrePersist
    protected void onCreate() {
        if (this.agreedAt == null) {
            this.agreedAt = LocalDateTime.now();
        }
    }

    @Builder
    private ConsentLog(Long userId, ConsentType consentType, String termsVersion,
                       boolean agreed, LocalDateTime agreedAt, String ipAddress, String userAgent) {
        this.userId = userId;
        this.consentType = consentType;
        this.termsVersion = termsVersion;
        this.agreed = agreed;
        this.agreedAt = agreedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}
