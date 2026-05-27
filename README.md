# 智能在线编程练习平台

团队成员
1.李俊熠
2.冯朝航
3.胡阳
4.黄嘉鑫
5.孙赫
6.唐代鑫
7.谢浩敏

# 项目介绍
文档版本：V1.1


---

## 一、项目背景
在计算机专业教学、求职笔试面试、算法竞赛训练等核心场景中，算法编程能力是衡量开发者技术水平的关键指标。随着编程学习需求的持续增长，传统算法题库系统逐渐暴露出诸多痛点：功能单一，仅支持基础的题目展示与结果判定；评测机制僵化，仅验证代码正确性，无法对代码质量进行深度分析；题目资源固定固化，无法满足个性化练习需求；缺乏专业的指导能力，学习者难以自主优化代码、提升编程规范。

为解决上述行业痛点，我们研发了**智能算法题库与在线评测系统**。项目以算法学习为核心，融合**自研C++在线判题（OJ）引擎**与人工智能大模型技术，构建了集题目练习、代码提交、自动测评、AI智能分析、题目自动生成为一体的全流程智能化平台，为用户提供一站式、高效率的算法学习解决方案。

## 二、项目定位
本项目是一款基于 **SpringBoot** 开发的Web端一站式算法学习平台，整合了**标准化题库管理、在线代码编辑、自动化代码评测、AI智能辅助、题目动态生成**五大核心能力。

平台面向在校学生、编程爱好者、求职备考人员、算法竞赛选手等全层次用户，致力于通过技术创新降低算法学习门槛，以「传统OJ判题+AI智能赋能」的双引擎模式，让用户不仅能高效刷题通关，更能规范代码风格、优化算法效率、全面提升编程核心素养。

## 三、核心功能
### 1. 标准化算法题库与在线练习
- 构建结构化题库体系，按照**知识点（数据结构、动态规划、贪心、搜索等）、难度等级（入门/简单/中等/困难）** 完成题目分级分类管理；
- 内置在线代码编辑器，支持C/C++编程语言，用户可直接在线编写；
- 提供题目收藏、历史练习、错题归集等功能，支撑用户针对性复习与查漏补缺。

### 2. 自动化代码评测引擎
- 集成自研C++评测程序，实现**代码自动编译、沙箱安全运行、测试用例逐组比对**的全自动化判题；
- 精准返回专业评测结果：答案正确(AC)、答案错误(WA)、编译错误(CE)、运行错误(RE)；
- 支持多组测试用例批量校验，保证评测结果的准确性、公平性与稳定性。

### 3. AI多维度代码智能评价
接入AI大模型接口，对用户提交的代码进行全方位深度分析：
- 算法复杂度分析：自动解析时间复杂度、空间复杂度，并提供针对性优化方案；
- 代码风格检测：依据行业开发规范，校验命名规范、缩进格式、注释完整性；
- 可读性评估：分析代码逻辑清晰度，识别冗余代码，提供重构建议；
- 缺陷智能诊断：定位潜在Bug、边界条件漏洞，给出修复思路。

### 4. AI智能生成算法题目与样例
- 支持自定义生成规则：用户可指定**知识点、难度、题目类型**，AI自动生成原创算法题；
- 自动配套生成完整内容：题目描述、输入输出格式、标准测试用例（in/out文件）、参考题解；
- 生成题目可直接入库并支持在线提交测评，实现题库资源的无限动态拓展。

### 5. 学习数据可视化管理
- 永久留存用户提交记录、评测结果、AI代码评价报告；
- 自动统计练习数据：刷题量、正确率、薄弱知识点、耗时分析等；
- 支持历史记录回溯与报告导出，方便用户复盘总结、持续提升。

## 四、技术亮点
1. **双引擎核心架构**
   传统OJ自动化评测引擎保障结果准确性，AI大模型引擎提供智能分析与生成能力，兼顾稳定性与智能化。
2. **全流程智能化闭环**
   实现「AI出题→在线做题→自动判题→AI评题→优化改进」全流程自动化，无需人工干预。
3. **高兼容与易扩展**
   支持多编程语言评测，AI接口可灵活切换，题库与功能模块支持无缝扩容。
4. **安全沙箱运行环境**
   用户提交的代码在隔离环境中运行，杜绝恶意代码风险，保障系统安全稳定。
5. **轻量化易用设计**
   界面简洁直观，核心功能聚焦算法学习，降低用户使用成本，提升学习效率。

## 五、应用场景
- **高校教学场景**：计算机专业算法课程课后练习、作业自动批改、实践教学辅助；
- **求职备考场景**：IT行业求职者笔试刷题、代码规范化训练、面试能力提升；
- **竞赛训练场景**：算法竞赛选手专项练习、代码效率优化、高强度刷题训练；
- **自主学习场景**：编程零基础/进阶爱好者自学算法，系统性提升编程能力。

## 六、项目价值
### 对用户
打破传统刷题的局限性，通过AI获得专业、个性化的代码指导，快速提升算法思维与代码质量。
### 对教学
简化教师的代码评测、作业批改工作，实现智能化、轻量化的算法教学辅助。
### 对行业
创新「AI+传统OJ」的学习模式，为编程教育智能化、个性化发展提供了可落地、可复制的解决方案。

---

# 安装与使用教程

## 系统要求

### 后端环境
- **JDK:** 17 或更高版本
- **Maven:** 3.6 或更高版本
- **MySQL:** 5.7 或更高版本（推荐 8.0+）
- **Docker:** 20.10 或更高版本（用于代码判题沙箱）
- **操作系统:** Windows/Linux/macOS

### 前端环境
- **Node.js:** 16 或更高版本
- **npm:** 8 或更高版本（随 Node.js 一起安装）

## 一、数据库配置

### 1. 创建数据库

打开 MySQL 命令行或图形化工具（如 Navicat、MySQL Workbench），执行以下 SQL 语句：

```sql
CREATE DATABASE IF NOT EXISTS `oj_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `oj_platform`;
```

### 2. 导入数据库表结构

找到项目中的数据库文档 `Docs/04-数据库文档.md`，复制其中的 SQL 建表语句并执行。或者直接在 MySQL 命令行中执行：

```bash
mysql -u root -p oj_platform < Docs/04-数据库文档.md
```

> **注意：** 如果文件中包含 Markdown 格式标记，请先提取纯 SQL 部分再执行。

### 3. 修改数据库连接配置

编辑后端配置文件 `Code/src/main/resources/application.yml`，修改以下内容：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oj_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root          # 修改为你的 MySQL 用户名
    password: 123456        # 修改为你的 MySQL 密码
```

## 二、后端启动

### 1. 配置环境变量

项目使用了多个外部服务，需要配置以下环境变量：

#### Windows (PowerShell)
```powershell
# 阿里云 OSS 配置（可选，用于头像上传等功能）
$env:ALIYUN_OSS_ACCESS_KEY_ID="你的AccessKeyID"
$env:ALIYUN_OSS_ACCESS_KEY_SECRET="你的AccessKeySecret"

# 火山引擎 Ark API Key（可选，用于 AI 功能）
$env:ARK_API_KEY="你的API密钥"
```

#### Linux/macOS
```bash
export ALIYUN_OSS_ACCESS_KEY_ID="你的AccessKeyID"
export ALIYUN_OSS_ACCESS_KEY_SECRET="你的AccessKeySecret"
export ARK_API_KEY="你的API密钥"
```

> **提示：** 如果不配置这些变量，相关功能将不可用，但不影响基础判题功能。

### 2. 修改判题脚本路径

编辑 `Code/src/main/resources/application.yml`，找到 `judge` 配置项：

```yaml
judge:
  script-path: C:/Users/27584/Desktop/SPM26-2/SPM26-2/Docker/judger-docker/  # 修改为你的实际路径
  temp-task-root: C:/tmp/judge-tasks/  # 修改为临时目录路径
```

> **重要：** `script-path` 必须指向 `Docker/judger-docker/` 目录的绝对路径。

### 3. 构建 Docker 判题镜像

进入 Docker 判题脚本目录：

```bash
cd Docker/judger-docker/dockerfile
```

构建三种语言的判题镜像：

```bash
# 构建 C++ 判题镜像
docker build -t gcc-judge:latest -f Dockerfile.cpp .

# 构建 Java 判题镜像
docker build -t java-judge:latest -f Dockerfile.java .

# 构建 Python 判题镜像
docker build -t python-judge:latest -f Dockerfile.py .
```

验证镜像是否构建成功：

```bash
docker images | grep judge
```

应该看到三个镜像：`gcc-judge:latest`、`java-judge:latest`、`python-judge:latest`

### 4. 启动后端服务

进入后端项目目录：

```bash
cd Code
```

使用 Maven 启动：

```bash
mvn spring-boot:run
```

或者先打包再运行：

```bash
mvn clean package -DskipTests
java -jar target/program_platform_backend-0.0.1-SNAPSHOT.jar
```

启动成功后，后端服务将运行在 `http://localhost:8080`

### 5. 验证后端启动

访问 Knife4j 接口文档页面：

```
http://localhost:8080/doc.html
```

如果能看到接口文档页面，说明后端启动成功。

## 三、前端启动

### 1. 安装依赖

进入前端项目目录：

```bash
cd frontend
```

安装 npm 依赖：

```bash
npm install
```

### 2. 配置后端代理

前端开发服务器已配置代理，将 `/api` 请求转发到后端。配置文件位于 `frontend/vite.config.ts`：

```typescript
server: {
  port: 5173,
  host: true,
  allowedHosts: true,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 确保后端地址正确
      changeOrigin: true,
    },
  },
},
```

如果后端运行在其他端口，请修改 `target` 字段。

### 3. 启动前端开发服务器

```bash
npm run dev
```

启动成功后，前端将运行在 `http://localhost:5173`

### 4. 访问应用

打开浏览器，访问：

```
http://localhost:5173
http://服务器主机外网ip地址:5173
```

即可看到智能在线编程练习平台的主页面。

## 四、生产部署

### 后端部署

#### 1. 打包应用

```bash
cd Code
mvn clean package -DskipTests
```

生成的 JAR 文件位于 `Code/target/program_platform_backend-0.0.1-SNAPSHOT.jar`

#### 2. 配置生产环境变量

创建 `.env` 文件或直接在系统中设置环境变量：

```bash
export ALIYUN_OSS_ACCESS_KEY_ID="你的AccessKeyID"
export ALIYUN_OSS_ACCESS_KEY_SECRET="你的AccessKeySecret"
export ARK_API_KEY="你的API密钥"
```

#### 3. 运行应用

```bash
java -jar target/program_platform_backend-0.0.1-SNAPSHOT.jar \
  --server.port=8080 \
  --spring.datasource.url=jdbc:mysql://your-mysql-host:3306/oj_platform \
  --spring.datasource.username=your_username \
  --spring.datasource.password=your_password
```


## 五、常见问题

### 1. Docker 镜像构建失败

**问题：** 构建 Docker 镜像时出现网络错误或依赖下载失败。

**解决方案：**
- 配置 Docker 国内镜像源
- 检查网络连接
- 确保 Dockerfile 中的基础镜像版本可用

### 2. 判题功能无法使用

**问题：** 提交代码后无法得到判题结果。

**解决方案：**
- 检查 Docker 服务是否正常运行：`docker ps`
- 确认判题镜像已正确构建：`docker images | grep judge`
- 检查 `application.yml` 中的 `script-path` 是否正确
- 查看后端日志，确认判题脚本执行是否正常

### 3. 前端无法连接后端

**问题：** 前端页面显示网络错误或 API 调用失败。

**解决方案：**
- 确认后端服务已启动：访问 `http://localhost:8080/doc.html`
- 检查前端代理配置是否正确（`vite.config.ts`）
- 检查浏览器控制台是否有 CORS 错误
- 确认防火墙未阻止端口 8080 和 5173

### 4. 数据库连接失败

**问题：** 后端启动时报数据库连接错误。

**解决方案：**
- 确认 MySQL 服务已启动
- 检查 `application.yml` 中的数据库地址、用户名、密码是否正确
- 确认数据库 `oj_platform` 已创建
- 检查 MySQL 是否允许远程连接（如果使用非 localhost）

### 5. AI 功能无法使用

**问题：** AI 代码评价或题目生成功能失效。

**解决方案：**
- 确认已配置 `ARK_API_KEY` 环境变量
- 检查网络连接是否能访问火山引擎 API
- 查看后端日志，确认 API 调用是否成功
- 验证 API Key 是否有效且有足够配额

### 6. 图片上传失败

**问题：** 头像上传或其他图片上传功能失败。

**解决方案：**
- 确认已配置阿里云 OSS 的 `ACCESS_KEY_ID` 和 `ACCESS_KEY_SECRET`
- 检查 OSS Bucket 权限是否设置为公共读
- 验证 Endpoint 和 BucketName 配置是否正确
- 查看后端日志，确认上传请求是否成功

## 六、项目结构

```
SPM26-2/
├── Code/                          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/             # Java 源代码
│   │   │   └── resources/        # 配置文件
│   │   │       ├── application.yml
│   │   │       └── mapper/       # MyBatis Mapper XML
│   │   └── test/                 # 测试代码
│   ├── target/                   # 编译输出目录
│   └── pom.xml                   # Maven 配置文件
│
├── frontend/                     # 前端项目
│   ├── src/
│   │   ├── api/                  # API 接口封装
│   │   ├── assets/               # 静态资源
│   │   ├── components/           # Vue 组件
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # Pinia 状态管理
│   │   ├── views/                # 页面视图
│   │   └── main.ts               # 入口文件
│   ├── dist/                     # 构建输出目录
│   ├── package.json              # npm 依赖配置
│   └── vite.config.ts            # Vite 配置
│
├── Docker/                       # Docker 判题相关
│   └── judger-docker/
│       ├── dockerfile/           # Dockerfile 文件
│       ├── judge_cpp.sh          # C++ 判题脚本
│       ├── judge_java.sh         # Java 判题脚本
│       └── judge_python.sh       # Python 判题脚本
│
├── Docs/                         # 项目文档
│   ├── 01-立项计划书.pdf
│   ├── 02-需求分析文档.pdf
│   ├── 03-接口文档.md
│   ├── 04-数据库文档.md
│   └── ...
│
└── README.md                     # 项目说明文档
```

## 七、技术栈

### 后端技术栈
- **框架：** Spring Boot 2.7.18
- **持久层：** MyBatis + PageHelper
- **数据库：** MySQL 8.0
- **安全认证：** JWT (JSON Web Token)
- **接口文档：** Knife4j + Swagger2
- **对象存储：** 阿里云 OSS
- **AI 集成：** 火山引擎 Ark Runtime
- **其他：** Lombok, Spring Security Crypto, Commons IO

### 前端技术栈
- **框架：** Vue 3.5.12
- **构建工具：** Vite 5.4.11
- **语言：** TypeScript 5.6
- **状态管理：** Pinia 2.2.6
- **路由：** Vue Router 4.4.5
- **HTTP 客户端：** Axios 1.7.7

### 判题系统
- **容器化：** Docker
- **支持语言：** C++ (GCC), Java (JDK), Python 3
- **沙箱隔离：** Docker 容器资源限制

