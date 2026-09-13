import { Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import KakaoCallback from './pages/KakaoCallback'
import SharePage from './pages/SharePage'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/kakao/callback" element={<KakaoCallback />} />
      <Route path="/s/:shareKey" element={<SharePage />} />
    </Routes>
  )
}
