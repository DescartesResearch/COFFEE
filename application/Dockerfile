#
# Build stage
#
FROM maven:3.8.5-jdk-11 AS build
COPY /shared/src /home/shared/src
COPY /shared/pom.xml /home/shared/pom.xml
COPY /application/src/main/java/application /home/app/src/main/java/application
COPY /application/pom.xml /home/app/pom.xml
COPY /application/src/main/resources/application.properties /home/app/src/main/resources/application.properties
RUN mvn -f /home/shared/pom.xml clean install
RUN mvn -f /home/app/pom.xml clean package spring-boot:repackage

#
# Package stage
#
FROM openjdk:11-slim
# alternative: FROM gcr.io/distroless/java 
COPY --from=build /home/app/target/application-0.1-SNAPSHOT.jar /usr/local/lib/application.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/application.jar"]