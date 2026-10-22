# ---- Compilación -----------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

# Primero solo los pom, para que Docker reutilice la caché de dependencias
# mientras no cambien.
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY oratge-domain/pom.xml oratge-domain/
COPY oratge-application/pom.xml oratge-application/
COPY oratge-adapter-in-web/pom.xml oratge-adapter-in-web/
COPY oratge-adapter-out-aemet/pom.xml oratge-adapter-out-aemet/
COPY oratge-bootstrap/pom.xml oratge-bootstrap/
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline || true

COPY . .
# Los tests ya los pasa GitHub Actions en cada push.
RUN ./mvnw -B -q -DskipTests package

# ---- Ejecución --------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 oratge
COPY --from=build /src/oratge-bootstrap/target/*.jar app.jar
USER oratge

# La JVM ajusta el heap al límite de memoria del contenedor.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
