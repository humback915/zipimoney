package kr.zipimoney.domain.realestate.repository;

import kr.zipimoney.domain.realestate.entity.AptTrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AptTradeRepository extends JpaRepository<AptTrade, Long> {

    /** 단지 ID와 기간으로 취소되지 않은 아파트 거래 목록 조회 */
    List<AptTrade> findAllByComplexIdAndDealDateBetweenAndCanceledFalse(
            Long complexId, LocalDate startDate, LocalDate endDate);

    /** 원본 해시값으로 거래 데이터 존재 여부 확인 */
    boolean existsBySourceHash(String sourceHash);
}
