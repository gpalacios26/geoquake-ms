# Imagen base de Java
FROM openjdk:17-jdk-slim

# Directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR al contenedor
COPY target/geoquake-0.0.1.jar geoquake-0.0.1.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Ejecutar la aplicación
CMD ["java", "-jar", "geoquake-0.0.1.jar"]