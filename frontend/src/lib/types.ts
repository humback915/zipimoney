export interface CalcInputs {
  housePrice: number
  annualIncome: number
  takeHomeRatio: number
  savingRate: number
  currentAssets: number
  savingsApr: number
  housePriceGrowth: number
  loanLtv: number
  birthYear?: number
}

export interface BreakdownPoint {
  month: number
  assets: number
  target: number
}

export interface FunFacts {
  ageWhenDone?: number
  megaCoffee: number
  starbucksCoffee: number
  bbqChicken: number
  memeText: string
}

export interface CalcResult {
  reachable: boolean
  totalMonths: number
  years: number
  restMonths: number
  monthlySaving: number
  requiredEquity: number
  totalInterest: number
  breakdown: BreakdownPoint[]
  fun: FunFacts
}

export interface AptDeal {
  name: string
  dong: string
  area: number
  price: number
  floor: number
  dealYear: number
  dealMonth: number
  dealDay: number
  buildYear: number
  roadName: string
}

export type PropertyType = 'all' | 'apt' | 'villa' | 'officetel' | 'house'

export interface AptComplex {
  name: string
  dong: string
  propertyType?: string
  lat: number
  lng: number
  avgPrice: number
  deals: AptDeal[]
  buildYear: number
}

export interface MemeInput {
  sigungu?: string
  area?: number
  totalMonths: number
  reachable: boolean
  age?: number
}

export interface LawdCode {
  code: string
  name: string
}
