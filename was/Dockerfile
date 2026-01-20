ARG JDK_VERSION=3.8.5-openjdk-17
ARG TOMCAT_VERSION=10.1-jdk17
FROM maven:${JDK_VERSION} AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM tomcat:${TOMCAT_VERSION}

ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

WORKDIR /usr/local/tomcat/webapps/

COPY --from=build /app/target/*.war ./ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
