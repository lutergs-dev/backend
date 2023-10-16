FROM ghcr.io/graalvm/graalvm-community:21-ol9 AS builder

ARG ACTIVE_PROFILES
ENV JAVA_VERSION=21

RUN mkdir /lutergs-backend
COPY . /lutergs-backend
RUN echo "spring.profiles.active=${ACTIVE_PROFILES}" > /lutergs-backend/src/main/resources/application.properties
WORKDIR /lutergs-backend

RUN ./gradlew nativeCompile


FROM ubuntu:latest

WORKDIR /
COPY --from=builder /lutergs-backend/build/native/nativeCompile/lutergs-backend application
COPY --from=builder /lutergs-backend/private.pem private.pem
CMD ["/application", "-Duser.timezone=Asia/Seoul"]

