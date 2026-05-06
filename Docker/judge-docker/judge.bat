@echo off
chcp 65001 >nul
cls
title = C++ 评测系统 =

:: ====================== 锁定脚本所在目录（核心修复） ======================
set "SCRIPT_DIR=%~dp0"
set "DATA_DIR=%SCRIPT_DIR%data"
set "LOGS_DIR=%SCRIPT_DIR%logs"
set "JUDGE_SH=%SCRIPT_DIR%judge.sh"

:: ====================== 配置 ======================
set "TIME_LIMIT=5"
set "MEMORY_LIMIT=256m"

:: ====================== 初始化环境 ======================
echo [初始化] 正在检查环境...
echo 脚本所在目录：%SCRIPT_DIR%
echo 测试用例目录：%DATA_DIR%
echo.

:: 1. 检查 Docker
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误：Docker 未启动！请先打开Docker Desktop，等待运行中再执行
    pause
    exit
)
echo ✅ Docker 服务正常

:: 2. 创建文件夹
if not exist "%DATA_DIR%" mkdir "%DATA_DIR%"
if not exist "%LOGS_DIR%" mkdir "%LOGS_DIR%"

:: 3. 检查测试文件（输出完整路径，一眼看到问题）
echo.
echo [检查] 正在验证测试文件...
set "FILE_ERROR=0"

if not exist "%DATA_DIR%\main.cpp" (
    echo ❌ 缺失：%DATA_DIR%\main.cpp
    set "FILE_ERROR=1"
) else (
    echo ✅ 找到：main.cpp
)

if not exist "%DATA_DIR%\input.txt" (
    echo ❌ 缺失：%DATA_DIR%\input.txt
    set "FILE_ERROR=1"
) else (
    echo ✅ 找到：input.txt
)

if not exist "%DATA_DIR%\answer.txt" (
    echo ❌ 缺失：%DATA_DIR%\answer.txt
    set "FILE_ERROR=1"
) else (
    echo ✅ 找到：answer.txt
)

:: 如果有文件缺失，提示并退出
if %FILE_ERROR% equ 1 (
    echo.
    echo ======================================================
    echo 请在 %DATA_DIR% 文件夹中放入以下3个文件：
    echo   - main.cpp   （你的C++代码）
    echo   - input.txt  （测试输入）
    echo   - answer.txt （标准答案）
    echo ======================================================
    pause
    exit
)

:: ====================== 开始评测 ======================
echo.
echo ✅ 所有检查通过，开始评测...
echo.

:: 复制judge.sh到logs目录，解决挂载权限问题
copy /Y "%JUDGE_SH%" "%LOGS_DIR%\judge.sh" >nul 2>&1

:: 运行Docker（路径全部用绝对路径，彻底解决挂载问题）
docker run --rm ^
  --network none ^
  --cap-drop=ALL ^
  --cpus 1 ^
  --memory %MEMORY_LIMIT% ^
  --memory-swap %MEMORY_LIMIT% ^
  -v "%DATA_DIR%":/data:ro ^
  -v "%LOGS_DIR%":/logs:rw ^
  -v "%LOGS_DIR%/judge.sh":/judge.sh:ro ^
  --entrypoint "sh" ^
  judge-cpp:latest ^
  /judge.sh %TIME_LIMIT% %MEMORY_LIMIT%

:: ====================== 结束 ======================
echo.
echo ======================================================
echo 评测结束。详细日志已保存在 logs 文件夹中。
pause
exit