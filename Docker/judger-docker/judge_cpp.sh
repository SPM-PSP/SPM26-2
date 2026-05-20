#!/bin/sh

# ====================== 配置 ======================
TIME_LIMIT=$1
MEMORY_LIMIT_KB=$2

# ====================== 准备环境 ======================
cd /tmp/work
dos2unix -q /data/main.cpp /data/input.txt /data/answer.txt 2>/dev/null

# ====================== 1. 编译 (CE) ======================
echo "[1/4] 正在编译..."
# 注意：编译阶段不设置内存限制，让编译器有足够内存
g++ /data/main.cpp -o /tmp/work/main -O2 -std=c++17 2>./compile.log

if [ $? -ne 0 ]; then
    echo "[CE] 编译错误"
    cat ./compile.log
    cp ./compile.log /logs/compile.log 2>/dev/null
    exit 2
fi
echo "编译成功"

# ====================== 2. 运行 (TLE / RE / MLE) ======================
echo "[2/4] 正在运行 (限制: ${TIME_LIMIT}s, ${MEMORY_LIMIT_KB}KB)..."

# 【重要】内存限制只在运行阶段生效，不在编译阶段生效
if [ -n "$MEMORY_LIMIT_KB" ] && [ "$MEMORY_LIMIT_KB" -gt 0 ] 2>/dev/null; then
    ulimit -v "$MEMORY_LIMIT_KB" 2>/dev/null
    echo "内存限制已设置: ${MEMORY_LIMIT_KB}KB"
else
    ulimit -v 262144 2>/dev/null
    echo "使用默认内存限制: 256MB"
fi

# 记录运行前的时间戳
START_TIME=$(date +%s%N)

# 运行程序并捕获退出码
timeout -s KILL ${TIME_LIMIT} /tmp/work/main < /data/input.txt > ./output.txt 2>./runtime.log
RUN_CODE=$?

END_TIME=$(date +%s%N)
ELAPSED_MS=$(( (END_TIME - START_TIME) / 1000000 ))

cp ./output.txt /logs/user_output.txt 2>/dev/null
cp ./runtime.log /logs/runtime.log 2>/dev/null

# 获取实际内存使用峰值
PEAK_MEMORY=0
if [ -f /proc/self/status ]; then
    PEAK_MEMORY=$(grep VmPeak /proc/self/status 2>/dev/null | awk '{print $2}' || echo "0")
fi

# 判断退出原因
if [ $RUN_CODE -eq 124 ]; then
    echo "[TLE] 时间超限 (${ELAPSED_MS}ms, 峰值内存: ${PEAK_MEMORY}KB)"
    exit 137
elif [ $RUN_CODE -eq 137 ]; then
    # SIGKILL，需要区分是超时还是内存超限

    # 方法1：检查 runtime.log 中是否有内存相关错误
    if grep -qi "Cannot allocate memory\|std::bad_alloc\|out of memory" ./runtime.log 2>/dev/null; then
        echo "[MLE] 内存超限 (峰值内存: ${PEAK_MEMORY}KB)"
        exit 138
    fi

    # 方法2：如果运行时间远小于时间限制，但被 kill 了，很可能是内存超限
    TIME_LIMIT_MS=$((TIME_LIMIT * 1000))  # TIME_LIMIT 是秒，转换为毫秒用于比较
    if [ $ELAPSED_MS -lt $((TIME_LIMIT_MS / 2)) ]; then
        echo "[MLE] 内存超限 (峰值内存: ${PEAK_MEMORY}KB, 用时: ${ELAPSED_MS}ms)"
        exit 138
    else
        echo "[TLE] 时间超限 (${ELAPSED_MS}ms, 峰值内存: ${PEAK_MEMORY}KB)"
        exit 137
    fi
elif [ $RUN_CODE -eq 152 ]; then
    echo "[TLE] 时间超限 (${ELAPSED_MS}ms)"
    exit 137
elif [ $RUN_CODE -eq 134 ]; then
    if grep -qi "bad_alloc\|abort" ./runtime.log 2>/dev/null; then
        echo "[MLE] 内存超限 (bad_alloc)"
        exit 138
    fi
    echo "[RE] 运行时错误 (aborted)"
    exit 3
elif [ $RUN_CODE -ne 0 ]; then
    echo "[RE] 运行时错误 (退出码: $RUN_CODE)"
    cat ./runtime.log
    exit 3
fi
echo "运行成功 (${ELAPSED_MS}ms, 峰值内存: ${PEAK_MEMORY}KB)"

# ====================== 3. 格式化输出 ======================
echo "[3/4] 正在格式化答案与输出..."
cat /data/answer.txt | dos2unix | sed '/^[[:space:]]*$/d' | sed 's/[[:space:]]*$//' | tr -s ' ' | sed 's/^ *//' > ./answer_formatted.txt
cat ./output.txt | dos2unix | sed '/^[[:space:]]*$/d' | sed 's/[[:space:]]*$//' | tr -s ' ' | sed 's/^ *//' > ./output_formatted.txt

cp ./answer_formatted.txt /logs/answer_formatted.txt 2>/dev/null
cp ./output_formatted.txt /logs/output_formatted.txt 2>/dev/null

# ====================== 4. 对比 (AC / WA) ======================
echo "[4/4] 正在对比答案..."
diff -w ./output_formatted.txt ./answer_formatted.txt > ./diff.log
DIFF_CODE=$?

cp ./diff.log /logs/diff.log 2>/dev/null

if [ $DIFF_CODE -eq 0 ]; then
    echo ""
    echo "###########################################"
    echo "#  AC (Accepted) 答案正确！  #"
    echo "###########################################"
    exit 0
else
    echo ""
    echo "###########################################"
    echo "#  WA (Wrong Answer) 答案错误  #"
    echo "###########################################"
    echo ""
    echo "提示：请查看 logs/ 目录下的文件进行对比："
    echo "  - logs/user_output.txt:    你的程序原始输出"
    echo "  - logs/answer_formatted.txt:  标准答案(格式化)"
    echo "  - logs/output_formatted.txt:  你的输出(格式化)"
    echo "  - logs/diff.log:           具体差异"
    exit 4
fi
