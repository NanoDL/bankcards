# --- Этап 1: Сборка проекта с использованием JDK 21 ---
FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /app

# Копируем файлы проекта для сборки
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Скачиваем зависимости
RUN ./mvnw dependency:go-offline

# Копируем исходный код
COPY src ./src

# Собираем проект с помощью Maven
RUN ./mvnw clean package -DskipTests


# --- Этап 2: Создание финального образа с использованием JRE 21 ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Указываем аргумент для WAR файла
ARG WAR_FILE=target/*.war

# Копируем собранный WAR-файл из этапа "builder"
COPY --from=builder /app/${WAR_FILE} app.war

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.war"]