package kr.zipimoney.domain.share.service;

import kr.zipimoney.domain.share.dto.request.ShareCreateRequest;
import kr.zipimoney.domain.share.dto.response.ShareCardResponse;
import kr.zipimoney.domain.share.entity.ShareCard;
import kr.zipimoney.domain.share.repository.ShareCardRepository;
import kr.zipimoney.global.exception.DomainException;
import kr.zipimoney.global.exception.DomainExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ShareService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final ShareCardRepository shareCardRepository;

    @Transactional
    public ShareCardResponse createShareCard(Long userId, ShareCreateRequest request) {
        String shareKey = generateShareKey();

        ShareCard card = ShareCard.builder()
                .shareKey(shareKey)
                .userId(userId)
                .sigungu(request.getSigungu())
                .exclusiveArea(request.getExclusiveArea())
                .totalMonths(request.getTotalMonths())
                .reachable(request.getReachable())
                .memeText(request.getMemeText())
                .viewCount(0)
                .build();

        shareCardRepository.save(card);

        return toResponse(card);
    }

    @Transactional
    public ShareCardResponse getShareCard(String shareKey) {
        ShareCard card = shareCardRepository.findByShareKey(shareKey)
                .orElseThrow(() -> new DomainException(DomainExceptionCode.SHARE_CARD_NOT_FOUND));

        card.setViewCount(card.getViewCount() + 1);

        return toResponse(card);
    }

    private ShareCardResponse toResponse(ShareCard card) {
        return ShareCardResponse.builder()
                .shareKey(card.getShareKey())
                .sigungu(card.getSigungu())
                .exclusiveArea(card.getExclusiveArea())
                .totalMonths(card.getTotalMonths())
                .reachable(card.isReachable())
                .memeText(card.getMemeText())
                .imageUrl(card.getImageUrl())
                .viewCount(card.getViewCount())
                .build();
    }

    private String generateShareKey() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
