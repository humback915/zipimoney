package kr.zipimoney.domain.share.controller;

import kr.zipimoney.domain.share.entity.ShareCard;
import kr.zipimoney.domain.share.repository.ShareCardRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class SharePageController {

    private static final Pattern CRAWLER_PATTERN = Pattern.compile(
        "facebookexternalhit|Facebot|Twitterbot|kakaotalk-scrap|googlebot|bingbot|yandex|"
        + "LinkedInBot|Slackbot|Discordbot|WhatsApp|TelegramBot|PinterestBot",
        Pattern.CASE_INSENSITIVE
    );

    private final ShareCardRepository shareCardRepository;

    public SharePageController(ShareCardRepository shareCardRepository) {
        this.shareCardRepository = shareCardRepository;
    }

    @GetMapping("/s/{shareKey}")
    public String sharePage(
        @PathVariable String shareKey,
        HttpServletRequest request,
        Model model
    ) {
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("");

        if (!CRAWLER_PATTERN.matcher(userAgent).find()) {
            // Normal browser: let SPA handle it
            return "forward:/index.html";
        }

        // Crawler: serve Thymeleaf template with OG tags
        Optional<ShareCard> cardOpt = shareCardRepository.findByShareKey(shareKey);
        if (cardOpt.isEmpty()) {
            return "forward:/index.html";
        }

        ShareCard card = cardOpt.get();
        String baseUrl = request.getScheme() + "://" + request.getServerName()
            + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");

        String title = card.getSigungu() != null
            ? card.getSigungu() + " - 내 집 마련 계산기"
            : "ZIPIMONEY - 내 집 마련 계산기";

        String description = card.getMemeText() != null
            ? card.getMemeText()
            : "내 집 마련까지 얼마나 걸릴까?";

        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("imageUrl", baseUrl + "/og-image.png");
        model.addAttribute("url", baseUrl + "/s/" + shareKey);
        model.addAttribute("spaUrl", "/s/" + shareKey);

        return "share";
    }
}
