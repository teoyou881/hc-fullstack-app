import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {BrowserRouter} from 'react-router-dom';
import {QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {ReactQueryDevtools} from '@tanstack/react-query-devtools'; // 선택 사항: 개발 도구

const queryClient = new QueryClient({
  defaultOptions:{
    queries:{
      staleTime           :1000 * 60 * 5, // 5분 동안 fresh 상태 유지 (기본 0)
      gcTime              :1000 * 60 * 10, // 10분 동안 캐시된 데이터 유지 (기본 5분)
      refetchOnWindowFocus:true, // 윈도우 포커스 시 재요청 (기본 true)
    },
  },
}); // QueryClient 인스턴스 생성

createRoot(document.getElementById('root')).render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter basename={'/'}>
        <App/>
      </BrowserRouter>,
      <ReactQueryDevtools initialIsOpen={false}/>
    </QueryClientProvider>,
)
