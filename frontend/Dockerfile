# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Serve stage
FROM nginx:alpine
# Copy the built app to nginx html folder
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Copy custom nginx config if needed (we'll use default for now, but redirect 404 to index.html for SPA)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
