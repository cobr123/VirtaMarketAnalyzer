# Cache maven dependencies as an intermediate docker image
# (This only happens when pom.xml changes or you clear your docker image cache)
FROM maven:3-amazoncorretto-21-alpine AS dependencies
COPY pom.xml /build/
WORKDIR /build/
RUN mvn --batch-mode dependency:go-offline dependency:resolve-plugins

# Build the app using Maven and the cached dependencies
# (This only happens when your source code changes or you clear your docker image cache)
# Should work offline, but https://issues.apache.org/jira/browse/MDEP-82
FROM maven:3-amazoncorretto-21-alpine AS build
COPY --from=dependencies /root/.m2 /root/.m2
COPY pom.xml /build/
COPY src /build/src
WORKDIR /build/
RUN mvn package -Dmaven.test.skip

# Run the application (using the JRE, not the JDK)
# This assumes that your dependencies are packaged in application.jar
FROM amazoncorretto:21-alpine AS runtime
COPY --from=build /build/target/VirtaMarketAnalyzer-jar-with-dependencies.jar /application.jar
# Copy script which should be run
COPY run_data_update.sh /run_data_update.sh
COPY run_trend_update.sh /run_trend_update.sh
COPY src/main/resources/log4j.properties /log4j.properties
RUN chmod +x /run_data_update.sh
RUN chmod +x /run_trend_update.sh
RUN mkdir /logs
# Run the cron every minute
RUN echo '0 17 * * MON-FRI    /run_data_update.sh' > /etc/crontabs/root
RUN echo '0 17 * * SAT    /run_trend_update.sh' >> /etc/crontabs/root
RUN echo '# new line' >> /etc/crontabs/root
CMD ["crond", "-f"]