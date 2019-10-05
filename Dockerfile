############################################
#          DO NOT TOUCH THIS FILE          #
############################################
FROM openjdk:8-alpine

RUN apk add gradle

ADD . /lhgames
WORKDIR /lhgames
RUN gradle build

ENTRYPOINT gradle run
