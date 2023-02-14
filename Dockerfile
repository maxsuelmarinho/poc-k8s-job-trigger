FROM azul/zulu-openjdk-debian:17.0.3 as builder
WORKDIR /app
COPY *.gradle gradlew /app/
COPY ./gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version
RUN ./gradlew --write-verification-metadata sha256 help
COPY . .
RUN ./gradlew --stacktrace --info build -xtest

FROM azul/zulu-openjdk-debian:17.0.3
WORKDIR /app
COPY --from=builder /app/build/libs/poc-k8s-job-trigger*.jar app.jar
RUN sh -c 'touch /app/app.jar'
CMD ["java", "--show-version", "-jar", "app.jar"]
