#!/bin/sh

# 注意：不使用 set -e，因为我们需要手动处理 timeout 的非零退出码

TIME_LIMIT=$1
MEMORY_LIMIT_KB=$2

mkdir -p /tmp/work /tmp/logs /logs
chmod 777 /tmp/logs /tmp/work /logs
cd /tmp/work

cp /data/main.py .
cp /data/input.txt .

echo "[1/4] 正在检查Python语法..."
if ! python -B -m py_compile main.py 2> /tmp/logs/compile.log; then
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo "[CE] 语法错误"
    exit 2
fi
echo "语法检查通过"

echo "[2/4] 正在运行代码 (限制: ${TIME_LIMIT}s, ${MEMORY_LIMIT_KB}KB)..."

# 创建内存限制包装脚本
cat > /tmp/work/run_with_memory_limit.py << 'PYEOF'
import resource
import sys
import os

# 获取内存限制（从环境变量，单位KB）
mem_limit_kb = int(os.environ.get('MEMORY_LIMIT_KB', 262144))  # 默认256MB
mem_limit_bytes = mem_limit_kb * 1024

# 设置内存限制（软限制和硬限制）
try:
    resource.setrlimit(resource.RLIMIT_AS, (mem_limit_bytes, mem_limit_bytes))
except (ValueError, resource.error) as e:
    print(f"Warning: Could not set memory limit: {e}", file=sys.stderr)

# 执行目标脚本
exec(open('main.py').read())
PYEOF

# 设置环境变量传递内存限制
export MEMORY_LIMIT_KB=${MEMORY_LIMIT_KB}

# 记录运行前的时间戳
START_TIME=$(date +%s%N)

# 运行程序（注意：不使用 set -e，所以 timeout 的非零退出码不会导致脚本立即退出）
timeout ${TIME_LIMIT}s python -B /tmp/work/run_with_memory_limit.py < input.txt > /tmp/logs/user_output.txt 2> /tmp/logs/runtime.log
RUN_CODE=$?

END_TIME=$(date +%s%N)
ELAPSED_MS=$(( (END_TIME - START_TIME) / 1000000 ))

# 获取内存使用峰值
PEAK_MEMORY=0
if [ -f /proc/self/status ]; then
    PEAK_MEMORY=$(grep VmPeak /proc/self/status 2>/dev/null | awk '{print $2}' || echo "0")
fi

# 将运行时间和内存写入专门的日志文件（供后端解析）
echo "${ELAPSED_MS}" > /logs/runtime_ms.txt
echo "${PEAK_MEMORY}" > /logs/memory_kb.txt

# 判断退出原因
if [ $RUN_CODE -eq 124 ]; then
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo "[TLE] 时间超限 (${ELAPSED_MS}ms)"
    exit 137
elif [ $RUN_CODE -eq 137 ]; then
    # 检查是否是内存超限
    if grep -qi "MemoryError\|Cannot allocate memory" /tmp/logs/runtime.log 2>/dev/null; then
        cp -rf /tmp/logs/* /logs/ 2>/dev/null
        echo "[MLE] 内存超限 (峰值内存: ${PEAK_MEMORY}KB)"
        exit 138
    else
        cp -rf /tmp/logs/* /logs/ 2>/dev/null
        echo "[TLE] 时间超限 (${ELAPSED_MS}ms)"
        exit 137
    fi
elif [ $RUN_CODE -eq 1 ]; then
    # Python MemoryError 通常退出码为 1
    if grep -qi "MemoryError" /tmp/logs/runtime.log 2>/dev/null; then
        cp -rf /tmp/logs/* /logs/ 2>/dev/null
        echo "[MLE] 内存超限"
        exit 138
    fi
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo "[RE] 运行时错误"
    exit 3
elif [ $RUN_CODE -ne 0 ]; then
    cp -rf /tmp/logs/* /logs/ 2>/dev/null
    echo "[RE] 运行时错误 (退出码: $RUN_CODE)"
    cat /tmp/logs/runtime.log >> /logs/runtime.log 2>/dev/null
    exit 3
fi
echo "运行成功 (${ELAPSED_MS}ms, 峰值内存: ${PEAK_MEMORY}KB)"

echo "[3/4] 正在对比答案..."
cat /data/answer.txt | tr -d '\r' | sed 's/[[:space:]]*$//' > /tmp/logs/answer_formatted.txt
cat /tmp/logs/user_output.txt | tr -d '\r' | sed 's/[[:space:]]*$//' > /tmp/logs/output_formatted.txt

diff -w /tmp/logs/answer_formatted.txt /tmp/logs/output_formatted.txt > /tmp/logs/diff.log || true

cp -rf /tmp/logs/* /logs/ 2>/dev/null

if [ -s /tmp/logs/diff.log ]; then
    echo "[4/4] WA Wrong Answer 答案错误"
    exit 4
else
    echo "[4/4] AC Accepted 答案正确！"
    exit 0
fi
