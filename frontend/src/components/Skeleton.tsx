import { useState, useEffect } from 'react';

/** 기본 스켈레톤 블록 */
export function Skeleton({ className = '' }: { className?: string }) {
  return (
    <div
      className={`animate-pulse bg-gray-200 dark:bg-gray-700 rounded ${className}`}
    />
  );
}

/** 지도 로딩 스켈레톤 */
export function MapSkeleton() {
  return (
    <div className="w-full h-full flex flex-col items-center justify-center bg-gray-100 dark:bg-gray-900 gap-4">
      <svg className="w-16 h-16 text-gray-300 dark:text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
      </svg>
      <div className="flex items-center gap-2 text-gray-400 dark:text-gray-500 text-sm">
        <svg
          className="animate-spin h-4 w-4"
          viewBox="0 0 24 24"
          fill="none"
        >
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>
        카카오맵을 불러오는 중...
      </div>
      <p className="text-xs text-gray-300 dark:text-gray-600">잠시만 기다려 주세요</p>
    </div>
  );
}

/** 지도 로딩 실패 */
export function MapErrorFallback({ onRetry }: { onRetry?: () => void }) {
  return (
    <div className="w-full h-full flex flex-col items-center justify-center bg-gray-100 dark:bg-gray-900 gap-4">
      <svg className="w-16 h-16 text-gray-400 dark:text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
      </svg>
      <p className="text-sm text-gray-500 dark:text-gray-400 font-medium">
        지도를 불러올 수 없습니다
      </p>
      <p className="text-xs text-gray-400 dark:text-gray-500 text-center px-8">
        카카오맵 SDK 로딩에 실패했습니다.<br />
        인터넷 연결을 확인하고 새로고침 해주세요.
      </p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-2 px-4 py-2 bg-brand-500 text-white rounded-full text-sm font-medium
                     hover:bg-brand-600 transition-colors"
        >
          새로고침
        </button>
      )}
    </div>
  );
}

/** 데이터 로딩 오버레이 (스피너 + 텍스트) */
export function DealsLoadingOverlay() {
  return (
    <div className="absolute top-4 left-1/2 -translate-x-1/2 bg-white dark:bg-gray-800
                    px-4 py-2 rounded-full shadow-lg text-sm text-gray-600 dark:text-gray-300
                    z-20 flex items-center gap-2">
      <svg
        className="animate-spin h-4 w-4 text-brand-500"
        viewBox="0 0 24 24"
        fill="none"
      >
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      실거래 데이터 로딩 중...
    </div>
  );
}

/** 에러 상태 오버레이 (재시도 버튼 포함) */
export function DealsErrorOverlay({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="absolute top-4 left-1/2 -translate-x-1/2 bg-red-50 dark:bg-red-900/40
                    px-5 py-3 rounded-2xl shadow-lg text-sm z-20 max-w-xs text-center">
      <p className="text-red-600 dark:text-red-300 font-medium">
        데이터를 불러올 수 없습니다
      </p>
      <p className="text-red-400 dark:text-red-400 text-xs mt-1">
        서버 연결에 문제가 있습니다. 잠시 후 다시 시도해 주세요.
      </p>
      <button
        onClick={onRetry}
        className="mt-2 px-4 py-1.5 bg-red-100 dark:bg-red-800 text-red-700 dark:text-red-200
                   rounded-full text-xs font-medium hover:bg-red-200 dark:hover:bg-red-700
                   transition-colors"
      >
        다시 시도
      </button>
    </div>
  );
}

/** 데이터 비어있을 때 안내 (3초 후 자동 사라짐) */
export function DealsEmptyOverlay() {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => setVisible(false), 3000);
    return () => clearTimeout(timer);
  }, []);

  if (!visible) return null;

  return (
    <div className="absolute top-4 left-1/2 -translate-x-1/2 bg-white dark:bg-gray-800
                    px-5 py-3 rounded-2xl shadow-lg text-sm z-20 max-w-xs text-center
                    animate-fade-out">
      <p className="text-gray-600 dark:text-gray-300 font-medium">
        이 지역의 거래 데이터가 없습니다
      </p>
      <p className="text-gray-400 dark:text-gray-500 text-xs mt-1">
        다른 지역을 선택하거나 지도를 이동해서 검색해 보세요.
      </p>
    </div>
  );
}
