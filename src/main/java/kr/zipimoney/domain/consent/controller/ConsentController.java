package kr.zipimoney.domain.consent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.zipimoney.domain.consent.dto.request.ConsentRequest;
import kr.zipimoney.domain.consent.dto.response.ConsentResponse;
import kr.zipimoney.domain.consent.service.ConsentService;
import kr.zipimoney.global.response.ApiResponse;
import kr.zipimoney.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Consent", description = "동의 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consent")
public class ConsentController {

    private final ConsentService consentService;

    @Operation(summary = "동의 내역 조회", description = "현재 사용자의 최신 동의 내역")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ConsentResponse>> getConsents(@CurrentUser Long userId) {
        return ApiResponse.ok(consentService.getConsents(userId));
    }

    @Operation(summary = "동의 기록", description = "동의/철회 기록 (INSERT only)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ConsentResponse> recordConsent(
            @CurrentUser Long userId,
            @Valid @RequestBody ConsentRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        return ApiResponse.ok(consentService.recordConsent(userId, request, ipAddress, userAgent));
    }
}
