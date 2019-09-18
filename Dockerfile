FROM ubuntu:latest
MAINTAINER Feras Wilson, http://www.dopplertask.com

EXPOSE 8090

ADD . /root

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  openjdk-11-jdk && \
    apt-get clean

RUN cd ~ && ./gradlew clean

RUN chmod +x /root/start.sh

WORKDIR /root
ENTRYPOINT ["./start.sh"]