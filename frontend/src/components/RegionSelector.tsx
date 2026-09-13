import { groupLawdCodesBySido } from '../lib/lawd-codes';

interface Props {
  currentCode: string;
  onSelect: (code: string, name: string) => void;
}

export default function RegionSelector({ currentCode, onSelect }: Props) {
  const groups = groupLawdCodesBySido();

  return (
    <select
      value={currentCode}
      onChange={(e) => {
        const code = e.target.value;
        // Find the name from groups
        for (const g of groups) {
          const entry = g.codes.find((c) => c.code === code);
          if (entry) {
            onSelect(code, entry.name);
            break;
          }
        }
      }}
      className="shrink-0 px-3 py-1.5 text-sm rounded-xl bg-white/20 text-white
                 focus:outline-none focus:ring-2 focus:ring-white/40 max-w-[160px]
                 [&>optgroup]:text-gray-900 [&>option]:text-gray-900"
    >
      {groups.map((g) => (
        <optgroup key={g.sido} label={g.sido}>
          {g.codes.map((c) => (
            <option key={c.code} value={c.code}>
              {c.name.replace(g.sido + ' ', '')}
            </option>
          ))}
        </optgroup>
      ))}
    </select>
  );
}
