package kr.zipimoney.domain.share.entity;

import jakarta.persistence.*;
import kr.zipimoney.domain.user.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "share_cards", indexes = {
        @Index(name = "idx_share_card_user_created", columnList = "userId, createdAt DESC")
})
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShareCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 22, unique = true, nullable = false)
    String shareKey;

    Long userId;

    @Column(length = 50, nullable = false)
    String sigungu;

    @Column(precision = 7, scale = 2)
    BigDecimal exclusiveArea;

    Integer totalMonths;

    @Column(nullable = false)
    boolean reachable;

    @Column(length = 200, nullable = false)
    String memeText;

    @Column(length = 500)
    String imageUrl;

    @Setter
    @Column(nullable = false)
    int viewCount;

    LocalDateTime expiresAt;

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
    private ShareCard(String shareKey, Long userId, String sigungu,
                      BigDecimal exclusiveArea, Integer totalMonths, boolean reachable,
                      String memeText, String imageUrl, int viewCount, LocalDateTime expiresAt) {
        this.shareKey = shareKey;
        this.userId = userId;
        this.sigungu = sigungu;
        this.exclusiveArea = exclusiveArea;
        this.totalMonths = totalMonths;
        this.reachable = reachable;
        this.memeText = memeText;
        this.imageUrl = imageUrl;
        this.viewCount = viewCount;
        this.expiresAt = expiresAt;
    }
}
