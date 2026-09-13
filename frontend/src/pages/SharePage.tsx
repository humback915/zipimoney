import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { apiFetch } from '../api/client'
import { useAuth } from '../hooks/useAuth'
import LoginOverlay from '../components/LoginOverlay'

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
  const { user, isLoading: authLoading } = useAuth()

  const { data, isLoading } = useQuery({
    queryKey: ['share', shareKey],
    queryFn: () => apiFetch<ShareCardData>(`/api/share/${shareKey}`),
    enabled: !!shareKey && !!user,
  })

  if (authLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <p>로딩 중...</p>
      </div>
    )
  }

  if (!user) {
    return <LoginOverlay />
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <p>로딩 중...</p>
      </div>
    )
  }

  if (!data) {
    return (
      <div className="flex items-center justify-center h-screen">
        <p>공유 카드를 찾을 수 없습니다.</p>
      </div>
    )
  }

  const pyeong = data.exclusiveArea
    ? `${Math.round(data.exclusiveArea / 3.3058)}평`
    : ''

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full">
        <h1 className="text-2xl font-bold text-center mb-4">
          {data.sigungu} {pyeong}
        </h1>
        <div className="text-center mb-6">
          {data.reachable && data.totalMonths != null ? (
            <p className="text-4xl font-bold text-blue-600">
              {Math.floor(data.totalMonths / 12)}년 {data.totalMonths % 12}개월
            </p>
          ) : (
            <p className="text-2xl font-bold text-red-500">도달 불가</p>
          )}
        </div>
        <p className="text-gray-600 text-center italic">"{data.memeText}"</p>
        <p className="text-sm text-gray-400 text-center mt-4">
          조회수 {data.viewCount}
        </p>
      </div>
    </div>
  )
}
