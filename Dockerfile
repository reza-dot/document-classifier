FROM eclipse-temurin:17-jdk AS build

# Use separate directory for code to avoid conflicts with default folders.
WORKDIR /app

# Download dependencies as a distinct layer.
# As layers are cached and packages updated infrequently this increases build speed.
COPY .mvn ./.mvn/
COPY mvnw pom.xml ./
RUN apt-get update
RUN apt-get -y install tesseract-ocr
RUN ldconfig
RUN ./mvnw dependency:go-offline

# Copy everything else and build
COPY . ./
RUN ./mvnw package

# Second (and final) stage starts here. All intermediate stages are deleted after build.
FROM eclipse-temurin:17-jre

RUN apt-get update
RUN apt-get -y install tesseract-ocr
RUN ldconfig
WORKDIR /app

EXPOSE 8080
COPY --from=build /app/target/document-classifier-1.0.jar ./
ENTRYPOINT [ "java", "-jar", "document-classifier-1.0.jar" ]



