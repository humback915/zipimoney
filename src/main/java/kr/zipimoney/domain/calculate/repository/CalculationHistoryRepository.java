package kr.zipimoney.domain.calculate.repository;

import kr.zipimoney.domain.calculate.entity.CalculationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalculationHistoryRepository extends JpaRepository<CalculationHistory, Long> {

    /** 사용자 ID로 최근 계산 이력 20건 조회 */
    List<CalculationHistory> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
