# Usar imagen oficial de Java 21
FROM eclipse-temurin:21-jdk-alpine AS builder

# Directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Dar permisos a Maven wrapper
RUN chmod +x mvnw

# Copiar código fuente
COPY src src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Imagen final más ligera
FROM eclipse-temurin:21-jre-alpine

# Crear usuario no root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR construido
COPY --from=builder /app/target/*.jar app.jar

# Exponer puerto (Render usará el puerto que le asignen)
EXPOSE 8080

# Parámetros JVM optimizados para 512MB RAM
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]