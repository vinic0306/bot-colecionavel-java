# Usa uma imagem do Java 17 com Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Executa o JAR do bot
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar
CMD ["java", "-jar", "app.jar"]
