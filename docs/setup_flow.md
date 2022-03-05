# Today we'll build

- Sample "Pet Care" application
- Service-oriented
- Secure from the start
- Multi-module

Featuring:
- Quarkus
- Keycloak

# Create root dir
mkdir petcare

# Create "core" module
quarkus create cli ptc-core --java=17 --help
quarkus dev

# Create "api" module
quarkus create app --java=17 --help
mvn quarkus dev

# Create "app" module
gh repo clone vaadin/base-starter-flow-quarkus

