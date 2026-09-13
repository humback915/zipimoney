import { useQuery } from '@tanstack/react-query';

interface HistoryItem {
  id: string;
  lawdCd: string;
  housePrice: string;
  totalMonths: number | null;
  reachable: boolean;
  createdAt: string;
}

function formatWon(v: number): string {
  const eok = Math.floor(v / 100_000_000);
  const man = Math.round((v % 100_000_000) / 10_000);
  if (eok > 0 && man > 0) return `${eok}억 ${man.toLocaleString()}만`;
  if (eok > 0) return `${eok}억`;
  return `${man.toLocaleString()}만`;
}

interface Props {
  onClose: () => void;
}

export default function HistoryDrawer({ onClose }: Props) {
  const { data, isLoading } = useQuery<HistoryItem[]>({
    queryKey: ['history'],
    queryFn: async () => {
      const res = await fetch('/api/history', {
        credentials: 'include',
      });
      if (!res.ok) return [];
      return res.json();
    },
  });

  return (
    <div
      className="fixed inset-0 z-50 bg-black/40 flex items-end sm:items-center justify-center"
      onClick={onClose}
    >
      <div
        className="bg-white dark:bg-gray-900 w-full sm:max-w-md rounded-t-3xl sm:rounded-3xl
                   max-h-[80vh] overflow-hidden flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-5 pt-5 pb-4 flex justify-between items-center">
          <h2 className="text-lg font-bold dark:text-white">계산 이력</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 p-1"
            aria-label="닫기"
          >
            ✕
          </button>
        </div>

        <div className="flex-1 overflow-y-auto">
          {isLoading && (
            <div className="p-8 text-center text-gray-400 text-sm">
              불러오는 중...
            </div>
          )}

          {!isLoading && (!data || data.length === 0) && (
            <div className="p-8 text-center text-gray-400 text-sm">
              아직 계산 이력이 없습니다.
            </div>
          )}

          {data && data.length > 0 && (
            <ul>
              {data.map((item) => {
                const years = item.totalMonths
                  ? Math.floor(item.totalMonths / 12)
                  : 0;
                const months = item.totalMonths
                  ? item.totalMonths % 12
                  : 0;
                const date = new Date(item.createdAt);

                return (
                  <li key={item.id} className="px-5 py-4 border-b border-gray-100 dark:border-gray-800/50">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium dark:text-white">
                          {formatWon(Number(item.housePrice))}
                        </p>
                        <p className="text-xs text-gray-400 mt-0.5">
                          {date.toLocaleDateString('ko-KR')}
                        </p>
                      </div>
                      <div className="text-right">
                        {item.reachable ? (
                          <p className="text-sm font-bold text-brand-500 dark:text-brand-400">
                            {years > 0 && `${years}년 `}
                            {months > 0 && `${months}개월`}
                            {item.totalMonths === 0 && '즉시!'}
                          </p>
                        ) : (
                          <p className="text-sm font-bold text-red-500">
                            불가
                          </p>
                        )}
                      </div>
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
