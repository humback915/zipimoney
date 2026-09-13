import { useEffect, useState } from 'react';

interface GeoResult {
  lat: number;
  lng: number;
  error: string | null;
  loading: boolean;
}

export function useGeolocation(): GeoResult {
  const [result, setResult] = useState<GeoResult>({
    lat: 0,
    lng: 0,
    error: null,
    loading: true,
  });

  useEffect(() => {
    if (!navigator.geolocation) {
      setResult({
        lat: 37.5665,
        lng: 126.978,
        error: 'GPS를 지원하지 않는 브라우저입니다.',
        loading: false,
      });
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setResult({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
          error: null,
          loading: false,
        });
      },
      (err) => {
        // 폴백: 서울시청
        setResult({
          lat: 37.5665,
          lng: 126.978,
          error:
            err.code === 1
              ? '위치 권한이 거부되었습니다. 지역을 직접 선택해주세요.'
              : '위치를 가져올 수 없습니다. 지역을 직접 선택해주세요.',
          loading: false,
        });
      },
      { enableHighAccuracy: false, timeout: 10000 },
    );
  }, []);

  return result;
}
