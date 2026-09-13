import type { CalcInputs, CalcResult, BreakdownPoint, FunFacts } from './types'
import { generateMemeText } from './meme'

const MAX_MONTHS = 1200;
const SCAN_STEP = 6;

/**
 * t개월 후 누적 자산
 * r = 월이율, S = 월저축액
 */
function assetsAt(t: number, currentAssets: number, S: number, r: number): number {
  if (r === 0) return currentAssets + S * t;
  return currentAssets * Math.pow(1 + r, t) + S * (Math.pow(1 + r, t) - 1) / r;
}

/**
 * t개월 후 필요 자기자본
 * g = 연간 집값 상승률
 */
function requiredEquityAt(
  t: number,
  housePrice: number,
  g: number,
  loanLtv: number,
): number {
  return housePrice * Math.pow(1 + g, t / 12) * (1 - loanLtv);
}

/** 재미 요소 계산 */
function computeFun(
  totalMonths: number,
  reachable: boolean,
  requiredEquity: number,
  birthYear?: number,
  housePrice?: number,
): FunFacts {
  const base = housePrice ?? requiredEquity;
  const megaCoffee = Math.floor(base / 2000);
  const starbucksCoffee = Math.floor(base / 4500);
  const bbqChicken = Math.floor(base / 22000);

  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;
  const ageWhenDone =
    birthYear != null && reachable
      ? currentYear - birthYear + Math.floor((currentMonth + totalMonths - 1) / 12)
      : undefined;

  const memeText = generateMemeText({
    totalMonths,
    reachable,
    age: birthYear != null ? currentYear - birthYear : undefined,
  });

  return { ageWhenDone, megaCoffee, starbucksCoffee, bbqChicken, memeText };
}

/** 3개월 간격 breakdown 생성 */
function buildBreakdown(
  totalMonths: number,
  currentAssets: number,
  S: number,
  r: number,
  housePrice: number,
  g: number,
  loanLtv: number,
): BreakdownPoint[] {
  const points: BreakdownPoint[] = [];
  const step = 3;
  const cap = Math.min(totalMonths, MAX_MONTHS);
  for (let m = 0; m <= cap; m += step) {
    points.push({
      month: m,
      assets: Math.round(assetsAt(m, currentAssets, S, r)),
      target: Math.round(requiredEquityAt(m, housePrice, g, loanLtv)),
    });
  }
  if (cap % step !== 0) {
    points.push({
      month: cap,
      assets: Math.round(assetsAt(cap, currentAssets, S, r)),
      target: Math.round(requiredEquityAt(cap, housePrice, g, loanLtv)),
    });
  }
  return points;
}

export function calculate(inputs: CalcInputs): CalcResult {
  const {
    housePrice,
    annualIncome,
    takeHomeRatio,
    savingRate,
    currentAssets,
    savingsApr,
    housePriceGrowth,
    loanLtv,
    birthYear,
  } = inputs;

  const monthlyTakeHome = (annualIncome / 12) * takeHomeRatio;
  const S = monthlyTakeHome * savingRate;
  const r = savingsApr / 12;
  const g = housePriceGrowth;

  const initialEquity = Math.round(housePrice * (1 - loanLtv));
  const G0 = initialEquity - currentAssets;

  // 즉시 구매 가능
  if (G0 <= 0) {
    const fun = computeFun(0, true, 0, birthYear, housePrice);
    return {
      reachable: true,
      totalMonths: 0,
      years: 0,
      restMonths: 0,
      monthlySaving: Math.round(S),
      requiredEquity: Math.round(initialEquity),
      totalInterest: 0,
      breakdown: [{ month: 0, assets: currentAssets, target: Math.round(initialEquity) }],
      fun,
    };
  }

  // 월 저축액이 0 이하면 도달 불가
  if (S <= 0) {
    const reqEq = Math.round(requiredEquityAt(0, housePrice, g, loanLtv));
    const fun = computeFun(0, false, reqEq, birthYear, housePrice);
    return {
      reachable: false,
      totalMonths: 0,
      years: 0,
      restMonths: 0,
      monthlySaving: 0,
      requiredEquity: reqEq,
      totalInterest: 0,
      breakdown: [],
      fun,
    };
  }

  // 케이스 1: 이율 0, 상승률 0 → 단순 나눗셈
  if (g === 0 && r === 0) {
    const months = Math.ceil(G0 / S);
    if (months > MAX_MONTHS) {
      const reqEq = Math.round(initialEquity);
      return {
        reachable: false,
        totalMonths: 0,
        years: 0,
        restMonths: 0,
        monthlySaving: Math.round(S),
        requiredEquity: reqEq,
        totalInterest: 0,
        breakdown: buildBreakdown(MAX_MONTHS, currentAssets, S, r, housePrice, g, loanLtv),
        fun: computeFun(0, false, reqEq, birthYear, housePrice),
      };
    }
    const years = Math.floor(months / 12);
    const restMonths = months % 12;
    const reqEq = Math.round(initialEquity);
    return {
      reachable: true,
      totalMonths: months,
      years,
      restMonths,
      monthlySaving: Math.round(S),
      requiredEquity: reqEq,
      totalInterest: 0,
      breakdown: buildBreakdown(months, currentAssets, S, r, housePrice, g, loanLtv),
      fun: computeFun(months, true, reqEq, birthYear, housePrice),
    };
  }

  // 케이스 2 & 3: 6개월 단위 조탐색 + 이분 탐색
  const diff = (t: number) =>
    assetsAt(t, currentAssets, S, r) - requiredEquityAt(t, housePrice, g, loanLtv);

  let foundMonth = -1;

  for (let t = SCAN_STEP; t <= MAX_MONTHS; t += SCAN_STEP) {
    if (diff(t) >= 0) {
      // 이분 탐색: [t - SCAN_STEP + 1, t] 구간에서 최초 도달 월 탐색
      let lo = t - SCAN_STEP + 1;
      let hi = t;
      while (lo < hi) {
        const mid = Math.floor((lo + hi) / 2);
        if (diff(mid) >= 0) {
          hi = mid;
        } else {
          lo = mid + 1;
        }
      }
      foundMonth = lo;
      break;
    }
  }

  if (foundMonth === -1) {
    const reqEq = Math.round(requiredEquityAt(MAX_MONTHS, housePrice, g, loanLtv));
    return {
      reachable: false,
      totalMonths: 0,
      years: 0,
      restMonths: 0,
      monthlySaving: Math.round(S),
      requiredEquity: reqEq,
      totalInterest: 0,
      breakdown: buildBreakdown(MAX_MONTHS, currentAssets, S, r, housePrice, g, loanLtv),
      fun: computeFun(0, false, reqEq, birthYear, housePrice),
    };
  }

  const totalMonths = foundMonth;
  const years = Math.floor(totalMonths / 12);
  const restMonths = totalMonths % 12;
  const finalAssets = assetsAt(totalMonths, currentAssets, S, r);
  const pureDeposit = currentAssets + S * totalMonths;
  const totalInterest = Math.round(finalAssets - pureDeposit);
  const reqEq = Math.round(requiredEquityAt(totalMonths, housePrice, g, loanLtv));

  return {
    reachable: true,
    totalMonths,
    years,
    restMonths,
    monthlySaving: Math.round(S),
    requiredEquity: reqEq,
    totalInterest: Math.max(0, totalInterest),
    breakdown: buildBreakdown(totalMonths, currentAssets, S, r, housePrice, g, loanLtv),
    fun: computeFun(totalMonths, true, reqEq, birthYear, housePrice),
  };
}
