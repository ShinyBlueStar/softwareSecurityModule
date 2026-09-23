### ===== Stage 1: Maven Build =====
FROM eclipse-temurin:21-jdk-alpine AS maven_builder

# Optional: point at an internal Nexus mirror instead of Maven Central.
# Leave both empty (the default) to build against public Maven Central only.
ARG NEXUS_USERNAME=
ARG NEXUS_PASSWORD=
ARG NEXUS_MIRROR_URL=

RUN apk add --no-cache maven

RUN if [ -n "$NEXUS_MIRROR_URL" ]; then \
      mkdir -p /root/.m2 && \
      { \
        echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"'; \
        echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"'; \
        echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">'; \
        echo '  <servers><server><id>private-nexus</id>'; \
        echo "      <username>${NEXUS_USERNAME}</username>"; \
        echo "      <password>${NEXUS_PASSWORD}</password>"; \
        echo '  </server></servers>'; \
        echo '  <mirrors><mirror><id>internal_nexus</id>'; \
        echo '      <mirrorOf>*</mirrorOf>'; \
        echo "      <url>${NEXUS_MIRROR_URL}</url>"; \
        echo '  </mirror></mirrors></settings>'; \
      } > /root/.m2/settings.xml; \
    fi

WORKDIR /app
# Build context is the repository root (see docker-compose.yml / CI).
COPY . .
RUN mvn -B clean package -DskipTests -Dmaven.test.skip=true

RUN cp ssm-container/target/ssm-container-*.jar /app/app.jar

### ===== Stage 2: Runtime =====
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S ssm && adduser -S ssm -G ssm
WORKDIR /app
COPY --from=maven_builder /app/app.jar app.jar
RUN chown ssm:ssm app.jar
USER ssm

ENV SPRING_PROFILES_ACTIVE=development
EXPOSE 8009

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- --no-check-certificate https://localhost:8009/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
