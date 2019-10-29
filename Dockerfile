FROM hirokimatsumoto/alpine-openjdk-11:latest as jlink-package
# First: generate java runtime module by jlink.

RUN jlink \
     --module-path /opt/java/jmods \
     --compress=2 \
     --add-modules jdk.jfr,jdk.management.agent,java.base,java.logging,java.xml,jdk.unsupported,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument \
     --no-header-files \
     --no-man-pages \
     --output /opt/jdk-11-mini-runtime


ADD . /root

RUN cd ~/backend && ./gradlew build -x test && chmod +x /root/start.sh && chmod +x /root/setup.sh

# Second image
FROM alpine:3.9

MAINTAINER Feras Wilson, http://www.dopplertask.com

ENV JAVA_HOME=/opt/jdk-11-mini-runtime
ENV PATH="$PATH:$JAVA_HOME/bin"

RUN cd ~ && ./setup.sh

COPY --from=jlink-package /opt/jdk-11-mini-runtime /opt/jdk-11-mini-runtime
COPY --from=jlink-package /root/backend/build/libs/doppler-0.2.0.jar /opt/spring-boot/
COPY --from=jlink-package /root/start.sh /opt/spring-boot/

EXPOSE 8090
EXPOSE 61617
WORKDIR  /opt/spring-boot/
CMD ["/opt/spring-boot/start.sh"]