FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew \
    && ./gradlew --no-daemon dependencies

COPY src src

RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

RUN useradd --system --uid 10001 --create-home spring

COPY --from=builder --chown=spring:spring /workspace/build/libs/*.jar app.jar

USER spring

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
