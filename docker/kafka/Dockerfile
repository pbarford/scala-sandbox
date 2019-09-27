FROM anapsix/alpine-java

#ENV http_proxy http://hqproxy.corp.ppbplc.com:8080
#ENV HTTP_PROXY http://hqproxy.corp.ppbplc.com:8080
#ENV https_proxy http://hqproxy.corp.ppbplc.com:8080
#ENV HTTPS_PROXY http://hqproxy.corp.ppbplc.com:8080

#ENV no_proxy "*.inhouse.paddypower.com, *.dev.docker, 192.168.65.0/24, artifactory.cmdb.inhouse.paddypower.com"
#ENV NO_PROXY "*.inhouse.paddypower.com, *.dev.docker, 192.168.65.0/24, artifactory.cmdb.inhouse.paddypower.com"

ARG kafka_version=2.1.1
ARG scala_version=2.11

MAINTAINER wurstmeister
RUN apk add --update unzip wget curl docker jq coreutils

ENV KAFKA_VERSION=$kafka_version SCALA_VERSION=$scala_version
ADD download-kafka.sh /tmp/download-kafka.sh
RUN chmod a+x /tmp/download-kafka.sh
RUN sync
RUN /tmp/download-kafka.sh
RUN tar xfz /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -C /opt && rm /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz && ln -s /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} /opt/kafka

VOLUME ["/kafka"]

ENV KAFKA_HOME /opt/kafka
ENV PATH ${PATH}:${KAFKA_HOME}/bin
ADD start-kafka.sh /usr/bin/start-kafka.sh
ADD broker-list.sh /usr/bin/broker-list.sh
ADD create-topics.sh /usr/bin/create-topics.sh
ADD server.properties /opt/kafka/config/server.properties
#ADD kafka-server-start.sh /opt/kafka/bin/kafka-server-start.sh

# The scripts need to have executable permission
RUN chmod a+x /usr/bin/start-kafka.sh && \
    chmod a+x /usr/bin/broker-list.sh && \
    chmod a+x /usr/bin/create-topics.sh
# Use "exec" form so that it runs as PID 1 (useful for graceful shutdown)
CMD ["start-kafka.sh"]