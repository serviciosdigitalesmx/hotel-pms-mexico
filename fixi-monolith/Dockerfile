FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S fixi \
    && adduser -S -G fixi fixi

WORKDIR /app
COPY --chown=fixi:fixi fixi-monolith/build/libs/fixi-monolith.jar /app/app.jar

USER fixi:fixi
EXPOSE 8080 8090

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=15.0 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=20s --timeout=4s --start-period=180s --retries=5 \
  CMD curl --fail --silent http://127.0.0.1:8090/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
