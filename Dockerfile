FROM maven:3-openjdk-11 as build
ARG APIKEY
WORKDIR /src
ADD . /src/
ENV APIKEY=$APIKEY
RUN mvn clean package test -P e2e-test

FROM openjdk:11-jdk-slim
COPY --from=build /src/target/tiny-weather-bulletin-*.jar app.jar
EXPOSE 9101
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
