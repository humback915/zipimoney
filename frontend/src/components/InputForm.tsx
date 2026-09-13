import { useState, useCallback } from 'react';

interface InputValues {
  annualIncome: number;
  takeHomeRatio: number;
  savingRate: number;
  currentAssets: number;
  savingsApr: number;
  housePriceGrowth: number;
  loanLtv: number;
  birthYear?: number;
}

interface Props {
  values: InputValues;
  housePrice: number;
  onChange: (partial: Partial<InputValues>) => void;
  onSubmit: () => void;
  onClose: () => void;
}

function formatWon(v: number): string {
  if (v >= 100_000_000) {
    const eok = v / 100_000_000;
    return `${eok.toFixed(eok % 1 === 0 ? 0 : 1)}억`;
  }
  if (v >= 10_000) return `${(v / 10_000).toLocaleString()}만`;
  return v.toLocaleString();
}

/** 숫자를 천 단위 콤마 문자열로 변환 */
function toComma(v: number): string {
  return v.toLocaleString('ko-KR');
}

/** 천 단위 콤마가 붙는 금액 입력 필드 */
function WonInput({
  value,
  onChange,
  label,
}: {
  value: number;
  onChange: (v: number) => void;
  label?: string;
}) {
  const [text, setText] = useState(toComma(value));
  const [focused, setFocused] = useState(false);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const raw = e.target.value.replace(/[^0-9]/g, '');
      const num = Number(raw) || 0;
      setText(raw ? toComma(num) : '');
      onChange(num);
    },
    [onChange],
  );

  // 포커스 해제 시 최신 value로 동기화
  const handleBlur = useCallback(() => {
    setFocused(false);
    setText(toComma(value));
  }, [value]);

  // 외부 value 변경 반영 (포커스 중이 아닐 때)
  if (!focused && toComma(value) !== text) {
    setText(toComma(value));
  }

  return (
    <div className="relative">
      <input
        type="text"
        inputMode="numeric"
        value={text}
        onChange={handleChange}
        onFocus={() => setFocused(true)}
        onBlur={handleBlur}
        aria-label={label}
        className="w-full px-3 py-2.5
                   rounded-xl text-right pr-8 text-base font-medium
                   text-gray-900 dark:text-white
                   bg-gray-50 dark:bg-gray-800
                   focus:outline-none focus:ring-2 focus:ring-brand-500"
      />
      <span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-gray-400 dark:text-gray-500">
        원
      </span>
    </div>
  );
}

export default function InputForm({
  values,
  housePrice,
  onChange,
  onSubmit,
  onClose,
}: Props) {
  const [incomeMode, setIncomeMode] = useState<'annual' | 'monthly'>('annual');
  const [showAdvanced, setShowAdvanced] = useState(true);

  const displayIncome =
    incomeMode === 'annual'
      ? values.annualIncome
      : Math.round(values.annualIncome / 12);

  const handleIncomeChange = useCallback(
    (v: number) => {
      onChange({
        annualIncome: incomeMode === 'annual' ? v : v * 12,
      });
    },
    [onChange, incomeMode],
  );

  return (
    <div className="fixed inset-0 z-50 bg-black/40 flex items-end sm:items-center justify-center">
      <div className="bg-white dark:bg-gray-900 w-full sm:max-w-md rounded-t-3xl sm:rounded-3xl
                      max-h-[85vh] overflow-y-auto">
        {/* 헤더 */}
        <div className="sticky top-0 bg-white dark:bg-gray-900 px-5 pt-5 pb-4 z-10">
          <div className="flex justify-between items-center">
            <h2 className="text-lg font-bold dark:text-white">내 조건 입력</h2>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
              ✕
            </button>
          </div>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            목표 집값: <span className="font-semibold text-brand-500 dark:text-brand-400">{formatWon(housePrice)}</span>원
          </p>
        </div>

        <div className="px-5 py-4 space-y-5">
          {/* 연봉/월급 */}
          <div>
            <div className="flex items-center gap-2 mb-2">
              <label className="text-sm font-medium dark:text-gray-200">소득</label>
              <div className="flex bg-gray-100 dark:bg-gray-700 rounded-xl p-0.5 text-xs">
                <button
                  onClick={() => setIncomeMode('annual')}
                  className={`px-2 py-1 rounded-md transition-colors ${
                    incomeMode === 'annual'
                      ? 'bg-white dark:bg-gray-600 shadow-sm font-medium dark:text-white'
                      : 'text-gray-500 dark:text-gray-400'
                  }`}
                >
                  연봉
                </button>
                <button
                  onClick={() => setIncomeMode('monthly')}
                  className={`px-2 py-1 rounded-md transition-colors ${
                    incomeMode === 'monthly'
                      ? 'bg-white dark:bg-gray-600 shadow-sm font-medium dark:text-white'
                      : 'text-gray-500 dark:text-gray-400'
                  }`}
                >
                  월급
                </button>
              </div>
            </div>
            <WonInput
              value={displayIncome}
              onChange={handleIncomeChange}
              label={incomeMode === 'annual' ? '세전 연봉' : '세전 월급'}
            />
            <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
              {incomeMode === 'annual' ? '세전 연봉' : '세전 월급'}
            </p>
          </div>

          {/* 저축률 */}
          <div>
            <label className="text-sm font-medium dark:text-gray-200">
              저축률: {Math.round(values.savingRate * 100)}%
            </label>
            <input
              type="range"
              min={0}
              max={90}
              value={Math.round(values.savingRate * 100)}
              onChange={(e) =>
                onChange({ savingRate: Number(e.target.value) / 100 })
              }
              className="w-full mt-1 accent-blue-600"
            />
            <div className="flex justify-between text-xs text-gray-400 dark:text-gray-500">
              <span>0%</span>
              <span>90%</span>
            </div>
          </div>

          {/* 보유 자산 */}
          <div>
            <label className="text-sm font-medium dark:text-gray-200">보유 자산</label>
            <div className="mt-1">
              <WonInput
                value={values.currentAssets}
                onChange={(v) => onChange({ currentAssets: v })}
                label="보유 자산"
              />
            </div>
          </div>

          {/* 대출 LTV */}
          <div>
            <label className="text-sm font-medium dark:text-gray-200">
              대출 비율 (LTV): {Math.round(values.loanLtv * 100)}%
            </label>
            <input
              type="range"
              min={0}
              max={70}
              value={Math.round(values.loanLtv * 100)}
              onChange={(e) =>
                onChange({ loanLtv: Number(e.target.value) / 100 })
              }
              className="w-full mt-1 accent-blue-600"
            />
            <div className="flex justify-between text-xs text-gray-400 dark:text-gray-500">
              <span>0% (전액 현금)</span>
              <span>70%</span>
            </div>
            <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
              집값 대비 대출 가능 비율입니다. 예: 70%면 5억 집에 3.5억 대출, 나머지 1.5억은 자기 자금으로 마련해야 합니다.
            </p>
          </div>

          {/* 고급 옵션 */}
          <button
            onClick={() => setShowAdvanced(!showAdvanced)}
            className="text-sm text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300"
          >
            {showAdvanced ? '▲ 고급 옵션 접기' : '▼ 고급 옵션 펼치기'}
          </button>

          {showAdvanced && (
            <div className="space-y-4 pl-2 border-l-[3px] border-gray-100/70 dark:border-gray-700">
              <div>
                <label className="text-sm font-medium dark:text-gray-200">
                  실수령 비율: {Math.round(values.takeHomeRatio * 100)}%
                </label>
                <input
                  type="range"
                  min={50}
                  max={100}
                  value={Math.round(values.takeHomeRatio * 100)}
                  onChange={(e) =>
                    onChange({ takeHomeRatio: Number(e.target.value) / 100 })
                  }
                  className="w-full mt-1 accent-blue-600"
                />
              </div>

              <div>
                <label className="text-sm font-medium dark:text-gray-200">
                  저축 연이율: {(values.savingsApr * 100).toFixed(1)}%
                </label>
                <input
                  type="range"
                  min={0}
                  max={10}
                  step={0.5}
                  value={values.savingsApr * 100}
                  onChange={(e) =>
                    onChange({ savingsApr: Number(e.target.value) / 100 })
                  }
                  className="w-full mt-1 accent-blue-600"
                />
                <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
                  저축한 돈에 붙는 연간 이자율입니다. 예금·적금 금리나 투자 수익률을 기준으로 설정하세요. 일반 적금 약 3~4%, 투자 포함 시 5~7% 정도입니다.
                </p>
              </div>

              <div>
                <label className="text-sm font-medium dark:text-gray-200">
                  집값 연간 상승률: {(values.housePriceGrowth * 100).toFixed(1)}%
                </label>
                <input
                  type="range"
                  min={-5}
                  max={15}
                  step={0.5}
                  value={values.housePriceGrowth * 100}
                  onChange={(e) =>
                    onChange({ housePriceGrowth: Number(e.target.value) / 100 })
                  }
                  className="w-full mt-1 accent-blue-600"
                />
                <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
                  매년 집값이 얼마나 오를지 예상치입니다. 최근 10년 전국 평균 약 3~5%이며, 마이너스로 설정하면 집값 하락을 가정합니다.
                </p>
              </div>

              <div>
                <label className="text-sm font-medium dark:text-gray-200">생년 (선택)</label>
                <input
                  type="number"
                  placeholder="예: 1995"
                  value={values.birthYear ?? ''}
                  onChange={(e) =>
                    onChange({
                      birthYear: e.target.value ? Number(e.target.value) : undefined,
                    })
                  }
                  className="w-full px-3 py-2
                             rounded-xl mt-1 text-base font-medium
                             text-gray-900 dark:text-white
                             bg-gray-50 dark:bg-gray-800
                             focus:outline-none focus:ring-2 focus:ring-brand-500"
                />
              </div>
            </div>
          )}
        </div>

        {/* 제출 */}
        <div className="sticky bottom-0 bg-white dark:bg-gray-900 px-5 py-4 shadow-[0_-2px_8px_rgba(0,0,0,0.04)]">
          <button
            onClick={onSubmit}
            className="w-full py-3 bg-brand-500 text-white font-semibold rounded-2xl
                       hover:bg-brand-700 transition-colors"
          >
            계산하기
          </button>
        </div>
      </div>
    </div>
  );
}
