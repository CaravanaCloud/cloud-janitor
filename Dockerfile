# docker build --pull --no-cache --tag cloud-janitor:latest --progress=plain .

ARG UBI=quay.io/quarkus/ubi-quarkus-native-image:22.3-java17
# Alternative base images:
# ARG UBI=registry.access.redhat.com/ubi8/ubi-minimal
# ARG UBI=ghcr.io/graalvm/graalvm-ce:latest
# ADD ARG UBI=ghcr.io/graalvm/native-image:latest

FROM ${UBI} AS build

USER root
RUN mkdir /opt/quarkus-src
COPY . /opt/quarkus-src
WORKDIR /opt/quarkus-src
RUN ./mvnw -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline
RUN ./mvnw -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -f /opt/quarkus-src/pom.xml \
    package
RUN chown -R quarkus:quarkus /opt/quarkus-src

FROM ${UBI} AS runtime
COPY --from=build /opt/quarkus-src/target/quarkus-app /opt/quarkus-app/
USER root
# Install utilities
RUN microdnf install which wget
RUN bash -c "curl 'https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip' -o 'awscliv2.zip' && unzip awscliv2.zip && ./aws/install && aws --version"
# Check Installs
RUN which --version
RUN wget --version
RUN curl --version
RUN aws --version
RUN chown -R quarkus:quarkus /opt/quarkus-app
USER quarkus
ENTRYPOINT ["java","-jar","/opt/quarkus-app/quarkus-run.jar"]
