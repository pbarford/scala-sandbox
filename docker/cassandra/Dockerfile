#
# Cassandra 3.0.13
#
FROM centos:centos6

#ENV http_proxy http://hqproxy.corp.ppbplc.com:8080
#ENV HTTP_PROXY http://hqproxy.corp.ppbplc.com:8080
#ENV https_proxy http://hqproxy.corp.ppbplc.com:8080
#ENV HTTPS_PROXY http://hqproxy.corp.ppbplc.com:8080

RUN yum -y install tar wget which
RUN curl -o /opt/jdk-8u141-linux-x64.rpm "http://artifactory.cmdb.inhouse.paddypower.com:8081/artifactory/yum-local/6Server/os/x86_64/Packages/jdk-8u141-linux-x64.rpm"
RUN rpm -Uvh /opt/jdk-8u141-linux-x64.rpm
RUN rm -rf /opt/jdk-8u141-linux-x64.rpm
RUN yum -y update
RUN yum install -y zlib-dev openssl-devel sqlite-devel bzip2-devel xz-libs gcc g++ build-essential make xz
RUN yum -y install initscripts
RUN curl -o /root/Python-2.7.9.tar.xz --insecure "https://www.python.org/ftp/python/2.7.9/Python-2.7.9.tar.xz"
RUN tar -xf /root/Python-2.7.9.tar.xz -C /root
RUN cd /root/Python-2.7.9 && ./configure --prefix=/usr && make && make altinstall

RUN mkdir /opt/cassandra
RUN mkdir /opt/cassandra/logs
RUN cd /tmp/
RUN wget -O - http://archive.apache.org/dist/cassandra/3.0.13/apache-cassandra-3.0.13-bin.tar.gz | tar xzf - --strip-components=1 -C "/opt/cassandra"

RUN mkdir /src
COPY cassandra.yaml /opt/cassandra/conf/
COPY cassandra-singlenode.sh /usr/bin/cassandra-singlenode

#RUN mkdir -p /etc/service/cassandra
#RUN cp /src/start-cassandra /etc/service/cassandra/run
#RUN sed -i -e "s/^python/python2.7/" /usr/bin/cqlsh && \
#    rm -rf /root/Python-2.7.9.tar.xz
#COPY cassandra-singlenode.sh /usr/bin/cassandra-singlenode

COPY data/* /data/
RUN chmod a+x /usr/bin/cassandra-singlenode

EXPOSE 7199 7000 7001 9160 9042 22 8012 61621

