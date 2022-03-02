# Project Base for Vaadin Flow and Quarkus

This project can be used as a starting point to create your own Vaadin Flow application for Quarkus. It contains all the necessary configuration with some placeholder files to get you started.

Quarkus 2.0+ requires Java 11. Java 8 is still supported for 1.11+ releases, which should work with this starter.

Starter is also available for [gradle](https://github.com/vaadin/base-starter-flow-quarkus/tree/gradle)

## Running the Application

Import the project to the IDE of your choosing as a Maven project. 

Run application using `mvnw` (Windows), or `./mvnw` (Mac & Linux).

Open [http://localhost:8080/](http://localhost:8080/) in browser.

If you want to run your app locally in production mode, call `mvnw package -Pproduction` (Windows), or `./mvnw package -Pproduction` (Mac & Linux)
and then
```
java -jar target/quarkus-app/quarkus-run.jar
```
