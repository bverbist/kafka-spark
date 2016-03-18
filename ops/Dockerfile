FROM java:8-jre

RUN  wget -q -O - http://www.eu.apache.org/dist/kafka/0.9.0.1/kafka_2.10-0.9.0.1.tgz | tar -xzf - -C /opt

ENV PATH /opt/kafka_2.10-0.9.0.1/bin:$PATH

COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

COPY ./config0 /opt/kafka_2.10-0.9.0.1/config
VOLUME ["/opt/kafka_2.10-0.9.0.1/config"]

EXPOSE 9092

CMD  ["kafka-server-start.sh", "/opt/kafka_2.10-0.9.0.1/config/server.properties"]