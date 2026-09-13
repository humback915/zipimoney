package kr.zipimoney.domain.profile.service;

import kr.zipimoney.domain.profile.dto.request.ProfileSaveRequest;
import kr.zipimoney.domain.profile.dto.response.ProfileResponse;
import kr.zipimoney.domain.profile.entity.UserProfile;
import kr.zipimoney.domain.profile.entity.UserProfileHistory;
import kr.zipimoney.domain.profile.repository.UserProfileHistoryRepository;
import kr.zipimoney.domain.profile.repository.UserProfileRepository;
import kr.zipimoney.domain.user.entity.User;
import kr.zipimoney.domain.user.repository.UserRepository;
import kr.zipimoney.global.crypto.AesGcmCryptoUtil;
import kr.zipimoney.global.exception.DomainException;
import kr.zipimoney.global.exception.DomainExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserProfileRepository profileRepository;
    private final UserProfileHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final AesGcmCryptoUtil cryptoUtil;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException(DomainExceptionCode.PROFILE_NOT_FOUND));

        Long annualIncome = profile.getAnnualIncomeEnc() != null
                ? cryptoUtil.decryptNumber(profile.getAnnualIncomeEnc()) : null;
        Long currentAssets = profile.getCurrentAssetsEnc() != null
                ? cryptoUtil.decryptNumber(profile.getCurrentAssetsEnc()) : null;

        return ProfileResponse.builder()
                .birthYear(profile.getBirthYear())
                .annualIncome(annualIncome)
                .currentAssets(currentAssets)
                .takeHomeRatio(profile.getTakeHomeRatio())
                .savingRate(profile.getSavingRate())
                .savingsApr(profile.getSavingsApr())
                .housePriceGrowth(profile.getHousePriceGrowth())
                .loanLtv(profile.getLoanLtv())
                .jobCategory(profile.getJobCategory())
                .build();
    }

    @Transactional
    public ProfileResponse saveProfile(Long userId, ProfileSaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(DomainExceptionCode.USER_NOT_FOUND));

        UserProfile profile = profileRepository.findByUserId(userId).orElse(null);

        byte[] incomeEnc = request.getAnnualIncome() != null
                ? cryptoUtil.encryptNumber(request.getAnnualIncome()) : null;
        byte[] assetsEnc = request.getCurrentAssets() != null
                ? cryptoUtil.encryptNumber(request.getCurrentAssets()) : null;

        if (profile == null) {
            profile = UserProfile.builder()
                    .userId(userId)
                    .birthYear(request.getBirthYear())
                    .annualIncomeEnc(incomeEnc)
                    .currentAssetsEnc(assetsEnc)
                    .takeHomeRatio(request.getTakeHomeRatio())
                    .savingRate(request.getSavingRate())
                    .savingsApr(request.getSavingsApr())
                    .housePriceGrowth(request.getHousePriceGrowth())
                    .loanLtv(request.getLoanLtv())
                    .jobCategory(request.getJobCategory())
                    .build();
            profileRepository.save(profile);
        } else {
            profile.setBirthYear(request.getBirthYear());
            profile.setAnnualIncomeEnc(incomeEnc);
            profile.setCurrentAssetsEnc(assetsEnc);
            profile.setTakeHomeRatio(request.getTakeHomeRatio());
            profile.setSavingRate(request.getSavingRate());
            profile.setSavingsApr(request.getSavingsApr());
            profile.setHousePriceGrowth(request.getHousePriceGrowth());
            profile.setLoanLtv(request.getLoanLtv());
            profile.setJobCategory(request.getJobCategory());
        }

        // 변경 이력 기록
        UserProfileHistory history = UserProfileHistory.builder()
                .userId(userId)
                .annualIncomeEnc(incomeEnc)
                .currentAssetsEnc(assetsEnc)
                .savingRate(request.getSavingRate())
                .loanLtv(request.getLoanLtv())
                .changedAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);

        return ProfileResponse.builder()
                .birthYear(request.getBirthYear())
                .annualIncome(request.getAnnualIncome())
                .currentAssets(request.getCurrentAssets())
                .takeHomeRatio(request.getTakeHomeRatio())
                .savingRate(request.getSavingRate())
                .savingsApr(request.getSavingsApr())
                .housePriceGrowth(request.getHousePriceGrowth())
                .loanLtv(request.getLoanLtv())
                .jobCategory(request.getJobCategory())
                .build();
    }
}
