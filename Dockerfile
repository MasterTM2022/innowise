FROM maven:latest AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

RUN mkdir -p build && cp target/*.jar build/app.jar


FROM eclipse-temurin:21-jre-jammy AS runner

LABEL maintainer="aliaksandr.piarou@innowise.com"

ENV LANG=C.UTF-8

WORKDIR /app

COPY --from=builder /app/build/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]