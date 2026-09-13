package kr.zipimoney.domain.auth.service;

import kr.zipimoney.domain.auth.dto.response.UserResponse;
import kr.zipimoney.domain.profile.repository.UserProfileRepository;
import kr.zipimoney.domain.user.entity.User;
import kr.zipimoney.domain.user.enums.UserStatus;
import kr.zipimoney.domain.user.repository.UserRepository;
import kr.zipimoney.global.exception.DomainException;
import kr.zipimoney.global.exception.DomainExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public LoginResult loginOrRegister(String kakaoId, String name, String email,
                                       String gender, String ageRange) {
        User user = userRepository.findByKakaoId(kakaoId).orElse(null);
        boolean isNew = false;

        if (user == null) {
            user = User.builder()
                    .kakaoId(kakaoId)
                    .name(name)
                    .email(email)
                    .gender(gender)
                    .ageRange(ageRange)
                    .status(UserStatus.ACTIVE)
                    .build();
            user = userRepository.save(user);
            isNew = true;
        } else if (user.getStatus() == UserStatus.WITHDRAWN) {
            user.setStatus(UserStatus.ACTIVE);
            user.setWithdrawnAt(null);
            user.setName(name);
            user.setEmail(email);
            user.setGender(gender);
            user.setAgeRange(ageRange);
            isNew = true;
        } else {
            user.setName(name);
            user.setEmail(email);
            user.setGender(gender);
            user.setAgeRange(ageRange);
        }

        return new LoginResult(user.getId(), isNew);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = findActiveUser(userId);
        return UserResponse.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .nickname(user.getNickname())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .email(user.getEmail())
                .gender(user.getGender())
                .ageRange(user.getAgeRange())
                .build();
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = findActiveUser(userId);
        user.setStatus(UserStatus.WITHDRAWN);
        user.setWithdrawnAt(LocalDateTime.now());

        // 프로필 암호화 데이터 삭제
        userProfileRepository.findByUserId(userId).ifPresent(profile -> {
            profile.setAnnualIncomeEnc(null);
            profile.setCurrentAssetsEnc(null);
        });
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(DomainExceptionCode.USER_NOT_FOUND));
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new DomainException(DomainExceptionCode.USER_WITHDRAWN);
        }
        return user;
    }

    public record LoginResult(Long userId, boolean isNew) {}
}
