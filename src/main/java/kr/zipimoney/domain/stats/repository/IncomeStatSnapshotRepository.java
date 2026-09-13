package kr.zipimoney.domain.stats.repository;

import kr.zipimoney.domain.stats.entity.IncomeStatSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeStatSnapshotRepository extends JpaRepository<IncomeStatSnapshot, Long> {
}
