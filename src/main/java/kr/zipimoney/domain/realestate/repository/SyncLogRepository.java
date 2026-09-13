package kr.zipimoney.domain.realestate.repository;

import kr.zipimoney.domain.realestate.entity.SyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {

    /** 법정동코드와 거래년월로 동기화 로그 조회 */
    Optional<SyncLog> findByLawdCdAndDealYmd(String lawdCd, String dealYmd);
}
