FROM maven:3.8.4-openjdk-17-slim AS build-env
WORKDIR /app
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2  \
    mvn dependency:go-offline

FROM build-env AS build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
     mvn clean install -DskipTests

FROM build AS test
RUN --mount=type=cache,target=/root/.m2 \
    mvn test

FROM openjdk:17.0.2-oracle AS app
WORKDIR /app
COPY --from=build /app/target/funds-loader-0.0.1-SNAPSHOT.jar /app/funds-loader.jar
ENTRYPOINT ["java", "-jar", "funds-loader.jar"]

