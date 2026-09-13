import { Link } from 'react-router-dom'

export default function PrivacyPage() {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950">
      <header className="bg-brand-500 dark:bg-brand-600 px-4 py-3">
        <div className="max-w-2xl mx-auto flex items-center gap-3">
          <Link to="/" className="text-white hover:text-white/80">
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
            </svg>
          </Link>
          <h1 className="text-lg font-bold text-white">개인정보 처리방침</h1>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6">
        <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-sm p-6 space-y-6 text-sm text-gray-700 dark:text-gray-300 leading-relaxed">

          <p className="text-xs text-gray-400 dark:text-gray-500">시행일: 2026년 9월 13일 | 버전: 1.0</p>

          <section>
            <h2 className="text-base font-bold text-gray-900 dark:text-white mb-2">1. 개인정보 수집 항목 및 수집 방법</h2>
            <p className="mb-2">ZIPIMONEY(이하 "서비스")는 다음과 같은 개인정보를 수집합니다.</p>
            <table className="w-full text-xs border-collapse">
              <thead>
                <tr className="bg-gray-50 dark:bg-gray-800">
                  <th className="border border-gray-200 dark:border-gray-700 px-3 py-2 text-left">구분</th>
                  <th className="border border-gray-200 dark:border-gray-700 px-3 py-2 text-left">수집 항목</th>
                  <th className="border border-gray-200 dark:border-gray-700 px-3 py-2 text-left">수집 방법</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2 font-medium">필수</td>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2">카카오 고유 ID, 이름, 닉네임, 프로필 이미지</td>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2">카카오 로그인</td>
                </tr>
                <tr>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2 font-medium">선택</td>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2">이메일, 성별, 연령대</td>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2">카카오 로그인 (동의 시)</td>
                </tr>
                <tr>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2 font-medium">자동 수집</td>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2">위치 정보(GPS 좌표), IP 주소, 브라우저 정보</td>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2">서비스 이용 시</td>
                </tr>
                <tr>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2 font-medium">서비스 이용</td>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2">연봉/소득, 저축률, 보유 자산, 대출 비율 등 계산 입력값, 계산 결과</td>
                  <td className="border border-gray-200 dark:border-gray-700 px-3 py-2">사용자 직접 입력</td>
                </tr>
              </tbody>
            </table>
          </section>

          <section>
            <h2 className="text-base font-bold text-gray-900 dark:text-white mb-2">2. 개인정보 수집 및 이용 목적</h2>
            <ul className="list-disc pl-5 space-y-1">
              <li><strong>회원 식별 및 인증:</strong> 카카오 로그인을 통한 회원 관리</li>
              <li><strong>서비스 제공:</strong> 부동산 자금 계산, 실거래가 조회, 계산 이력 관리</li>
              <li><strong>위치 기반 서비스:</strong> 현재 위치 기반 지역 부동산 데이터 제공</li>
              <li><strong>서비스 개선:</strong> 이용 통계 분석 및 서비스 품질 향상</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-gray-900 dark:text-white mb-2">3. 개인정보 보유 및 이용 기간</h2>
            <ul className="list-disc pl-5 space-y-1">
              <li><strong>회원 정보:</strong> 회원 탈퇴 시까지 (탈퇴 후 즉시 파기)</li>
              <li><strong>계산 이력:</strong> 회원 탈퇴 시까지</li>
              <li><strong>위치 정보:</strong> 일시적 이용 후 즉시 파기 (별도 저장하지 않음)</li>
              <li><strong>동의 기록:</strong> 관련 법령에 따라 3년 보관</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-gray-900 dark:text-white mb-2">4. 개인정보의 제3자 제공</h2>
            <p>서비스는 이용자의 개인정보를 제3자에게 제공하지 않습니다. 다만, 다음의 경우는 예외로 합니다.</p>
            <ul className="list-disc pl-5 space-y-1 mt-1">
              <li>이용자가 사전에 동의한 경우</li>
              <li>법령에 의해 요구되는 경우</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-gray-900 dark:text-white mb-2">5. 개인정보의 파기</h2>
            <p>회원 탈퇴 시 개인정보는 즉시 파기됩니다. 전자적 파일은 복구 불가능한 방법으로 삭제하며, 종이 문서는 분쇄 또는 소각합니다.</p>
          </section>

          <section>
            <h2 className="text-base font-bold text-gray-900 dark:text-white mb-2">6. 이용자의 권리</h2>
            <p>이용자는 언제든지 다음의 권리를 행사할 수 있습니다.</p>
            <ul className="list-disc pl-5 space-y-1 mt-1">
              <li>개인정보 열람 요구</li>
              <li>개인정보 정정·삭제 요구</li>
              <li>개인정보 처리 정지 요구</li>
              <li>회원 탈퇴 (앱 내 설정에서 직접 가능)</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-gray-900 dark:text-white mb-2">7. 쿠키 및 인증 정보</h2>
            <p>서비스는 JWT 기반 인증 토큰을 쿠키에 저장하여 로그인 상태를 유지합니다. 브라우저 설정에서 쿠키를 비활성화할 수 있으나, 이 경우 서비스 이용이 제한될 수 있습니다.</p>
          </section>

          <section>
            <h2 className="text-base font-bold text-gray-900 dark:text-white mb-2">8. 개인정보 보호책임자</h2>
            <p>개인정보 관련 문의사항은 아래로 연락해주시기 바랍니다.</p>
            <ul className="list-none pl-0 mt-1 space-y-0.5">
              <li>서비스명: ZIPIMONEY</li>
              <li>이메일: support@zipimoney.com</li>
            </ul>
          </section>

          <section>
            <h2 className="text-base font-bold text-gray-900 dark:text-white mb-2">9. 개인정보 처리방침 변경</h2>
            <p>본 방침은 시행일로부터 적용되며, 변경 시 서비스 내 공지를 통해 안내합니다.</p>
          </section>
        </div>
      </main>
    </div>
  )
}
