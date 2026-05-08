@echo off
chcp 65001 >nul
cls

:: 检查语言参数
if "%~1"=="" (
    echo ❌ 错误：请指定判题语言！用法：judge.bat [cpp^|java^|python]
    pause
    exit
)
set "LANG=%~1"
if not "%LANG%"=="cpp" if not "%LANG%"=="java" if not "%LANG%"=="python" (
    echo ❌ 错误：不支持的语言！仅支持 cpp / java / python
    pause
    exit
)

title = %LANG% 评测系统 =

:: ====================== 锁定脚本所在目录 ======================
set "SCRIPT_DIR=%~dp0"
set "DATA_DIR=%SCRIPT_DIR%data"
set "LOGS_DIR=%SCRIPT_DIR%logs"
set "JUDGE_SH=%SCRIPT_DIR%judge_%LANG%.sh"

:: 检查对应语言的判题脚本是否存在
if not exist "%JUDGE_SH%" (
    echo ❌ 错误：未找到 %LANG% 判题脚本 %JUDGE_SH%
    pause
    exit
)

:: ====================== 配置 ======================
set "TIME_LIMIT=5"
set "MEMORY_LIMIT=256m"

:: ====================== 初始化环境 ======================
echo [初始化] 正在检查环境...
echo 脚本所在目录：%SCRIPT_DIR%
echo 测试用例目录：%DATA_DIR%
echo 判题语言：%LANG%
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

:: 3. 检查测试文件（按语言检查代码文件）
echo.
echo [检查] 正在验证测试文件...
set "FILE_ERROR=0"

:: 检查代码文件
if "%LANG%"=="cpp" (
    set "CODE_FILE=%DATA_DIR%\main.cpp"
) else if "%LANG%"=="java" (
    set "CODE_FILE=%DATA_DIR%\Main.java"
) else if "%LANG%"=="python" (
    set "CODE_FILE=%DATA_DIR%\main.py"
)

if not exist "%CODE_FILE%" (
    echo ❌ 缺失：%CODE_FILE%
    set "FILE_ERROR=1"
) else (
    echo ✅ 找到：%CODE_FILE%
)

:: 检查输入和答案文件（通用）
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
    if "%LANG%"=="cpp" (
        echo 请在 %DATA_DIR% 文件夹中放入以下3个文件：
        echo   - main.cpp   （你的C++代码）
        echo   - input.txt  （测试输入）
        echo   - answer.txt （标准答案）
    ) else if "%LANG%"=="java" (
        echo 请在 %DATA_DIR% 文件夹中放入以下3个文件：
        echo   - Main.java  （你的Java代码，类名必须为Main且public）
        echo   - input.txt  （测试输入）
        echo   - answer.txt （标准答案）
    ) else if "%LANG%"=="python" (
        echo 请在 %DATA_DIR% 文件夹中放入以下3个文件：
        echo   - main.py    （你的Python代码）
        echo   - input.txt  （测试输入）
        echo   - answer.txt （标准答案）
    )
    echo ======================================================
    pause
    exit
)

:: ====================== 开始评测 ======================
echo.
echo ✅ 所有检查通过，开始评测...
echo.

:: 复制对应语言的judge.sh到logs目录
copy /Y "%JUDGE_SH%" "%LOGS_DIR%\judge.sh" >nul 2>&1

:: 选择对应语言的Docker镜像
if "%LANG%"=="cpp" (
    set "DOCKER_IMAGE=judge-cpp:latest"
) else if "%LANG%"=="java" (
    set "DOCKER_IMAGE=judge-java:latest"
) else if "%LANG%"=="python" (
    set "DOCKER_IMAGE=judge-python:latest"
)

:: 构建Docker镜像（如果未构建）
docker images | findstr "%DOCKER_IMAGE%" >nul 2>&1
if %errorlevel% neq 0 (
    echo [构建镜像] 正在构建 %DOCKER_IMAGE% 镜像...
    docker build -t %DOCKER_IMAGE% -f "%SCRIPT_DIR%dockerfile/Dockerfile.%LANG%" "%SCRIPT_DIR%"
)

:: 运行Docker
docker run --rm ^
  --network none ^
--cap-drop=NET_RAW --cap-drop=SYS_PTRACE --cap-drop=SETUID --cap-drop=SETGID ^
  --cpus 1 ^
  --memory %MEMORY_LIMIT% ^
  --memory-swap %MEMORY_LIMIT% ^
  -v "%DATA_DIR%":/data:ro ^
  -v "%LOGS_DIR%":/logs:rw ^
  -v "%LOGS_DIR%/judge.sh":/judge.sh:ro ^
  --entrypoint "sh" ^
  %DOCKER_IMAGE% ^
  /judge.sh %TIME_LIMIT% %MEMORY_LIMIT%

:: ====================== 结束 ======================
echo.
echo ======================================================
echo %LANG% 评测结束。详细日志已保存在 logs 文件夹中。
pause
exit