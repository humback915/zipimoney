import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { apiFetch } from '../api/client'

interface ShareCardData {
  sigungu: string
  exclusiveArea: number | null
  totalMonths: number | null
  reachable: boolean
  memeText: string
  viewCount: number
}

export default function SharePage() {
  const { shareKey } = useParams<{ shareKey: string }>()

  const { data, isLoading } = useQuery({
    queryKey: ['share', shareKey],
    queryFn: () => apiFetch<ShareCardData>(`/api/share/${shareKey}`),
    enabled: !!shareKey,
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50 dark:bg-gray-950">
        <p className="text-gray-400">로딩 중...</p>
      </div>
    )
  }

  if (!data) {
    return (
      <div className="flex flex-col items-center justify-center h-screen bg-gray-50 dark:bg-gray-950 gap-4">
        <p className="text-gray-500">공유 카드를 찾을 수 없습니다.</p>
        <Link
          to="/"
          className="px-4 py-2 bg-brand-500 text-white rounded-lg text-sm"
        >
          홈으로
        </Link>
      </div>
    )
  }

  const years = data.totalMonths ? Math.floor(data.totalMonths / 12) : 0
  const months = data.totalMonths ? data.totalMonths % 12 : 0

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950 flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-900 w-full max-w-md rounded-3xl shadow-xl overflow-hidden">
        {/* 헤더 */}
        <div className="px-5 pt-5 pb-3">
          <p className="text-sm text-gray-500 dark:text-gray-400">{data.sigungu}</p>
          <p className="text-sm text-gray-400 dark:text-gray-500">사려면?</p>
        </div>

        {/* 메인 결과 */}
        <div className="px-5 py-6 text-center">
          {data.reachable && data.totalMonths != null ? (
            <>
              <p className="text-5xl font-black text-brand-500 dark:text-brand-400">
                {years > 0 && `${years}년 `}
                {months > 0 && `${months}개월`}
                {data.totalMonths === 0 && '지금 당장!'}
              </p>
              {data.totalMonths > 0 && (
                <p className="text-lg font-semibold text-gray-700 dark:text-gray-300 mt-2">
                  저축해야 합니다
                </p>
              )}
            </>
          ) : (
            <>
              <p className="text-4xl font-black text-red-500 dark:text-red-400">
                영원히 못 삽니다
              </p>
              <p className="text-gray-500 dark:text-gray-400 mt-3">
                집값 상승 속도가 저축 속도보다 빠릅니다
              </p>
            </>
          )}
        </div>

        {/* 재미 카드 */}
        {data.memeText && (
          <div className="px-5 pb-4">
            <div className="bg-amber-50 dark:bg-amber-900/30 rounded-2xl p-4">
              <p className="text-sm font-medium text-amber-800 dark:text-amber-300">
                {data.memeText}
              </p>
            </div>
          </div>
        )}

        {/* 조회수 */}
        <div className="px-5 pb-4">
          <p className="text-xs text-gray-400 dark:text-gray-500 text-center">
            {data.viewCount}명이 봤어요
          </p>
        </div>

        {/* 하단 버튼 */}
        <div className="px-5 pb-5">
          <Link
            to="/"
            className="block w-full py-3 bg-brand-500 text-white font-semibold rounded-2xl
                       hover:bg-brand-700 transition-colors text-center"
          >
            나도 계산해보기
          </Link>
          <p className="text-[10px] text-gray-400 dark:text-gray-500 text-center leading-relaxed mt-3">
            본 앱은 재미용이며 실제 투자·대출 판단의 근거가 될 수 없습니다.
            실거래가는 국토교통부 공개 데이터를 기반으로 하며 실제 시세와 다를 수 있습니다.
          </p>
        </div>
      </div>
    </div>
  )
}
