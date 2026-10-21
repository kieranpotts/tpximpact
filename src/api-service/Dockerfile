# syntax=docker/dockerfile:1

#
# Builds the API service's image, from this directory alone.
#
# See: docs > decisions > 0016.
#

# Build stage. Compiles the service, and lays out its runtime classpath.
FROM eclipse-temurin:25-jdk AS build

WORKDIR /build

# Fetch Gradle and the dependencies first, so a change to the code alone
# doesn't download them again.
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon --quiet dependencies --configuration runtimeClasspath > /dev/null

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon installDist

# Runtime stage. A Debian base with a Java runtime, and no build tools.
FROM debian:trixie-slim

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=eclipse-temurin:25-jre /opt/java/openjdk /opt/java/openjdk

RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --no-create-home app

WORKDIR /app
COPY --from=build /build/build/install/api-service/lib ./lib

USER 10001:10001
EXPOSE 8080

# Netty loads its native transport library, which needs native access.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "--enable-native-access=ALL-UNNAMED", \
    "-cp", "/app/lib/*", "com.tpximpact.urlshortener.ApplicationKt"]
