package kr.zipimoney.domain.profile.repository;

import kr.zipimoney.domain.profile.entity.UserProfileHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProfileHistoryRepository extends JpaRepository<UserProfileHistory, Long> {

    /** 사용자 ID로 최근 프로필 변경 이력 20건 조회 */
    List<UserProfileHistory> findTop20ByUserIdOrderByChangedAtDesc(Long userId);
}
