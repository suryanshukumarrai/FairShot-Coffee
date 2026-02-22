# Multi-stage Dockerfile for FairShot Coffee application
# Stage 1: Build React frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/coffee-shop-frontend

# Copy frontend package files
COPY coffee-shop-frontend/package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY coffee-shop-frontend/src ./src
COPY coffee-shop-frontend/index.html ./
COPY coffee-shop-frontend/*.ts* ./
COPY coffee-shop-frontend/*.config.* ./

# Build frontend (skip tsc type checking, vite will handle it)
RUN npm run build

# Stage 2: Build Java backend and combine with frontend
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app

# Copy Maven build files
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copy source code
COPY src ./src

# Build backend JAR
RUN ./mvnw clean package -DskipTests

# Stage 3: Create runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy built JAR from backend-builder
COPY --from=backend-builder /app/target/Coffee-Shop-*.jar app.jar

# Copy built React frontend to backend's static resources
RUN mkdir -p /app/public
COPY --from=frontend-builder /app/coffee-shop-frontend/dist /app/public

# Expose port 8080 (internal) - Render will map to 10000
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run Spring Boot application
CMD ["java", "-jar", "app.jar"]
