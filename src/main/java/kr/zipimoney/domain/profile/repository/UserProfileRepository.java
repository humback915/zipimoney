package kr.zipimoney.domain.profile.repository;

import kr.zipimoney.domain.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /** 사용자 ID로 프로필 조회 */
    Optional<UserProfile> findByUserId(Long userId);
}
