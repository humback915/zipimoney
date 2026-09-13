package kr.zipimoney.domain.region.repository;

import kr.zipimoney.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, String> {

    /** 시도·시군구 이름순으로 전체 지역 목록 조회 */
    List<Region> findAllByOrderBySidoAscSigunguAsc();

    /** 법정동코드로 지역 조회 */
    Optional<Region> findByLawdCd(String lawdCd);

    /** 시군구명 키워드로 지역 검색 */
    List<Region> findBySigunguContaining(String sigungu);
}
