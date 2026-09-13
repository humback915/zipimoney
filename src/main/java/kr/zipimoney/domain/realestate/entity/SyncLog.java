package kr.zipimoney.domain.realestate.entity;

import jakarta.persistence.*;
import kr.zipimoney.domain.realestate.enums.SyncStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "sync_logs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_sync_log_lawd_ymd", columnNames = {"lawdCd", "dealYmd"})
        })
@Getter
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 5, nullable = false)
    String lawdCd;

    @Column(length = 6, nullable = false)
    String dealYmd;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SyncStatus status;

    @Setter
    Integer rowCount;

    @Setter
    @Column(length = 500)
    String errorMessage;

    @Setter
    @Column(nullable = false)
    LocalDateTime syncedAt;

    @PrePersist
    protected void onCreate() {
        if (this.syncedAt == null) {
            this.syncedAt = LocalDateTime.now();
        }
    }

    @Builder
    private SyncLog(String lawdCd, String dealYmd, SyncStatus status,
                    Integer rowCount, String errorMessage) {
        this.lawdCd = lawdCd;
        this.dealYmd = dealYmd;
        this.status = status;
        this.rowCount = rowCount;
        this.errorMessage = errorMessage;
    }
}
