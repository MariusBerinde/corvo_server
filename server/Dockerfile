FROM openjdk:24-jdk-slim

# Crea un utente non-root per sicurezza
RUN addgroup --system spring && adduser --system spring --ingroup spring

WORKDIR /app

# Copia il JAR
COPY target/*.jar app.jar

# Cambia ownership del file
RUN chown spring:spring app.jar

# Passa all'utente non-root
USER spring

# Esponi la porta (configurabile)
EXPOSE ${SERVER_PORT:-8083}

ENTRYPOINT ["java","-jar","app.jar"]
