import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AptComplex, CalcInputs, PriceMode } from '../lib/types';

interface AppState {
  // 위치
  lat: number;
  lng: number;
  lawdCd: string;
  regionName: string;

  // 선택된 단지
  selectedComplex: AptComplex | null;

  // 사용자 입력 (로컬 저장)
  inputs: Omit<CalcInputs, 'housePrice'>;

  // UI 상태
  showInputForm: boolean;
  showResult: boolean;
  selectedPrice: number; // 선택한 거래 가격
  priceMode: PriceMode;

  // 액션
  setLocation: (lat: number, lng: number, lawdCd: string, regionName: string) => void;
  setSelectedComplex: (complex: AptComplex | null) => void;
  setInputs: (inputs: Partial<Omit<CalcInputs, 'housePrice'>>) => void;
  setShowInputForm: (show: boolean) => void;
  setShowResult: (show: boolean) => void;
  setSelectedPrice: (price: number) => void;
  setPriceMode: (mode: PriceMode) => void;
}

const DEFAULT_INPUTS: Omit<CalcInputs, 'housePrice'> = {
  annualIncome: 50_000_000,
  takeHomeRatio: 0.84,
  savingRate: 0.4,
  currentAssets: 0,
  savingsApr: 0.03,
  housePriceGrowth: 0.02,
  loanLtv: 0,
};

// 서울시청 좌표 (GPS 실패 시 폴백)
const FALLBACK_LAT = 37.5665;
const FALLBACK_LNG = 126.978;

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      lat: FALLBACK_LAT,
      lng: FALLBACK_LNG,
      lawdCd: '11140', // 서울 중구
      regionName: '서울특별시 중구',

      selectedComplex: null,
      inputs: DEFAULT_INPUTS,
      showInputForm: false,
      showResult: false,
      selectedPrice: 0,
      priceMode: 'iceAmericano' as PriceMode,

      setLocation: (lat, lng, lawdCd, regionName) =>
        set({ lat, lng, lawdCd, regionName }),

      setSelectedComplex: (complex) =>
        set({
          selectedComplex: complex,
          selectedPrice: complex ? complex.avgPrice : 0,
        }),

      setInputs: (partial) =>
        set((state) => ({
          inputs: { ...state.inputs, ...partial },
        })),

      setShowInputForm: (show) => set({ showInputForm: show }),
      setShowResult: (show) => set({ showResult: show }),
      setSelectedPrice: (price) => set({ selectedPrice: price }),
      setPriceMode: (mode) => set({ priceMode: mode }),
    }),
    {
      name: 'zipimoney-inputs',
      partialize: (state) => ({
        inputs: state.inputs,
        priceMode: state.priceMode,
      }),
    },
  ),
);
