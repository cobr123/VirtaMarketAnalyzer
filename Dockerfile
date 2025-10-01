# Cache maven dependencies as an intermediate docker image
# (This only happens when pom.xml changes or you clear your docker image cache)
FROM maven:3-amazoncorretto-24-alpine AS dependencies
COPY pom.xml /build/
WORKDIR /build/
RUN mvn --batch-mode dependency:go-offline dependency:resolve-plugins

# Build the app using Maven and the cached dependencies
# (This only happens when your source code changes or you clear your docker image cache)
# Should work offline, but https://issues.apache.org/jira/browse/MDEP-82
FROM maven:3-amazoncorretto-24-alpine AS build
COPY --from=dependencies /root/.m2 /root/.m2
COPY pom.xml /build/
COPY src /build/src
WORKDIR /build/
RUN mvn package -Dmaven.test.skip

# Run the application (using the JRE, not the JDK)
# This assumes that your dependencies are packaged in application.jar
FROM amazoncorretto:24-alpine AS runtime
COPY --from=build /build/target/VirtaMarketAnalyzer-jar-with-dependencies.jar /application.jar
# Copy script which should be run
COPY run_data_update.sh /run_data_update.sh
COPY run_trend_update.sh /run_trend_update.sh
COPY src/main/resources/log4j.properties /log4j.properties
RUN chmod +x /run_data_update.sh
RUN chmod +x /run_trend_update.sh
RUN mkdir /logs
# Run the cron at 14:05 UTC, mary
RUN echo '5 14 * * 1    /run_data_update.sh' > /etc/crontabs/root
# Run the cron at 14:05 UTC, anna
RUN echo '5 14 * * 2    /run_data_update.sh' >> /etc/crontabs/root
# Run the cron at 14:05 UTC, olga
RUN echo '5 14 * * 3    /run_data_update.sh' >> /etc/crontabs/root
# Run the cron at 05:05 UTC, vera
RUN echo '5 5 * * 4    /run_data_update.sh' >> /etc/crontabs/root
# Run the cron at 14:05 UTC, lien, nika
RUN echo '5 14 * * 5    /run_data_update.sh' >> /etc/crontabs/root
# Run the cron at 14:05 UTC, once a month
RUN echo '5 14 * * 6    /run_trend_update.sh' >> /etc/crontabs/root
RUN echo '# new line' >> /etc/crontabs/root
CMD ["crond", "-f"]