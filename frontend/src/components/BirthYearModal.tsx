import { useState } from 'react';
import { apiFetch } from '../api/client';

interface Props {
  onSaved: (birthYear: number) => void;
  onClose?: () => void;
  defaultValue?: number;
}

export default function BirthYearModal({ onSaved, onClose, defaultValue }: Props) {
  const currentYear = new Date().getFullYear();
  const [value, setValue] = useState(defaultValue?.toString() ?? '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async () => {
    const year = Number(value);
    if (!value || isNaN(year) || year < 1920 || year > currentYear) {
      setError(`1920 ~ ${currentYear} 사이의 출생연도를 입력해주세요.`);
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await apiFetch('/api/profile', {
        method: 'PUT',
        body: JSON.stringify({ birthYear: year }),
      });
      onSaved(year);
    } catch {
      setError('저장에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl p-8 mx-4 max-w-sm w-full text-center">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-1">
          {defaultValue ? '출생연도 변경' : '출생연도를 알려주세요'}
        </h2>
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">
          태어나서 지금까지 몇 잔의 아아와<br />몇 마리의 치킨을 먹었는지 알려드릴게요!
        </p>

        <input
          type="number"
          inputMode="numeric"
          placeholder="예: 1995"
          value={value}
          onChange={(e) => { setValue(e.target.value); setError(null); }}
          onKeyDown={(e) => { if (e.key === 'Enter') handleSubmit(); }}
          className="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700
                     bg-gray-50 dark:bg-gray-800 text-center text-lg font-semibold
                     text-gray-900 dark:text-white placeholder-gray-400
                     focus:outline-none focus:ring-2 focus:ring-brand-500"
          autoFocus
        />

        {error && (
          <p className="mt-2 text-xs text-red-500">{error}</p>
        )}

        <button
          onClick={handleSubmit}
          disabled={saving || !value}
          className="mt-4 w-full py-3 rounded-xl font-semibold text-sm text-white
                     bg-brand-500 hover:bg-brand-600 transition-colors
                     disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {saving ? '저장 중...' : '확인'}
        </button>

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
