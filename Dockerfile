ARG UBI=quay.io/quarkus/ubi-quarkus-native-image:22.0.0-java17
FROM ${UBI} AS build
COPY --chown=quarkus:quarkus . /opt/quarkus-src
USER quarkus
WORKDIR /opt/quarkus-src
RUN ./mvnw -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline
RUN ./mvnw -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -f /opt/quarkus-src/pom.xml \
    package

FROM  quay.io/quarkus/ubi-quarkus-native-image:22.0.0-java17 AS runtime
COPY --from=build --chown=quarkus:quarkus /opt/quarkus-src/target/quarkus-app /opt/quarkus-app/
ENTRYPOINT ["java","-jar","/opt/quarkus-app/quarkus-run.jar"]
