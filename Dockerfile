# Etapa 1: build da aplicação com Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia os arquivos de configuração do Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Baixa dependências (para cache mais eficiente)
RUN ./mvnw dependency:go-offline

# Copia o restante do projeto
COPY src ./src

# Executa o build (gera o JAR final)
RUN ./mvnw clean package -DskipTests

# Etapa 2: imagem final, apenas com o JAR
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copia o JAR da etapa anterior
COPY --from=build /app/target/AVALIA-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta usada pelo Spring Boot
EXPOSE 8080

# Comando que inicia a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
