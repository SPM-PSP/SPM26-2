# 基础镜像
FROM alpine:3.19

# 替换为阿里云国内源，彻底解决apk安装超时问题
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories

# 安装所有必要依赖（移除不存在的awk包，busybox已内置）
RUN apk add --no-cache g++ coreutils diffutils dos2unix sed

# 创建安全运行用户
RUN adduser -D judge-user

# 赋予工作目录权限
RUN mkdir -p /tmp/work && chown -R judge-user:judge-user /tmp/work

# 切换普通用户
USER judge-user
WORKDIR /tmp/work