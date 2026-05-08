FROM eclipse-temurin:21-jdk-jammy
RUN apt-get update --allow-releaseinfo-change && \
apt-get install -y --no-install-recommends dos2unix coreutils procps && \
rm -rf /var/lib/apt/lists/*
RUN mkdir -p /tmp/work /logs
WORKDIR /tmp/work
