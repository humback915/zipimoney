import { useState } from 'react';
import type { AptComplex, AptDeal } from '../lib/types';

interface Props {
  complex: AptComplex;
  onClose: () => void;
  onCalculate: (price: number) => void;
}

type AreaFilter = 'all' | '59' | '84';

function formatPrice(price: number): string {
  const eok = Math.floor(price / 100_000_000);
  const man = Math.round((price % 100_000_000) / 10_000);
  if (eok > 0 && man > 0) return `${eok}억 ${man.toLocaleString()}만`;
  if (eok > 0) return `${eok}억`;
  return `${man.toLocaleString()}만`;
}

function formatDate(deal: AptDeal): string {
  return `${deal.dealYear}.${String(deal.dealMonth).padStart(2, '0')}.${String(deal.dealDay).padStart(2, '0')}`;
}

export default function ComplexBottomSheet({
  complex,
  onClose,
  onCalculate,
}: Props) {
  const [areaFilter, setAreaFilter] = useState<AreaFilter>('all');

  const filteredDeals = complex.deals.filter((d) => {
    if (areaFilter === 'all') return true;
    const target = parseInt(areaFilter);
    return Math.abs(d.area - target) < 5;
  });

  return (
    <div className="fixed inset-x-0 bottom-0 z-50 bg-white dark:bg-gray-900 rounded-t-3xl shadow-2xl
                    max-h-[70vh] flex flex-col animate-slide-up">
      {/* 핸들 */}
      <div className="flex justify-center pt-3 pb-2">
        <div className="w-12 h-1.5 bg-gray-300 rounded-full" />
      </div>

      {/* 헤더 */}
      <div className="px-4 pb-4">
        <div className="flex items-start justify-between">
          <div>
            <h2 className="text-lg font-bold dark:text-white">{complex.name}</h2>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {complex.dong} · {complex.buildYear}년 건축
              {complex.propertyType && complex.propertyType !== 'apt' && (
                <span className="ml-1.5 px-1.5 py-0.5 bg-gray-200 dark:bg-gray-700 rounded text-xs">
                  {{ villa: '빌라', officetel: '오피스텔', house: '단독' }[complex.propertyType] ?? complex.propertyType}
                </span>
              )}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1 text-gray-400 hover:text-gray-600"
          >
            ✕
          </button>
        </div>

        {/* 면적 필터 */}
        <div className="flex gap-2 mt-3">
          {(['all', '59', '84'] as const).map((f) => (
            <button
              key={f}
              onClick={() => setAreaFilter(f)}
              className={`px-3 py-1 text-sm rounded-full transition-colors ${
                areaFilter === f
                  ? 'bg-brand-500 text-white border border-brand-500'
                  : 'bg-white text-gray-600 border border-gray-200 hover:border-brand-400'
              }`}
            >
              {f === 'all' ? '전체' : `${f}㎡`}
            </button>
          ))}
        </div>
      </div>

      {/* 거래 리스트 */}
      <div className="flex-1 overflow-y-auto px-4 py-3">
        {filteredDeals.length === 0 ? (
          <p className="text-center text-gray-400 py-8">거래 내역이 없습니다.</p>
        ) : (
          <div className="space-y-2">
            {filteredDeals.map((deal, i) => (
              <button
                key={i}
                onClick={() => onCalculate(deal.price)}
                className="w-full text-left p-3 rounded-xl bg-gray-50 dark:bg-gray-800
                           hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
              >
                <div className="flex justify-between items-center">
                  <span className="font-semibold text-brand-500">
                    {formatPrice(deal.price)}
                  </span>
                  <span className="text-xs text-gray-400">
                    {formatDate(deal)}
                  </span>
                </div>
                <div className="text-sm text-gray-500 mt-1">
                  전용 {deal.area}㎡ · {deal.floor}층
                </div>
              </button>
            ))}
          </div>
        )}
      </div>

      {/* CTA */}
      <div className="px-4 py-3 bg-gray-50 dark:bg-gray-800 shadow-[0_-2px_8px_rgba(0,0,0,0.04)]">
        <button
          onClick={() => onCalculate(complex.avgPrice)}
          className="w-full py-3 bg-brand-500 text-white font-semibold rounded-2xl
                     hover:bg-brand-700 transition-colors"
        >
          이 집 사려면? (평균 {formatPrice(complex.avgPrice)})
        </button>
      </div>
    </div>
  );
}
