import { useState, useRef, useEffect } from 'react';
import { useAuth, getKakaoLoginUrl } from '../hooks/useAuth';

interface Props {
  onShowProfile?: () => void;
  onShowHistory?: () => void;
  birthYear?: number;
}

function parseAgeRange(ageRange: string | null): number | null {
  if (!ageRange) return null;
  const match = ageRange.match(/^(\d+)/);
  if (!match) return null;
  return Number(match[1]) + 5;
}

function computeConsumption(birthYear: number | undefined, ageRange: string | null): number | null {
  const currentYear = new Date().getFullYear();
  let age: number | null = null;
  if (birthYear != null) {
    age = currentYear - birthYear;
  } else {
    age = parseAgeRange(ageRange);
  }
  if (age == null || age <= 0) return null;
  return age * 365;
}

export default function AuthButton({ onShowProfile, onShowHistory, birthYear }: Props) {
  const { user, isLoggedIn, isLoading, logout, withdraw } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  if (isLoading) {
    return (
      <div className="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-700 animate-pulse" />
    );
  }

  if (!isLoggedIn) {
    const loginUrl = getKakaoLoginUrl();

    return (
      <a
        href={loginUrl}
        className="bg-[#FEE500] text-[#191919] px-3 py-1.5 rounded-xl text-xs font-semibold
                   hover:bg-[#FDD835] transition-colors flex items-center gap-1"
      >
        <svg className="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 3C6.48 3 2 6.48 2 10.8c0 2.76 1.84 5.18 4.6 6.54-.14.52-.92 3.37-.95 3.58 0 0-.02.17.09.24.11.06.24.01.24.01.32-.04 3.7-2.44 4.28-2.86.56.08 1.14.12 1.74.12 5.52 0 10-3.48 10-7.63C22 6.48 17.52 3 12 3z" />
        </svg>
        로그인
      </a>
    );
  }

  const consumption = isLoggedIn ? computeConsumption(birthYear, user?.ageRange ?? null) : null;

  return (
    <div className="relative flex items-center gap-2" ref={menuRef}>
      {consumption != null && (
        <span className="text-[11px] text-white/80 whitespace-nowrap">
          ☕{consumption.toLocaleString()}잔 🍗{consumption.toLocaleString()}마리
        </span>
      )}
      <button
        onClick={() => setMenuOpen(!menuOpen)}
        className="flex items-center gap-2 hover:opacity-80 transition-opacity"
      >
        <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">
          <span className="text-xs font-bold text-white">
            {user?.name?.charAt(0) || '?'}
          </span>
        </div>
      </button>

      {menuOpen && (
        <div className="absolute right-0 top-full mt-1 w-48 bg-white dark:bg-gray-800
                        rounded-2xl shadow-lg py-1 z-50">
          <div className="px-3 py-2 pb-3">
            <p className="text-sm font-medium dark:text-white truncate">
              {user?.name || '사용자'}
            </p>
          </div>

          <button
            onClick={() => { setMenuOpen(false); onShowProfile?.(); }}
            className="w-full text-left px-3 py-2 text-sm text-gray-700 dark:text-gray-300
                       hover:bg-gray-50/80 dark:hover:bg-gray-700"
          >
            내 조건 설정
          </button>

          <button
            onClick={() => { setMenuOpen(false); onShowHistory?.(); }}
            className="w-full text-left px-3 py-2 text-sm text-gray-700 dark:text-gray-300
                       hover:bg-gray-50/80 dark:hover:bg-gray-700"
          >
            계산 이력
          </button>

          <div className="bg-gray-100 dark:bg-gray-800 h-px mx-3" />

          <button
            onClick={() => { setMenuOpen(false); logout(); }}
            className="w-full text-left px-3 py-2 text-sm text-gray-500 dark:text-gray-400
                       hover:bg-gray-50/80 dark:hover:bg-gray-700"
          >
            로그아웃
          </button>

          <button
            onClick={() => {
              if (confirm('정말 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.')) {
                setMenuOpen(false);
                withdraw();
              }
            }}
            className="w-full text-left px-3 py-2 text-sm text-red-400 dark:text-red-400
                       hover:bg-red-50 dark:hover:bg-red-900/20"
          >
            회원 탈퇴
          </button>
        </div>
      )}
    </div>
  );
}
