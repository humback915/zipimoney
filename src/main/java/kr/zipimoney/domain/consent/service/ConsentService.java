package kr.zipimoney.domain.consent.service;

import kr.zipimoney.domain.consent.dto.request.ConsentRequest;
import kr.zipimoney.domain.consent.dto.response.ConsentResponse;
import kr.zipimoney.domain.consent.entity.ConsentLog;
import kr.zipimoney.domain.consent.enums.ConsentType;
import kr.zipimoney.domain.consent.repository.ConsentLogRepository;
import kr.zipimoney.domain.user.entity.User;
import kr.zipimoney.domain.user.repository.UserRepository;
import kr.zipimoney.global.exception.DomainException;
import kr.zipimoney.global.exception.DomainExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentLogRepository consentLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ConsentResponse> getConsents(Long userId) {
        List<ConsentLog> logs = consentLogRepository.findLatestConsentsByUserId(userId);
        return logs.stream()
                .map(log -> ConsentResponse.builder()
                        .consentType(log.getConsentType().name())
                        .termsVersion(log.getTermsVersion())
                        .agreed(log.isAgreed())
                        .agreedAt(log.getAgreedAt())
                        .build())
                .toList();
    }

    @Transactional
    public ConsentResponse recordConsent(Long userId, ConsentRequest request,
                                         String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(DomainExceptionCode.USER_NOT_FOUND));

        ConsentType consentType;
        try {
            consentType = ConsentType.valueOf(request.getConsentType());
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainExceptionCode.INVALID_CONSENT_TYPE);
        }

        ConsentLog consentLog = ConsentLog.builder()
                .userId(userId)
                .consentType(consentType)
                .termsVersion(request.getTermsVersion())
                .agreed(request.getAgreed())
                .agreedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        consentLogRepository.save(consentLog);

        return ConsentResponse.builder()
                .consentType(consentType.name())
                .termsVersion(request.getTermsVersion())
                .agreed(request.getAgreed())
                .agreedAt(consentLog.getAgreedAt())
                .build();
    }
}
