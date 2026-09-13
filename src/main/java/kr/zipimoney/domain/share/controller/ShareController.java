package kr.zipimoney.domain.share.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.zipimoney.domain.share.dto.request.ShareCreateRequest;
import kr.zipimoney.domain.share.dto.response.ShareCardResponse;
import kr.zipimoney.domain.share.service.ShareService;
import kr.zipimoney.global.response.ApiResponse;
import kr.zipimoney.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Share", description = "공유 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/share")
public class ShareController {

    private final ShareService shareService;

    @Operation(summary = "공유 카드 생성", description = "계산 결과를 공유 카드로 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShareCardResponse> createShareCard(
            @CurrentUser Long userId,
            @Valid @RequestBody ShareCreateRequest request) {
        return ApiResponse.ok(shareService.createShareCard(userId, request));
    }

    @Operation(summary = "공유 카드 조회", description = "공유 키로 카드 조회 (조회수 증가)")
    @GetMapping("/{shareKey}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ShareCardResponse> getShareCard(@PathVariable String shareKey) {
        return ApiResponse.ok(shareService.getShareCard(shareKey));
    }
}
