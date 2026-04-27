# syntax=docker/dockerfile:1.7

# === Stage 1: build ===
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
# Cache de dependências: pré-baixa antes de copiar o código.
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package

# === Stage 2: runtime ===
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário não-root para reduzir superfície em caso de RCE.
RUN addgroup -S serveflow && adduser -S serveflow -G serveflow

COPY --from=build /workspace/target/serveflow-*.jar /app/app.jar
RUN chown -R serveflow:serveflow /app
USER serveflow

EXPOSE 8080

# Healthcheck básico (Spring Actuator não está habilitado; usa endpoint público).
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/v3/api-docs >/dev/null 2>&1 || exit 1

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
