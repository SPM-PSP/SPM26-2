#!/bin/sh
set -e

TIME_LIMIT=$1
MEMORY_LIMIT=$2

mkdir -p /tmp/work /tmp/logs
chmod 777 /tmp/logs /tmp/work
cd /tmp/work

cp /data/main.py .
cp /data/input.txt .

echo "[2/4] 正在检查Python语法..."
if ! python -B -m py_compile main.py 2> /tmp/logs/compile.log; then
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo -e "\e[31m❌ [CE] 语法错误\e[0m"
    exit 2
fi
echo "语法检查通过"

echo "[3/4] 正在运行代码 (限制: ${TIME_LIMIT}s, ${MEMORY_LIMIT})..."
timeout ${TIME_LIMIT}s python -B main.py < input.txt > /tmp/logs/user_output.txt 2> /tmp/logs/runtime.log
RUN_CODE=$?

if [ $RUN_CODE -eq 124 ] || [ $RUN_CODE -eq 137 ]; then
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo -e "\e[31m❌ TLE (时间超限)\e[0m"
    exit 137
elif [ $RUN_CODE -ne 0 ]; then
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo -e "\e[31m❌ RE (运行时错误)\e[0m"
    exit 3
fi
echo "运行成功"

echo "[4/4] 正在对比答案..."
cat /data/answer.txt | tr -d '\r' | sed 's/[[:space:]]*$//' > /tmp/logs/answer_formatted.txt
cat /tmp/logs/user_output.txt | tr -d '\r' | sed 's/[[:space:]]*$//' > /tmp/logs/output_formatted.txt

diff -w /tmp/logs/answer_formatted.txt /tmp/logs/output_formatted.txt > /tmp/logs/diff.log || true

cp -rf /tmp/logs/* /logs/ 2>/dev/null

if [ -s /tmp/logs/diff.log ]; then
    echo -e "\e[31m❌ WA (Wrong Answer) 答案错误\e[0m"
    exit 4
else
    echo -e "\e[32m🎉 AC (Accepted) 答案正确！\e[0m"
    exit 0
fi
