# Multi-stage build para optimizar el tamaño de la imagen final
FROM eclipse-temurin:21-jdk-jammy AS builder

# Instalar herramientas necesarias y curl para health checks
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./

# Dar permisos de ejecución al wrapper de Gradle
RUN chmod +x gradlew

# Descargar dependencias (esto se cachea si no cambian)
RUN ./gradlew dependencies --no-daemon

# Copiar código fuente
COPY src/ src/

# Construir la aplicación
RUN ./gradlew bootJar --no-daemon

# Crear imagen final más liviana
FROM eclipse-temurin:21-jre-jammy

# Instalar curl para health checks de Render
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Crear usuario no-root para seguridad
RUN groupadd -r spring && useradd -r -g spring spring

# Crear directorio para la aplicación
WORKDIR /app

# Copiar el JAR construido desde la etapa anterior
COPY --from=builder /app/build/libs/*.jar app.jar

# Cambiar permisos del archivo
RUN chown spring:spring app.jar

# Cambiar al usuario no-root
USER spring

# Exponer puerto (Render usa variable PORT)
EXPOSE $PORT
EXPOSE 8080

# Configurar JVM para contenedores cloud (optimizado para Render)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

# Punto de entrada con configuración optimizada para Render
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]

# Healthcheck para verificar que la aplicación esté funcionando
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

