FROM eclipse-temurin:17-jdk-alpine
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseZGC"
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
