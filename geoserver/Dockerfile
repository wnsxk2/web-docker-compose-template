ARG GEOSERVER_TOMCAT_VERSION=9.0-jdk11-openjdk-slim
ARG ADMIN_USER
ARG ADMIN_PW
FROM tomcat:${GEOSERVER_TOMCAT_VERSION}

RUN apt-get update && apt-get install -y \
    fontconfig \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*
    
ENV CATALINA_OPTS="-Xms4096M -Xmx4096M -Djava.awt.headless=true"
ENV GEOSERVER_DATA_DIR=/opt/geoserver_data
ENV GEOSERVER_ADMIN_USER=${ADMIN_USER:-admin}
ENV GEOSERVER_ADMIN_PASSWORD=${ADMIN_PW:-geoserver}

RUN mkdir -p /opt/geoserver_data

EXPOSE 8080

CMD ["catalina.sh", "run"]