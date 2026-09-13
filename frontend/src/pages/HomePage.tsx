import { useEffect, useCallback, useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { calculate } from '../lib/calculator';
import type { AptComplex, CalcResult, PropertyType } from '../lib/types';
import { useAppStore } from '../stores/app-store';
import { useGeolocation } from '../hooks/useGeolocation';
import { useAuth } from '../hooks/useAuth';
import KakaoMap from '../components/KakaoMap';
import RegionSelector from '../components/RegionSelector';
import ComplexBottomSheet from '../components/ComplexBottomSheet';
import InputForm from '../components/InputForm';
import ResultView from '../components/ResultView';
import { MapSkeleton, MapErrorFallback, DealsLoadingOverlay, DealsErrorOverlay, DealsEmptyOverlay } from '../components/Skeleton';
import SearchBar from '../components/SearchBar';
import AuthButton from '../components/AuthButton';
import HistoryDrawer from '../components/HistoryDrawer';
import LoginOverlay from '../components/LoginOverlay';

async function fetchRegion(lat: number, lng: number) {
  const res = await fetch(`/api/geocode?lat=${lat}&lng=${lng}`);
  if (!res.ok) throw new Error('지역 정보를 가져올 수 없습니다.');
  const json = await res.json();
  return json.data as { lawdCd: string; regionName: string };
}

async function fetchDeals(lawdCd: string, ymd: string) {
  const res = await fetch(`/api/deals?lawdCd=${lawdCd}&ymd=${ymd}`);
  if (!res.ok) throw new Error('실거래 데이터를 가져올 수 없습니다.');
  const json = await res.json();
  return (json.data ?? []) as AptComplex[];
}

function getCurrentYmd(): string {
  const now = new Date();
  // 이번 달 데이터가 아직 없을 수 있으므로 전월 사용
  const d = new Date(now.getFullYear(), now.getMonth() - 1, 1);
  return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`;
}

export default function HomePage() {
  const geo = useGeolocation();
  const store = useAppStore();
  const { user } = useAuth();
  const [calcResult, setCalcResult] = useState<CalcResult | null>(null);
  const [mapReady, setMapReady] = useState(false);
  const [mapMoved, setMapMoved] = useState(false);
  const [pendingCenter, setPendingCenter] = useState<{ lat: number; lng: number } | null>(null);
  const [showHistory, setShowHistory] = useState(false);
  const [showLoginOverlay, setShowLoginOverlay] = useState(false);
  const [, setShowProfile] = useState(false);
  const [mapLoadFailed, setMapLoadFailed] = useState(false);
  const [propertyFilter, setPropertyFilter] = useState<PropertyType>('all');

  const requireAuth = (action: () => void) => {
    if (!user) {
      setShowLoginOverlay(true);
      return;
    }
    action();
  };

  // 카카오맵 SDK 로드 (autoload=false)
  useEffect(() => {
    let attempts = 0;
    const maxAttempts = 50; // 10초 (200ms * 50)
    const check = () => {
      if (window.kakao?.maps?.load) {
        window.kakao.maps.load(() => {
          console.log('[KakaoMap] SDK loaded, Map=', !!window.kakao.maps.Map);
          setMapReady(true);
        });
      } else if (attempts < maxAttempts) {
        attempts++;
        setTimeout(check, 200);
      } else {
        setMapLoadFailed(true);
      }
    };
    check();
  }, []);

  // GPS → 지역코드
  const regionQuery = useQuery({
    queryKey: ['region', geo.lat, geo.lng],
    queryFn: () => fetchRegion(geo.lat, geo.lng),
    enabled: !geo.loading && geo.lat !== 0,
    retry: 1,
  });

  // 지역코드 업데이트
  useEffect(() => {
    if (regionQuery.data) {
      store.setLocation(
        geo.lat,
        geo.lng,
        regionQuery.data.lawdCd,
        regionQuery.data.regionName,
      );
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [regionQuery.data]);

  const ymd = getCurrentYmd();

  // 실거래 데이터 조회
  const dealsQuery = useQuery({
    queryKey: ['deals', store.lawdCd, ymd],
    queryFn: () => fetchDeals(store.lawdCd, ymd),
    enabled: !!store.lawdCd,
  });

  const filteredComplexes = useMemo(() => {
    const data = dealsQuery.data ?? [];
    if (propertyFilter === 'all') return data;
    return data.filter((c) => c.propertyType === propertyFilter);
  }, [dealsQuery.data, propertyFilter]);

  const handleComplexClick = useCallback(
    (complex: AptComplex) => {
      store.setSelectedComplex(complex);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [],
  );

  const handleRegionChange = async (code: string, name: string) => {
    store.setSelectedComplex(null);
    setMapMoved(false);
    setPendingCenter(null);

    // 지역명으로 좌표 검색 (카카오 geocoder)
    try {
      const res = await fetch(
        `/api/geocode?q=${encodeURIComponent(name)}`,
      );
      if (res.ok) {
        const json = await res.json();
        const data = json.data;
        store.setLocation(data.lat, data.lng, code, name);
        return;
      }
    } catch {}

    // 실패 시 좌표 유지, 코드만 변경
    store.setLocation(store.lat, store.lng, code, name);
  };

  const handleSearchComplex = (_name: string, lat: number, lng: number) => {
    store.setLocation(lat, lng, store.lawdCd, store.regionName);
  };

  const handleMapCenterChanged = useCallback((newLat: number, newLng: number) => {
    setPendingCenter({ lat: newLat, lng: newLng });
    setMapMoved(true);
  }, []);

  const handleMapClick = useCallback(async (clickLat: number, clickLng: number) => {
    try {
      const data = await fetchRegion(clickLat, clickLng);
      if (data.lawdCd !== store.lawdCd) {
        store.setLocation(clickLat, clickLng, data.lawdCd, data.regionName);
        store.setSelectedComplex(null);
        setMapMoved(false);
        setPendingCenter(null);
      }
    } catch {}
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [store.lawdCd]);

  const handleSearchThisArea = async () => {
    if (!pendingCenter) return;
    try {
      const data = await fetchRegion(pendingCenter.lat, pendingCenter.lng);
      store.setLocation(pendingCenter.lat, pendingCenter.lng, data.lawdCd, data.regionName);
      store.setSelectedComplex(null);
    } catch {
      // 지역 코드 변환 실패 시 좌표만 업데이트
      store.setLocation(pendingCenter.lat, pendingCenter.lng, store.lawdCd, store.regionName);
    }
    setMapMoved(false);
    setPendingCenter(null);
  };

  const handleMoveToMyLocation = () => {
    if (geo.lat && geo.lng) {
      store.setLocation(geo.lat, geo.lng, store.lawdCd, store.regionName);
      // GPS 위치 기반으로 지역코드도 다시 가져오기
      fetchRegion(geo.lat, geo.lng).then((data) => {
        store.setLocation(geo.lat, geo.lng, data.lawdCd, data.regionName);
      }).catch(() => {});
    }
  };

  const handleCalculate = (price: number) => {
    store.setSelectedPrice(price);
    store.setSelectedComplex(null);
    store.setShowInputForm(true);
  };

  const handleSubmitCalc = () => {
    const result = calculate({
      housePrice: store.selectedPrice || 500_000_000,
      ...store.inputs,
    });
    setCalcResult(result);
    store.setShowInputForm(false);
    store.setShowResult(true);
  };

  return (
    <div className="h-screen flex flex-col bg-gray-50 dark:bg-gray-950">
      {/* 상단 바 */}
      <header className="bg-brand-500 dark:bg-brand-600 shadow-sm px-4 py-3 z-30">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-white">ZIPIMONEY</h1>
            <p className="text-xs text-white/70">
              {store.regionName || '위치 확인 중...'}
            </p>
          </div>
          <AuthButton
            onShowProfile={() => {
              setShowProfile(true);
              store.setShowInputForm(true);
            }}
            onShowHistory={() => requireAuth(() => setShowHistory(true))}
          />
        </div>
        <div className="mt-2 flex items-center gap-2">
          <RegionSelector
            currentCode={store.lawdCd}
            onSelect={handleRegionChange}
          />
          <SearchBar
            lawdCd={store.lawdCd}
            onSelectRegion={handleRegionChange}
            onSelectComplex={handleSearchComplex}
          />
        </div>
      </header>

      {/* 카테고리 탭 */}
      <div className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 px-4 py-1.5 flex gap-1.5 overflow-x-auto z-20">
        {([
          ['all', '전체'],
          ['apt', '아파트'],
          ['villa', '빌라'],
          ['officetel', '오피스텔'],
          ['house', '단독/다가구'],
        ] as [PropertyType, string][]).map(([value, label]) => (
          <button
            key={value}
            onClick={() => setPropertyFilter(value)}
            className={`px-3 py-1.5 text-xs font-medium rounded-full whitespace-nowrap transition-colors ${
              propertyFilter === value
                ? 'bg-brand-500 text-white'
                : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {/* GPS 에러 안내 */}
      {geo.error && (
        <div className="bg-amber-50 dark:bg-amber-900/30 border-b border-amber-200 dark:border-amber-700 px-4 py-2 text-sm text-amber-700 dark:text-amber-300" role="alert">
          {geo.error}
        </div>
      )}

      {/* 지도 */}
      <div className="flex-1 relative">
        {mapReady ? (
          <KakaoMap
            lat={store.lat}
            lng={store.lng}
            myLat={geo.lat}
            myLng={geo.lng}
            complexes={filteredComplexes}
            regionName={store.regionName}
            onComplexClick={handleComplexClick}
            onCenterChanged={handleMapCenterChanged}
            onMapClick={handleMapClick}
          />
        ) : mapLoadFailed ? (
          <MapErrorFallback onRetry={() => window.location.reload()} />
        ) : (
          <MapSkeleton />
        )}

        {/* 로딩 */}
        {dealsQuery.isLoading && <DealsLoadingOverlay />}

        {/* 에러 */}
        {dealsQuery.isError && (
          <DealsErrorOverlay onRetry={() => dealsQuery.refetch()} />
        )}

        {/* 데이터 비어있음 */}
        {dealsQuery.isSuccess && filteredComplexes.length === 0 && (
          <DealsEmptyOverlay />
        )}

        {/* 이 지역 검색 버튼 (하단 중앙) */}
        {mapMoved && (
          <button
            onClick={handleSearchThisArea}
            className="absolute bottom-4 left-1/2 -translate-x-1/2 bg-white dark:bg-gray-800
                       px-4 py-2 rounded-full shadow-md text-sm font-semibold
                       text-brand-500 dark:text-brand-400 z-20
                       hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors
                       flex items-center gap-1.5"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            이 지역 검색
          </button>
        )}

        {/* 내 위치 FAB (좌측 하단) */}
        <button
          onClick={handleMoveToMyLocation}
          className="absolute bottom-4 left-4 bg-white dark:bg-gray-800 w-12 h-12
                     rounded-full shadow-md flex items-center justify-center z-20
                     hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
          aria-label="내 위치로 이동"
          title="내 위치로 이동"
        >
          <svg className="w-5 h-5 text-brand-500 dark:text-brand-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <circle cx="12" cy="12" r="3" />
            <path strokeLinecap="round" d="M12 2v3m0 14v3m-10-10h3m14 0h3" />
          </svg>
        </button>

        {/* 결과 요약 (데이터 있을 때) */}
        {filteredComplexes.length > 0 && (
          <div className="absolute bottom-16 left-4 bg-white dark:bg-gray-800 px-3 py-2 rounded-2xl
                          shadow-md text-xs text-gray-600 dark:text-gray-300 z-20">
            {filteredComplexes.length}개 단지 · 마커를 눌러 상세 보기
          </div>
        )}

        {/* 내 조건 입력하기 FAB */}
        <button
          onClick={() => requireAuth(() => store.setShowInputForm(true))}
          className="absolute bottom-4 right-4 bg-brand-500 text-white px-6 py-3.5
                     rounded-full shadow-md hover:bg-brand-700 transition-colors
                     font-semibold text-sm z-20"
        >
          내 조건 입력하기
        </button>
      </div>

      {/* 바텀시트: 단지 상세 */}
      {store.selectedComplex && (
        <ComplexBottomSheet
          complex={store.selectedComplex}
          onClose={() => store.setSelectedComplex(null)}
          onCalculate={(price) => requireAuth(() => handleCalculate(price))}
        />
      )}

      {/* 입력 폼 */}
      {store.showInputForm && (
        <InputForm
          values={store.inputs}
          housePrice={store.selectedPrice || 500_000_000}
          onChange={(partial) => store.setInputs(partial)}
          onSubmit={handleSubmitCalc}
          onClose={() => store.setShowInputForm(false)}
        />
      )}

      {/* 결과 */}
      {store.showResult && calcResult && (
        <ResultView
          result={calcResult}
          complexName={store.selectedComplex?.name ?? '선택한 아파트'}
          regionName={store.regionName}
          onClose={() => store.setShowResult(false)}
        />
      )}

      {/* 로그인 오버레이 */}
      {showLoginOverlay && (
        <LoginOverlay onClose={() => setShowLoginOverlay(false)} />
      )}

      {/* 계산 이력 */}
      {showHistory && (
        <HistoryDrawer onClose={() => setShowHistory(false)} />
      )}

      {/* 하단 고지 */}
      <footer className="bg-gray-100 dark:bg-gray-900 px-4 py-2 text-center">
        <p className="text-[10px] text-gray-400 dark:text-gray-500 leading-relaxed">
          본 앱은 재미용이며 실제 투자·대출 판단의 근거가 될 수 없습니다.
          실거래가는 국토교통부 공개 데이터를 기반으로 하며 실제 시세와 다를 수 있습니다.
        </p>
      </footer>
    </div>
  );
}
