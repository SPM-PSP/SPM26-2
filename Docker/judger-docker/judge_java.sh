#!/bin/sh

# ====================== 配置 ======================
TIME_LIMIT=$1
MEMORY_LIMIT_KB=$2

# ====================== 准备环境 ======================
cd /tmp/work
dos2unix -q /data/Main.java /data/input.txt /data/answer.txt 2>/dev/null

# ====================== 1. 编译 (CE) ======================
echo "[1/4] 正在编译..."
# 编译阶段不限制内存
javac /data/Main.java -d /tmp/work -encoding UTF-8 2>./compile.log

if [ $? -ne 0 ]; then
    echo "[CE] 编译错误"
    cat ./compile.log
    cp ./compile.log /logs/compile.log 2>/dev/null
    exit 2
fi
echo "编译成功"

# ====================== 2. 运行 (TLE / RE / MLE) ======================
echo "[2/4] 正在运行 (限制: ${TIME_LIMIT}s, ${MEMORY_LIMIT_KB}KB)..."

# Java 特有的内存限制：通过 JVM 参数限制堆内存（只在运行时）
# 预留部分内存给 JVM 非堆区域，堆内存设置为总限制的 70%
if [ -n "$MEMORY_LIMIT_KB" ] && [ "$MEMORY_LIMIT_KB" -gt 0 ] 2>/dev/null; then
    HEAP_MEMORY=$((MEMORY_LIMIT_KB * 70 / 100))
    JAVA_XMX="${HEAP_MEMORY}k"
    echo "JVM 堆内存限制: ${JAVA_XMX}"
else
    JAVA_XMX="192m"
    echo "使用默认 JVM 堆内存: 192m"
fi

# 记录运行前的时间戳
START_TIME=$(date +%s%N)

# 运行 Java 程序并限制内存
timeout -s KILL ${TIME_LIMIT} java -Xmx${JAVA_XMX} -Xms${JAVA_XMX} -XX:+ExitOnOutOfMemoryError -cp /tmp/work Main < /data/input.txt > ./output.txt 2>./runtime.log
RUN_CODE=$?

END_TIME=$(date +%s%N)
ELAPSED_MS=$(( (END_TIME - START_TIME) / 1000000 ))

cp ./output.txt /logs/user_output.txt 2>/dev/null
cp ./runtime.log /logs/runtime.log 2>/dev/null

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
    echo "[TLE] 时间超限 (${ELAPSED_MS}ms)"
    exit 137
elif [ $RUN_CODE -eq 137 ]; then
    # 检查是否是内存超限
    if grep -qi "OutOfMemoryError\|java.lang.OutOfMemoryError" ./runtime.log 2>/dev/null; then
        echo "[MLE] 内存超限 (峰值内存: ${PEAK_MEMORY}KB)"
        exit 138
    else
        echo "[TLE] 时间超限 (${ELAPSED_MS}ms)"
        exit 137
    fi
elif [ $RUN_CODE -eq 3 ]; then
    # -XX:+ExitOnOutOfMemoryError 会让 JVM 在 OOM 时退出码为 3
    echo "[MLE] 内存超限 (OutOfMemoryError)"
    exit 138
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
