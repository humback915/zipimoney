const BASE_URL = '';

interface ApiResponse<T> {
  data?: T;
  error?: {
    errorCode: string;
    errorMessage: string;
  };
}

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!res.ok) {
    const body = (await res.json().catch(() => ({}))) as ApiResponse<never>;
    throw new Error(body.error?.errorMessage ?? `HTTP ${res.status}`);
  }

  const body = (await res.json()) as ApiResponse<T>;
  return body.data as T;
}

/** GET helper */
export function apiGet<T>(path: string, signal?: AbortSignal): Promise<T> {
  return apiFetch<T>(path, { method: 'GET', signal });
}

/** POST helper */
export function apiPost<T>(path: string, data?: unknown): Promise<T> {
  return apiFetch<T>(path, {
    method: 'POST',
    body: data ? JSON.stringify(data) : undefined,
  });
}
