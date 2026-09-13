import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../api/client'

export default function KakaoCallback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

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
      .catch(() => {
        navigate('/')
      })
  }, [searchParams, navigate, queryClient])

  return (
    <div className="flex items-center justify-center h-screen">
      <p className="text-lg">로그인 중...</p>
    </div>
  )
}
