FROM node:26-alpine AS build
WORKDIR /app
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN npm install --global pnpm@12.10.1 && pnpm install --frozen-lockfile
COPY . .
RUN pnpm exec tsc -p tsconfig.fixi.json --noEmit
RUN pnpm exec vite build

FROM nginxinc/nginx-unprivileged:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
