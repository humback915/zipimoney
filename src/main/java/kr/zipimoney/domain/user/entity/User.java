package kr.zipimoney.domain.user.entity;

import jakarta.persistence.*;
import kr.zipimoney.domain.calculate.entity.CalculationHistory;
import kr.zipimoney.domain.consent.entity.ConsentLog;
import kr.zipimoney.domain.profile.entity.UserProfile;
import kr.zipimoney.domain.profile.entity.UserProfileHistory;
import kr.zipimoney.domain.share.entity.ShareCard;
import kr.zipimoney.domain.user.enums.UserStatus;
import kr.zipimoney.global.entity.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String kakaoId;

    @Setter
    @Column(length = 50)
    String nickname;

    @Setter
    @Column(length = 50)
    String name;

    @Setter
    @Column(length = 500)
    String profileImage;

    @Setter
    @Column(length = 100)
    String email;

    @Setter
    @Column(length = 10)
    String gender;

    @Setter
    @Column(length = 10)
    String ageRange;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    UserStatus status;

    @Setter
    LocalDateTime withdrawnAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    List<ConsentLog> consents = new ArrayList<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    UserProfile profile;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    List<UserProfileHistory> histories = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    List<CalculationHistory> calculations = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    List<ShareCard> shareCards = new ArrayList<>();

    @Builder
    private User(String kakaoId, String nickname, String name, String profileImage,
                 String email, String gender, String ageRange, UserStatus status) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.name = name;
        this.profileImage = profileImage;
        this.email = email;
        this.gender = gender;
        this.ageRange = ageRange;
        this.status = status == null ? UserStatus.ACTIVE : status;
    }
}
