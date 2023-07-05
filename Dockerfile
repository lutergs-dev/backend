FROM ghcr.io/graalvm/graalvm-ce:ol9-java17-22.3.2 AS builder

ARG ACTIVE_PROFILES

RUN mkdir /lutergs-backend
COPY . /lutergs-backend
RUN echo "spring.profiles.active=${ACTIVE_PROFILES}" > /lutergs-backend/src/main/resources/application.properties
WORKDIR /lutergs-backend

RUN ./gradlew nativeCompile


FROM busybox:latest

WORKDIR /
COPY --from=builder /lutergs-backend/build/native/nativeCompile/lutergs-backend application
CMD ["/application", "-Duser.timezone=Asia/Seoul"]

