import { useEffect, useRef } from 'react';
import type { AptComplex, PriceMode } from '../lib/types';
import districtsData from '../data/districts.json';

declare global {
  interface Window {
    kakao: {
      maps: {
        load: (callback: () => void) => void;
        Map: new (container: HTMLElement, options: unknown) => KakaoMapInstance;
        LatLng: new (lat: number, lng: number) => unknown;
        Marker: new (options: unknown) => KakaoMarker;
        CustomOverlay: new (options: unknown) => KakaoOverlay;
        Polygon: new (options: unknown) => KakaoPolygon;
        event: {
          addListener: (target: unknown, type: string, handler: (e?: unknown) => void) => void;
        };
      };
    };
  }
}

interface KakaoMapInstance {
  setCenter: (latlng: unknown) => void;
  setLevel: (level: number) => void;
  getCenter: () => { getLat: () => number; getLng: () => number };
}

interface KakaoMarker {
  setMap: (map: KakaoMapInstance | null) => void;
}

interface KakaoOverlay {
  setMap: (map: KakaoMapInstance | null) => void;
  setPosition: (position: unknown) => void;
}

interface KakaoPolygon {
  setMap: (map: KakaoMapInstance | null) => void;
  setOptions: (options: Record<string, unknown>) => void;
}

interface Props {
  lat: number;
  lng: number;
  myLat?: number;
  myLng?: number;
  complexes: AptComplex[];
  regionName?: string;
  priceMode: PriceMode;
  onComplexClick: (complex: AptComplex) => void;
  onCenterChanged?: (lat: number, lng: number) => void;
  onMapClick?: (lat: number, lng: number) => void;
}

interface DistrictGroup {
  polygons: KakaoPolygon[];
  nameOverlay: KakaoOverlay;
  name: string;
}

interface DistrictEntry {
  c: string;
  n: string;
  ct: [number, number];
  co: number[][][][];
}

function formatPriceLabel(price: number, mode: PriceMode): string {
  if (mode === 'iceAmericano') {
    const cups = Math.round(price / 4500);
    if (cups >= 10000) return `☕${(cups / 10000).toFixed(1)}만잔`;
    return `☕${cups.toLocaleString()}잔`;
  }
  if (mode === 'chicken') {
    const count = Math.round(price / 22000);
    if (count >= 10000) return `🍗${(count / 10000).toFixed(1)}만마리`;
    return `🍗${count.toLocaleString()}마리`;
  }
  const eok = price / 100_000_000;
  if (eok >= 1) {
    return `${eok.toFixed(1)}억`;
  }
  return `${Math.round(price / 10_000).toLocaleString()}만`;
}

function getPriceColor(price: number): string {
  const eok = price / 100_000_000;
  if (eok < 3) return '#00A650';
  if (eok < 5) return '#7CB342';
  if (eok < 10) return '#F9A825';
  if (eok < 20) return '#EF6C00';
  return '#E53935';
}

export default function KakaoMap({ lat, lng, myLat, myLng, complexes, regionName, priceMode, onComplexClick, onCenterChanged, onMapClick }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<KakaoMapInstance | null>(null);
  const markersRef = useRef<KakaoMarker[]>([]);
  const overlaysRef = useRef<KakaoOverlay[]>([]);
  const myLocationOverlayRef = useRef<KakaoOverlay | null>(null);
  const onCenterChangedRef = useRef(onCenterChanged);
  onCenterChangedRef.current = onCenterChanged;
  const onMapClickRef = useRef(onMapClick);
  onMapClickRef.current = onMapClick;

  // 구 경계 폴리곤
  const districtGroupsRef = useRef<Map<string, DistrictGroup>>(new Map());
  const hoveredCodeRef = useRef<string | null>(null);
  const activeCodeRef = useRef<string | null>(null);

  // 맵 생성 + cleanup (StrictMode 대응)
  useEffect(() => {
    if (!window.kakao?.maps?.Map || !containerRef.current) return;

    const center = new window.kakao.maps.LatLng(lat, lng);
    const map = new window.kakao.maps.Map(containerRef.current, {
      center,
      level: 5,
    });
    mapRef.current = map;

    window.kakao.maps.event.addListener(map, 'dragend', () => {
      const c = map.getCenter();
      onCenterChangedRef.current?.(c.getLat(), c.getLng());
    });

    window.kakao.maps.event.addListener(map, 'click', (e?: unknown) => {
      const me = e as { latLng?: { getLat: () => number; getLng: () => number } } | undefined;
      if (me?.latLng) {
        onMapClickRef.current?.(me.latLng.getLat(), me.latLng.getLng());
      }
    });

    return () => {
      mapRef.current = null;
      markersRef.current = [];
      overlaysRef.current = [];
      myLocationOverlayRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 좌표 변경 시 지도 중심 이동
  useEffect(() => {
    if (!mapRef.current || !window.kakao?.maps) return;
    const center = new window.kakao.maps.LatLng(lat, lng);
    mapRef.current.setCenter(center);
  }, [lat, lng]);

  // 현재 위치 마커 (초록 점)
  useEffect(() => {
    if (!mapRef.current || !window.kakao?.maps?.Map) return;
    const targetLat = myLat ?? lat;
    const targetLng = myLng ?? lng;
    if (targetLat === 0 && targetLng === 0) return;

    myLocationOverlayRef.current?.setMap(null);

    const position = new window.kakao.maps.LatLng(targetLat, targetLng);
    const overlay = new window.kakao.maps.CustomOverlay({
      map: mapRef.current,
      position,
      content: `<div style="
        width: 16px; height: 16px;
        background: #00A650;
        border: 3px solid white;
        border-radius: 50%;
        box-shadow: 0 0 0 2px rgba(3,199,90,0.3), 0 2px 4px rgba(0,0,0,0.2);
        pointer-events: none;
      "></div>`,
      zIndex: 10,
    });
    myLocationOverlayRef.current = overlay;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [myLat, myLng, lat, lng]);

  // 구 경계 폴리곤 로드
  useEffect(() => {
    if (!mapRef.current || !window.kakao?.maps?.Polygon) return;

    const map = mapRef.current;
    const districts = districtsData as DistrictEntry[];

    districts.forEach((d) => {
      const polygons: KakaoPolygon[] = [];

      d.co.forEach((poly) => {
        const outerRing = poly[0];
        if (!outerRing || outerRing.length < 3) return;

        const path = outerRing.map(([lngCoord, latCoord]) =>
          new window.kakao.maps.LatLng(latCoord, lngCoord),
        );
        // 초기에는 map에 넣지 않음 (숨김 상태)
        const polygon = new window.kakao.maps.Polygon({
          path,
          strokeWeight: 4,
          strokeColor: '#059669',
          strokeOpacity: 0.9,
          fillColor: '#10B981',
          fillOpacity: 0.18,
        });
        polygons.push(polygon);
      });

      // 구역 이름 라벨 (항상 표시)
      const [centerLat, centerLng] = d.ct;
      const nameOverlay = new window.kakao.maps.CustomOverlay({
        map,
        position: new window.kakao.maps.LatLng(centerLat, centerLng),
        content: `<div style="
          padding: 4px 12px;
          background: rgba(255,255,255,0.92);
          border: 1.5px solid #94a3b8;
          border-radius: 8px;
          font-size: 14px;
          font-weight: 700;
          color: #1e293b;
          white-space: nowrap;
          pointer-events: none;
          backdrop-filter: blur(4px);
          box-shadow: 0 2px 6px rgba(0,0,0,0.12);
          letter-spacing: 0.5px;
        ">${d.n}</div>`,
        zIndex: 5,
      });

      districtGroupsRef.current.set(d.c, { polygons, nameOverlay, name: d.n });

      polygons.forEach((polygon) => {
        window.kakao.maps.event.addListener(polygon, 'click', (e?: unknown) => {
          const me = e as { latLng?: { getLat: () => number; getLng: () => number } } | undefined;
          if (me?.latLng) {
            onMapClickRef.current?.(me.latLng.getLat(), me.latLng.getLng());
          }
        });
      });
    });

    return () => {
      districtGroupsRef.current.forEach((group) => {
        group.polygons.forEach((p) => p.setMap(null));
        group.nameOverlay.setMap(null);
      });
      districtGroupsRef.current.clear();
      hoveredCodeRef.current = null;
    };
  }, []);

  // 현재 선택된 구역만 표시
  useEffect(() => {
    if (!mapRef.current || districtGroupsRef.current.size === 0) return;
    const map = mapRef.current;

    // 이전 활성 구역 숨기기
    if (activeCodeRef.current) {
      const prevGroup = districtGroupsRef.current.get(activeCodeRef.current);
      prevGroup?.polygons.forEach((p) => p.setMap(null));
    }

    // regionName에서 구/시/군 이름 매칭 (예: "서울특별시 송파구" → "송파구")
    let matchCode: string | null = null;
    if (regionName) {
      districtGroupsRef.current.forEach((group, code) => {
        if (regionName.includes(group.name)) {
          matchCode = code;
        }
      });
    }

    activeCodeRef.current = matchCode;
    if (matchCode) {
      const group = districtGroupsRef.current.get(matchCode);
      group?.polygons.forEach((p) => p.setMap(map));
    }
  }, [regionName]);

  // 마커 업데이트
  const onComplexClickRef = useRef(onComplexClick);
  onComplexClickRef.current = onComplexClick;

  useEffect(() => {
    if (!mapRef.current || !window.kakao?.maps) return;

    markersRef.current.forEach((m) => m.setMap(null));
    overlaysRef.current.forEach((o) => o.setMap(null));
    markersRef.current = [];
    overlaysRef.current = [];

    const map = mapRef.current;

    complexes.forEach((complex) => {
      if (complex.lat === 0 && complex.lng === 0) return;

      const position = new window.kakao.maps.LatLng(complex.lat, complex.lng);

      const marker = new window.kakao.maps.Marker({ map, position });
      markersRef.current.push(marker);

      const label = formatPriceLabel(complex.avgPrice, priceMode);
      const bgColor = getPriceColor(complex.avgPrice);
      const overlay = new window.kakao.maps.CustomOverlay({
        map,
        position,
        content: `<div style="
          padding: 2px 6px;
          background: ${bgColor};
          color: white;
          border-radius: 4px;
          font-size: 11px;
          font-weight: bold;
          white-space: nowrap;
          transform: translateY(-40px);
          pointer-events: none;
        ">${label}</div>`,
        yAnchor: 1,
      });
      overlaysRef.current.push(overlay);

      window.kakao.maps.event.addListener(marker, 'click', () => {
        onComplexClickRef.current(complex);
      });
    });
  }, [complexes, priceMode]);

  return (
    <div
      ref={containerRef}
      className="w-full h-full"
      style={{ minHeight: '400px' }}
    />
  );
}
