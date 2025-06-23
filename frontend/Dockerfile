# Build Stage: Node.js를 사용하여 프론트엔드 애플리케이션 빌드
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json ./
RUN npm install --ci # npm 사용 시

# .env 파일을 /app 디렉토리로 복사
COPY .env.production .env

COPY . .

RUN yarn build

# 2. Serve Stage: Nginx를 사용하여 빌드된 정적 파일 서빙
FROM nginx:alpine

# Nginx 기본 설정 파일을 제거하고, 프로젝트의 Nginx 설정 파일 복사
# 만약 필요하다면, frontend-app/nginx/default.conf 파일을 생성하여 Nginx 설정을 커스터마이징합니다.
# 일반적으로는 빌드된 SPA의 index.html을 서빙하도록 설정합니다.
# 예시 nginx/default.conf 내용:
# server {
#     listen 80;
#     location / {
#         root   /usr/share/nginx/html;
#         index  index.html index.htm;
#         try_files $uri $uri/ /index.html; # SPA를 위한 설정 (새로고침 시 경로 문제 방지)
#     }
#     # /api 경로로 들어오는 요청을 백엔드 서비스로 프록시 (옵션)
#     # location /api/ {
#     #     proxy_pass http://spring_backend:8080/; # spring_backend는 Docker Compose 서비스 이름
#     #     proxy_set_header Host $host;
#     #     proxy_set_header X-Real-IP $remote_addr;
#     #     proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
#     # }
#     error_page 500 502 503 504  /50x.html;
#     location = /50x.html {
#         root   /usr/share/nginx/html;
#     }
# }
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# 빌드 스테이지에서 생성된 빌드 결과물을 Nginx의 웹 루트로 복사
# React:
COPY --from=builder /app/dist /usr/share/nginx/html
# Vue:
# COPY --from=builder /app/dist /usr/share/nginx/html
# Angular:
# COPY --from=builder /app/dist/your-app-name/browser /usr/share/nginx/html # 'your-app-name'은 실제 프로젝트 이름

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]