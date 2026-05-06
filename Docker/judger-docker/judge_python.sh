#!/bin/sh
set -e

# 接收传入的时间、内存限制参数
TIME_LIMIT=$1
MEMORY_LIMIT=$2

# 1. 初始化：创建容器内原生可写目录，彻底规避挂载权限问题
mkdir -p /tmp/work /tmp/logs
chmod 777 /tmp/logs /tmp/work
cd /tmp/work

# 2. 复制测试文件到可写目录，不触碰只读的测试用例
cp /data/main.py .
cp /data/input.txt .

echo "[2/4] 正在检查Python语法..."
# 禁用字节码缓存，语法检查日志写入容器内可写目录
if ! python -B -m py_compile main.py 2> /tmp/logs/compile.log; then
    # 同步日志到宿主机挂载目录
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo -e "\e[31m❌ [CE] 语法错误\e[0m"
    exit 1
fi
echo "语法检查通过"

echo "[3/4] 正在运行代码 (限制: ${TIME_LIMIT}s, ${MEMORY_LIMIT})..."
# 超时控制+禁用缓存，运行结果写入临时目录
timeout ${TIME_LIMIT}s python -B main.py < input.txt > /tmp/logs/user_output.txt 2> /tmp/logs/runtime.log
RUN_CODE=$?

# 处理运行异常结果
if [ $RUN_CODE -eq 124 ]; then
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo -e "\e[31m❌ TLE (时间超限)\e[0m"
    exit 1
elif [ $RUN_CODE -ne 0 ]; then
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo -e "\e[31m❌ RE (运行时错误)\e[0m"
    exit 1
fi
echo "运行成功"

echo "[4/4] 正在对比答案..."
# 格式化答案与输出，兼容换行/空格差异
cat /data/answer.txt | tr -d '\r' | sed 's/[[:space:]]*$//' > /tmp/logs/answer_formatted.txt
cat /tmp/logs/user_output.txt | tr -d '\r' | sed 's/[[:space:]]*$//' > /tmp/logs/output_formatted.txt

# 对比答案差异
diff -w /tmp/logs/answer_formatted.txt /tmp/logs/output_formatted.txt > /tmp/logs/diff.log
DIFF_CODE=$?

# 把所有日志同步到宿主机logs目录
cp -rf /tmp/logs/* /logs/ 2>/dev/null

# 输出最终判题结果
if [ $DIFF_CODE -eq 0 ]; then
    echo -e "\e[32m🎉 AC (Accepted) 答案正确！\e[0m"
else
    echo -e "\e[31m❌ WA (Wrong Answer) 答案错误\e[0m"
fi