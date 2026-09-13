import { useState, useEffect, useRef, useCallback } from 'react';

interface SearchResult {
  type: 'region' | 'complex';
  label: string;
  code?: string;
  name?: string;
  lat?: number;
  lng?: number;
}

interface Props {
  lawdCd: string;
  onSelectRegion: (code: string, name: string) => void;
  onSelectComplex: (name: string, lat: number, lng: number) => void;
}

export default function SearchBar({ lawdCd, onSelectRegion, onSelectComplex }: Props) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);
  const abortRef = useRef<AbortController | null>(null);

  // 디바운스 검색
  useEffect(() => {
    if (!query.trim()) {
      setResults([]);
      setOpen(false);
      return;
    }

    const timer = setTimeout(async () => {
      abortRef.current?.abort();
      const controller = new AbortController();
      abortRef.current = controller;

      setLoading(true);
      try {
        const res = await fetch(
          `/api/search?q=${encodeURIComponent(query)}&lawdCd=${lawdCd}`,
          { signal: controller.signal },
        );
        if (res.ok) {
          const json = await res.json();
          const data: SearchResult[] = json.data ?? [];
          setResults(data);
          setOpen(data.length > 0);
        }
      } catch {
        // abort or network error
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => {
      clearTimeout(timer);
      abortRef.current?.abort();
    };
  }, [query, lawdCd]);

  // 외부 클릭 시 닫기
  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const handleSelect = useCallback(
    (item: SearchResult) => {
      if (item.type === 'region' && item.code) {
        onSelectRegion(item.code, item.label);
      } else if (item.type === 'complex' && item.lat && item.lng && item.name) {
        onSelectComplex(item.name, item.lat, item.lng);
      }
      setQuery('');
      setOpen(false);
    },
    [onSelectRegion, onSelectComplex],
  );

  return (
    <div ref={wrapperRef} className="relative flex-1 min-w-0">
      <div className="relative">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => results.length > 0 && setOpen(true)}
          placeholder="지역·단지 검색"
          className="w-full pl-8 pr-3 py-1.5 text-sm
                     rounded-xl bg-white/20 text-white
                     focus:outline-none focus:ring-2 focus:ring-white/40
                     placeholder:text-white/60"
        />
        <svg
          className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-white/60"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        {loading && (
          <svg
            className="absolute right-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-white/60 animate-spin"
            viewBox="0 0 24 24"
            fill="none"
          >
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
        )}
      </div>

      {open && (
        <ul className="absolute top-full mt-1 w-full bg-white dark:bg-gray-800
                       rounded-xl shadow-lg z-50 max-h-60 overflow-y-auto">
          {results.map((item, i) => (
            <li key={`${item.type}-${item.code ?? item.name}-${i}`}>
              <button
                onClick={() => handleSelect(item)}
                className="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 dark:hover:bg-gray-700
                           flex items-center gap-2 transition-colors"
              >
                <span
                  className={`shrink-0 px-1.5 py-0.5 text-[10px] font-medium rounded-lg ${
                    item.type === 'region'
                      ? 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300'
                      : 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300'
                  }`}
                >
                  {item.type === 'region' ? '지역' : '단지'}
                </span>
                <span className="truncate dark:text-gray-200">{item.label}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
