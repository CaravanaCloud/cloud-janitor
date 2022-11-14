# ARG UBI=ghcr.io/graalvm/graalvm-ce:latest
# ARG UBI=quay.io/quarkus/ubi-quarkus-native-image:22.0-java17
# ARG UBI=registry.access.redhat.com/ubi8/ubi-minimal
#TODO: Avoid security vulnerabilities by finding a minimal safe UBI
ARG UBI=ghcr.io/graalvm/native-image:latest

FROM ${UBI} AS build
COPY . /opt/quarkus-src
WORKDIR /opt/quarkus-src
RUN ./mvnw -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline
RUN ./mvnw -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -f /opt/quarkus-src/pom.xml \
    package

FROM ${UBI} AS runtime
COPY --from=build /opt/quarkus-src/target/quarkus-app /opt/quarkus-app/
ENTRYPOINT ["java","-jar","/opt/quarkus-app/quarkus-run.jar"]
