#!/bin/sh

# ====================== 配置 ======================
TIME_LIMIT=$1
MEMORY_LIMIT=$2

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# ====================== 准备环境 ======================
cd /tmp/work
dos2unix -q /data/Main.java /data/input.txt /data/answer.txt 2>/dev/null

# ====================== 1. 编译 (CE) ======================
echo "[1/4] 正在编译..."
javac /data/Main.java -d /tmp/work -encoding UTF-8 2>./compile.log

if [ $? -ne 0 ]; then
    echo -e "${RED}[CE] 编译错误${NC}"
    cat ./compile.log
    cp ./compile.log /logs/compile.log 2>/dev/null
    exit 2
fi
echo -e "${GREEN}编译成功${NC}"

# ====================== 2. 运行 (TLE / RE / MLE) ======================
echo "[2/4] 正在运行 (限制: ${TIME_LIMIT}s, ${MEMORY_LIMIT})..."
timeout -s KILL ${TIME_LIMIT} java -cp /tmp/work Main < /data/input.txt > ./output.txt 2>./runtime.log
RUN_CODE=$?

cp ./output.txt /logs/user_output.txt 2>/dev/null
cp ./runtime.log /logs/runtime.log 2>/dev/null

if [ $RUN_CODE -eq 137 ]; then
    echo -e "${RED}[TLE] 时间超限 / 内存超限${NC}"
    exit 137
fi

if [ $RUN_CODE -ne 0 ]; then
    echo -e "${RED}[RE] 运行时错误 (崩溃)${NC}"
    cat ./runtime.log
    exit 3
fi
echo -e "${GREEN}运行成功${NC}"

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
    echo -e "#  ${GREEN}🎉 AC (Accepted) 答案正确！${NC}  #"
    echo "###########################################"
    exit 0
else
    echo ""
    echo "###########################################"
    echo -e "#  ${RED}❌ WA (Wrong Answer) 答案错误${NC}  #"
    echo "###########################################"
    echo ""
    echo "提示：请查看 logs/ 目录下的文件进行对比："
    echo "  - logs/user_output.txt:    你的程序原始输出"
    echo "  - logs/answer_formatted.txt:  标准答案(格式化)"
    echo "  - logs/output_formatted.txt:  你的输出(格式化)"
    echo "  - logs/diff.log:           具体差异"
    exit 4
fi