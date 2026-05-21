FROM node:24.15.0-slim AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.30.1-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
