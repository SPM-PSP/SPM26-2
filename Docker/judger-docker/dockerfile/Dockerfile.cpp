FROM gcc:12.2.0
# 修复：timeout属于coreutils包，同时优化旧版Debian源的兼容性
RUN apt-get update --allow-releaseinfo-change && \
    apt-get install -y --no-install-recommends dos2unix coreutils procps && \
    rm -rf /var/lib/apt/lists/*
RUN mkdir -p /tmp/work /logs
WORKDIR /tmp/work
