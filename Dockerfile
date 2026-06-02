FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY clublago/.mvn clublago/.mvn
COPY clublago/mvnw clublago/pom.xml clublago/
RUN cd clublago && chmod +x mvnw

COPY clublago/src clublago/src
RUN cd clublago && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/clublago/target/clublago-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
