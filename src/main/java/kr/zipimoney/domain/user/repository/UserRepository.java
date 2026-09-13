package kr.zipimoney.domain.user.repository;

import kr.zipimoney.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** 카카오 ID로 사용자 조회 */
    Optional<User> findByKakaoId(String kakaoId);

    /** 카카오 ID로 사용자 존재 여부 확인 */
    boolean existsByKakaoId(String kakaoId);
}
