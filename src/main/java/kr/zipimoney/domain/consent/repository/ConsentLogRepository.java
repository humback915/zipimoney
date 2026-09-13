package kr.zipimoney.domain.consent.repository;

import kr.zipimoney.domain.consent.entity.ConsentLog;
import kr.zipimoney.domain.consent.enums.ConsentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsentLogRepository extends JpaRepository<ConsentLog, Long> {

    /** 사용자 ID로 동의 유형별 최신 동의 내역 조회 */
    @Query("""
            SELECT c FROM ConsentLog c
            WHERE c.userId = :userId
              AND c.agreedAt = (
                  SELECT MAX(c2.agreedAt) FROM ConsentLog c2
                  WHERE c2.userId = c.userId AND c2.consentType = c.consentType
              )
            """)
    List<ConsentLog> findLatestConsentsByUserId(@Param("userId") Long userId);
}
