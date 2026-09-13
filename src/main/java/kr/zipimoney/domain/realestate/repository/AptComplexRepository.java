package kr.zipimoney.domain.realestate.repository;

import kr.zipimoney.domain.realestate.entity.AptComplex;
import kr.zipimoney.domain.realestate.enums.GeocodeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AptComplexRepository extends JpaRepository<AptComplex, Long> {

    /** 법정동코드·동·단지명으로 아파트 단지 조회 */
    Optional<AptComplex> findByLawdCdAndDongAndName(String lawdCd, String dong, String name);

    /** 법정동코드·동·단지명·매물유형으로 아파트 단지 조회 */
    Optional<AptComplex> findByLawdCdAndDongAndNameAndPropertyType(String lawdCd, String dong, String name, String propertyType);

    /** 법정동코드로 전체 아파트 단지 목록 조회 */
    List<AptComplex> findAllByLawdCd(String lawdCd);

    /** 법정동코드와 매물유형으로 아파트 단지 목록 조회 */
    List<AptComplex> findAllByLawdCdAndPropertyType(String lawdCd, String propertyType);

    /** 법정동코드와 지오코드 상태로 아파트 단지 목록 조회 */
    List<AptComplex> findAllByLawdCdAndGeocodeStatus(String lawdCd, GeocodeStatus geocodeStatus);
}
