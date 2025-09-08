# Spring Boot Dockerfile
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Копирование всего проекта
COPY . .

RUN ls -la src/main/resources/db/migration/

# Сборка Spring Boot приложения
RUN mvn clean package -DskipTests

# Runtime образ
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Копирование WAR файла из build stage
COPY --from=build /app/target/*.war app.war

# Открытие порта
EXPOSE 8070

# Запуск Spring Boot приложения
ENTRYPOINT ["java", "-jar", "app.war"]