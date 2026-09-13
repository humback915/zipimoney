package kr.zipimoney.domain.calculate.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MemeTextGenerator {

    public String generate(String sigungu, Double area, int totalMonths, boolean reachable, Integer age) {
        String loc = (sigungu != null && !sigungu.isBlank()) ? sigungu : "이 동네";
        String pyeong = area != null ? Math.round(area / 3.3058) + "평" : "";

        if (!reachable) {
            return pick(List.of(
                    loc + "에서 내 집 마련? 다음 생에 도전하세요.",
                    "저축 속도보다 집값이 빠릅니다. 자전거로 KTX를 추월할 순 없어요.",
                    "이 속도로는 영원히 못 삽니다. 전세를 진지하게 고려해보세요.",
                    "집값이 로켓이고 저축은 자전거입니다. 현실적인 지역을 알아보세요."
            ));
        }

        if (totalMonths == 0) {
            return pick(List.of(
                    "축하합니다! 지금 당장 계약서에 도장 찍으세요.",
                    "이미 충분합니다. 뭘 망설이고 계신가요?",
                    loc + (!pyeong.isEmpty() ? " " + pyeong : "") + ", 오늘이 가장 싼 날입니다."
            ));
        }

        int yrs = (int) Math.ceil(totalMonths / 12.0);

        if (totalMonths <= 12) {
            return pick(List.of(
                    "1년도 안 걸립니다! 커피 좀 줄이면 더 빨라요.",
                    "올해 안에 가능합니다. 연말 집들이 계획 세우세요.",
                    "곧입니다! " + loc + (!pyeong.isEmpty() ? " " + pyeong : "") + "이 기다리고 있어요."
            ));
        }

        if (totalMonths <= 36) {
            return pick(List.of(
                    yrs + "년이면 금방입니다. 적금 들고 버티세요.",
                    loc + " 입성까지 " + yrs + "년. 충분히 할 만합니다.",
                    "전세 한 번만 더 버티면 됩니다. 파이팅!"
            ));
        }

        if (totalMonths <= 60) {
            return pick(List.of(
                    yrs + "년이면 강산이 반은 변하죠. 목표가 있으니 버틸 수 있습니다.",
                    "마라톤이지 단거리가 아닙니다. 페이스 유지하세요.",
                    age != null
                            ? (age + yrs) + "살의 나에게 미리 축하를 보내세요."
                            : yrs + "년 후의 나에게 미리 축하를 보내세요."
            ));
        }

        if (totalMonths <= 120) {
            return pick(List.of(
                    yrs + "년 계획이 필요합니다. 하지만 불가능하진 않아요.",
                    "초등학생이 대학 갈 때쯤이면 됩니다. 긴 호흡으로 가세요.",
                    "연봉 인상이 있다면 더 빨라질 수 있습니다. 이직도 방법이에요."
            ));
        }

        if (totalMonths <= 240) {
            return pick(List.of(
                    yrs + "년이면 청춘이 통째로 날아갑니다. 전세는 어떠세요?",
                    "대출 만기와 비슷한 기간입니다. 현실적인 대안을 고민해보세요.",
                    "인생은 한 번뿐입니다. 좀 더 현실적인 목표를 세워보는 건 어떨까요?"
            ));
        }

        if (totalMonths <= 360) {
            return pick(List.of(
                    yrs + "년... 은퇴 후에나 가능합니다. 목표를 재설정하세요.",
                    "이 기간이면 화성 이주가 더 빠를 수도 있습니다.",
                    "로또 당첨 확률을 계산해보는 게 더 현실적일 수 있습니다."
            ));
        }

        return pick(List.of(
                "사실상 불가능합니다. 로또 1등 당첨 확률은 1/8,145,060입니다.",
                yrs + "년 넘게 걸립니다. 영생을 먼저 연구하세요.",
                "이 집은 포기하고 다른 지역을 알아보시는 게 낫겠습니다."
        ));
    }

    private static String pick(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
