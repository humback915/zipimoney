import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'

interface User {
  id: number
  kakaoId?: string
  nickname: string | null
  name: string | null
  profileImage: string | null
  email: string | null
  ageRange: string | null
}

async function fetchMe(): Promise<User | null> {
  const res = await fetch('/api/auth/me', { credentials: 'include' })
  if (res.status === 401) return null
  if (!res.ok) return null
  const json = await res.json()
  return json.data ?? null
}

export function useAuth() {
  const queryClient = useQueryClient()

  const { data: user, isLoading } = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: fetchMe,
    staleTime: 5 * 60 * 1000,
    retry: false,
  })

  const logoutMutation = useMutation({
    mutationFn: async () => {
      await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' })
    },
    onSuccess: () => {
      queryClient.setQueryData(['auth', 'me'], null)
      queryClient.invalidateQueries({ queryKey: ['auth'] })
    },
  })

  const withdrawMutation = useMutation({
    mutationFn: async () => {
      const res = await fetch('/api/auth/withdraw', {
        method: 'POST',
        credentials: 'include',
      })
      if (!res.ok) throw new Error('탈퇴 실패')
    },
    onSuccess: () => {
      queryClient.setQueryData(['auth', 'me'], null)
      queryClient.invalidateQueries({ queryKey: ['auth'] })
    },
  })

  return {
    user: user ?? null,
    isLoggedIn: !!user,
    isLoading,
    logout: logoutMutation.mutate,
    withdraw: withdrawMutation.mutate,
    isLoggingOut: logoutMutation.isPending,
  }
}

/** 카카오 로그인 URL 생성 */
export function getKakaoLoginUrl(): string {
  const meta = document.querySelector(
    'meta[name="kakao-client-id"]',
  ) as HTMLMetaElement | null
  const clientId = meta?.content ?? ''
  const redirectUri = `${window.location.origin}/kakao/callback`
  return `https://kauth.kakao.com/oauth/authorize?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code`
}
