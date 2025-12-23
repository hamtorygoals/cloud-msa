FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :ticketing-core:bootJar -x test

FROM eclipse-temurin:21-jre
COPY --from=build /app/ticketing-core/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]