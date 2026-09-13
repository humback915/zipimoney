import { useRef, useCallback, useMemo, useState } from 'react';
import { toPng } from 'html-to-image';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
} from 'recharts';
import type { CalcResult } from '../lib/types';

interface Props {
  result: CalcResult;
  complexName: string;
  regionName?: string;
  onClose: () => void;
}

function formatWon(v: number): string {
  const eok = Math.floor(v / 100_000_000);
  const man = Math.round((v % 100_000_000) / 10_000);
  if (eok > 0 && man > 0) return `${eok}억 ${man.toLocaleString()}만원`;
  if (eok > 0) return `${eok}억원`;
  return `${man.toLocaleString()}만원`;
}

export default function ResultView({
  result,
  complexName,
  regionName,
  onClose,
}: Props) {
  const cardRef = useRef<HTMLDivElement>(null);
  const [shareUrl, setShareUrl] = useState<string | null>(null);
  const [sharing, setSharing] = useState(false);

  const chartData = useMemo(() => {
    if (!result.breakdown.length) return [];
    const step = Math.max(1, Math.floor(result.breakdown.length / 20));
    return result.breakdown
      .filter((_, i) => i % step === 0 || i === result.breakdown.length - 1)
      .map((pt) => ({
        label: pt.month >= 12 ? `${Math.floor(pt.month / 12)}년` : `${pt.month}개월`,
        assets: Math.round(pt.assets),
        target: Math.round(pt.target),
      }));
  }, [result.breakdown]);

  const handleShare = useCallback(async () => {
    if (!cardRef.current) return;

    try {
      const dataUrl = await toPng(cardRef.current, {
        backgroundColor: '#ffffff',
        pixelRatio: 2,
      });

      // Web Share API with file
      if (navigator.share && navigator.canShare) {
        const blob = await (await fetch(dataUrl)).blob();
        const file = new File([blob], 'zipimoney-result.png', {
          type: 'image/png',
        });
        const shareData = { files: [file] };

        if (navigator.canShare(shareData)) {
          await navigator.share({
            ...shareData,
            title: 'ZIPIMONEY 결과',
            text: result.reachable
              ? `${complexName} 사려면 ${result.years}년 ${result.restMonths}개월!`
              : `${complexName}은(는) 영원히 못 산다네요...`,
          });
          return;
        }
      }

      // 폴백: 이미지 다운로드
      const link = document.createElement('a');
      link.download = 'zipimoney-result.png';
      link.href = dataUrl;
      link.click();
    } catch {
      // 공유 취소 또는 실패 - 텍스트 폴백
      const text = result.reachable
        ? `${complexName} 사려면 ${result.years}년 ${result.restMonths}개월 걸린대요... (ZIPIMONEY)`
        : `${complexName}은(는) 영원히 못 산다네요... (ZIPIMONEY)`;
      await navigator.clipboard.writeText(text);
      alert('결과가 클립보드에 복사되었습니다!');
    }
  }, [result, complexName]);

  return (
    <div
      className="fixed inset-0 z-50 bg-black/40 flex items-end sm:items-center justify-center"
      role="dialog"
      aria-modal="true"
      aria-label="계산 결과"
    >
      <div
        ref={cardRef}
        className="bg-white w-full sm:max-w-md rounded-t-3xl sm:rounded-3xl
                   max-h-[90vh] overflow-y-auto dark:bg-gray-900 dark:text-white"
      >
        {/* 헤더 */}
        <div className="px-5 pt-5 pb-3 flex justify-between items-start">
          <div>
            <p className="text-sm text-gray-500 dark:text-gray-400">{complexName}</p>
            <p className="text-sm text-gray-400 dark:text-gray-500">사려면?</p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 p-1"
            aria-label="닫기"
          >
            ✕
          </button>
        </div>

        {/* 메인 결과 */}
        <div className="px-5 py-6 text-center">
          {result.reachable ? (
            <>
              <p className="text-5xl font-black text-brand-500 dark:text-brand-400">
                {result.years > 0 && `${result.years}년 `}
                {result.restMonths > 0 && `${result.restMonths}개월`}
                {result.totalMonths === 0 && '지금 당장!'}
              </p>
              {result.totalMonths > 0 && (
                <p className="text-lg font-semibold text-gray-700 dark:text-gray-300 mt-2">
                  저축해야 합니다
                </p>
              )}
              <p className="text-gray-500 dark:text-gray-400 mt-2 text-sm">
                월 <span className="font-semibold">{formatWon(result.monthlySaving)}</span> 저축 ·
                총 <span className="font-semibold">{formatWon(result.monthlySaving * result.totalMonths)}</span>
              </p>
            </>
          ) : (
            <>
              <p className="text-4xl font-black text-red-500 dark:text-red-400">
                영원히 못 삽니다
              </p>
              <p className="text-gray-500 dark:text-gray-400 mt-3">
                집값 상승 속도가 저축 속도보다 빠릅니다
              </p>
            </>
          )}
        </div>

        {/* 상세 정보 */}
        {result.reachable && result.totalMonths > 0 && (
          <div className="px-5 pb-4">
            <div className="bg-gray-50 dark:bg-gray-800 rounded-2xl p-4 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500 dark:text-gray-400">필요 자기자본</span>
                <span className="font-medium">{formatWon(result.requiredEquity)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500 dark:text-gray-400">이자 수익</span>
                <span className="font-medium text-green-600 dark:text-green-400">
                  +{formatWon(result.totalInterest)}
                </span>
              </div>
            </div>
          </div>
        )}

        {/* 자산 vs 집값 그래프 */}
        {result.reachable && result.breakdown.length > 1 && (
          <div className="px-5 pb-4">
            <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              자산 vs 목표 추격 그래프
            </h3>
            <div className="bg-gray-50 dark:bg-gray-800 rounded-2xl p-4">
              <ResponsiveContainer width="100%" height={180}>
                <LineChart data={chartData}>
                  <XAxis
                    dataKey="label"
                    tick={{ fontSize: 11 }}
                    interval="preserveStartEnd"
                  />
                  <YAxis
                    tickFormatter={(v: number) => `${(v / 100_000_000).toFixed(1)}억`}
                    tick={{ fontSize: 11 }}
                    width={50}
                  />
                  <Tooltip
                    formatter={(value: number, name: string) => [
                      formatWon(value),
                      name,
                    ]}
                    labelFormatter={(label: string) => label}
                  />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="assets"
                    name="내 자산"
                    stroke="#00A650"
                    strokeWidth={2}
                    dot={false}
                  />
                  <Line
                    type="monotone"
                    dataKey="target"
                    name="필요 금액"
                    stroke="#EF4444"
                    strokeWidth={2}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}

        {/* 재미 카드 */}
        <div className="px-5 pb-4">
          <div className="bg-amber-50 dark:bg-amber-900/30 rounded-2xl p-4 space-y-2">
            <p className="text-sm font-medium text-amber-800 dark:text-amber-300">
              {result.fun.memeText}
            </p>
            <div className="text-sm text-amber-700 dark:text-amber-400 space-y-1">
              {result.fun.ageWhenDone != null && (
                <p>다 모았을 때 내 나이: <strong>{result.fun.ageWhenDone}세</strong></p>
              )}
              <p>메가커피 아메리카노: <strong>{result.fun.megaCoffee.toLocaleString()}</strong>잔</p>
              <p>스타벅스 아메리카노: <strong>{result.fun.starbucksCoffee.toLocaleString()}</strong>잔</p>
              <p>BBQ 황금올리브치킨: <strong>{result.fun.bbqChicken.toLocaleString()}</strong>마리</p>
            </div>
          </div>
        </div>

        {/* 하단 버튼 + 고지 */}
        <div className="px-5 pb-5">
          <div className="flex gap-2 mb-3">
            <button
              onClick={handleShare}
              className="flex-1 py-3 bg-gray-800 dark:bg-gray-200 text-white dark:text-gray-900
                         font-semibold rounded-2xl hover:bg-gray-900 dark:hover:bg-gray-300
                         transition-colors"
            >
              이미지 저장
            </button>
            <button
              onClick={async () => {
                if (shareUrl) {
                  await navigator.clipboard.writeText(shareUrl);
                  alert('링크가 복사되었습니다!');
                  return;
                }
                setSharing(true);
                try {
                  const sigungu = regionName || complexName;
                  const res = await fetch('/api/share', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({
                      sigungu,
                      totalMonths: result.reachable ? result.totalMonths : null,
                      reachable: result.reachable,
                      memeText: result.fun.memeText,
                    }),
                  });
                  if (res.ok) {
                    const data = await res.json();
                    setShareUrl(data.url);
                    await navigator.clipboard.writeText(data.url);
                    alert('공유 링크가 복사되었습니다!');
                  }
                } catch { /* ignore */ } finally {
                  setSharing(false);
                }
              }}
              disabled={sharing}
              className="flex-1 py-3 bg-brand-500 text-white font-semibold rounded-2xl
                         hover:bg-brand-700 transition-colors disabled:opacity-50"
            >
              {sharing ? '생성 중...' : shareUrl ? '링크 복사' : '링크 공유'}
            </button>
          </div>
          <p className="text-[10px] text-gray-400 dark:text-gray-500 text-center leading-relaxed">
            본 앱은 재미용이며 실제 투자·대출 판단의 근거가 될 수 없습니다.
            실거래가는 국토교통부 공개 데이터를 기반으로 하며 실제 시세와 다를 수 있습니다.
          </p>
        </div>
      </div>
    </div>
  );
}
