package kr.zipimoney.domain.share.repository;

import kr.zipimoney.domain.share.entity.ShareCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareCardRepository extends JpaRepository<ShareCard, Long> {

    /** 공유 키로 공유 카드 조회 */
    Optional<ShareCard> findByShareKey(String shareKey);
}
