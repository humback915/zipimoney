import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../api/client'

export default function KakaoCallback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const code = searchParams.get('code')
    if (!code) {
      navigate('/')
      return
    }

    apiFetch('/api/auth/kakao', {
      method: 'POST',
      body: JSON.stringify({
        code,
        redirectUri: `${window.location.origin}/kakao/callback`,
      }),
    })
      .then(() => {
        queryClient.invalidateQueries({ queryKey: ['auth'] })
        navigate('/')
      })
      .catch((err) => {
        console.error('카카오 로그인 실패:', err)
        setError(err.message || '로그인에 실패했습니다.')
      })
  }, [searchParams, navigate, queryClient])

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-screen gap-4 px-4">
        <p className="text-lg font-semibold text-red-500">로그인 실패</p>
        <p className="text-sm text-gray-500 text-center">{error}</p>
        <button
          onClick={() => navigate('/')}
          className="px-4 py-2 bg-brand-500 text-white rounded-lg text-sm"
        >
          홈으로 돌아가기
        </button>
      </div>
    )
  }

  return (
    <div className="flex items-center justify-center h-screen">
      <p className="text-lg">로그인 중...</p>
    </div>
  )
}
