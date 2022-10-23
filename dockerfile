FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER Theodore Lee (theodore.lee@nextlabs.com)

WORKDIR /opt/rightsprotect
COPY ./build/dist/BulkProtect .
RUN chmod 777 start.sh
RUN mkdir -p /mnt/RPT_temp

EXPOSE 80/tcp
EXPOSE 443/tcp
EXPOSE 8081/tcp

CMD ["/opt/rightsprotect/start.sh"]