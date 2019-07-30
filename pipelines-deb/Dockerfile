FROM ubuntu:latest
MAINTAINER Max Pumperla <max@skymind.io>
RUN apt update
RUN apt install -y  openjdk-8-jdk
ADD pipelines /usr/share/pipelines
WORKDIR  /usr/share/pipelines
RUN ./mvnw -Djavacpp.platform=linux-x86_64 -Dchip=cpu -Pdeb -Ppython clean install -Dmaven.test.skip=true