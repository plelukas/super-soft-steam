FROM openjdk:8-jdk

MAINTAINER Synerise

ADD target/scala-2.12/master.jar /srv/application/master.jar

EXPOSE 8080
EXPOSE 2551
EXPOSE 8558

ENV JMX_DEBUG_JAVA_OPTS '-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7091 -Dcom.sun.management.jmxremote.rmi.port=7091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=127.0.0.1'
ENV JAVA_OPTS '-Xmx600m'

CMD java \
 $JMX_DEBUG_JAVA_OPTS $JAVA_OPTS \
 -Dfile.encoding=UTF-8 \
 -jar /srv/application/master.jar