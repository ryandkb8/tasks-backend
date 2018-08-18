FROM ryandkb8/scala-sbt-builder:0.0.1 as builder
COPY . /opt/play
WORKDIR /opt/play
RUN sbt clean stage

FROM openjdk:8-jre
COPY --from=builder /opt/play /opt/play
WORKDIR /opt/play/target/universal/stage
ENTRYPOINT ["bin/tasks-backend"]
