import { getKakaoLoginUrl } from '../hooks/useAuth';

interface Props {
  onClose?: () => void;
}

export default function LoginOverlay({ onClose }: Props) {
  const loginUrl = getKakaoLoginUrl();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl p-8 mx-4 max-w-sm w-full text-center">
        <h1 className="text-2xl font-bold text-brand-500 dark:text-brand-400 mb-2">
          ZIPIMONEY
        </h1>
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">
          로그인 후 이용할 수 있습니다
        </p>

        <a
          href={loginUrl}
          className="inline-flex items-center justify-center gap-2 w-full
                     bg-[#FEE500] text-[#191919] font-semibold py-3 rounded-xl
                     hover:bg-[#FDD835] transition-colors text-sm"
        >
          <svg className="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 3C6.48 3 2 6.48 2 10.8c0 2.76 1.84 5.18 4.6 6.54-.14.52-.92 3.37-.95 3.58 0 0-.02.17.09.24.11.06.24.01.24.01.32-.04 3.7-2.44 4.28-2.86.56.08 1.14.12 1.74.12 5.52 0 10-3.48 10-7.63C22 6.48 17.52 3 12 3z" />
          </svg>
          카카오로 시작하기
        </a>

        {onClose && (
          <button
            onClick={onClose}
            className="mt-3 text-sm text-gray-400 dark:text-gray-500 hover:text-gray-600
                       dark:hover:text-gray-300 transition-colors"
          >
            닫기
          </button>
        )}
      </div>
    </div>
  );
}
