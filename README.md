# SmartStudy AI 智能伴学系统

一个基于大模型的 AI 智能伴学 Web 应用：上传学习笔记，AI 帮你出题、生成测验和摘要，把学习资料快速变成可练习的内容。

## 功能特性

- 用户注册 / 登录（学生、教师、职场新人等角色）
- 笔记上传：支持 PDF、Word（.doc/.docx）、TXT，自动提取文本
- AI 智能欢迎语
- AI 随机出题（今日挑战）
- 基于笔记自动生成选择题测验，结构化输出并渲染答题卡
- AI 智能摘要（Markdown 格式）
- 笔记列表管理与删除

## 技术栈

- 后端：Java 17、Spring Boot 3、MyBatis、MySQL
- AI：DeepSeek（deepseek-chat）API
- 文档解析：Apache PDFBox、Apache POI
- 前端：原生 HTML / CSS / JavaScript
- 构建与部署：Maven、Procfile

## 项目结构

```
smart-study/
├── src/main/java/com/example/smart_study/
│   ├── controller/AiTestController.java   # 登录、出题、上传、测验、摘要等接口
│   ├── entity/                            # User、Note 实体
│   ├── mapper/                            # MyBatis Mapper
│   └── util/TextExtractor.java            # PDF/Word/TXT 文本提取
├── src/main/resources/
│   ├── application.properties             # 配置（密钥通过环境变量注入）
│   └── static/                            # 前端页面
├── pom.xml
└── Procfile
```

## 快速开始

1. 准备 MySQL 数据库，创建 `smart_study_db`。
2. 配置环境变量：

```bash
export DEEPSEEK_API_KEY=你的DeepSeek密钥
export DB_PASSWORD=你的数据库密码
```

3. 本地启动：

```bash
./mvnw spring-boot:run
```

Windows 使用：

```bash
mvnw.cmd spring-boot:run
```

4. 访问 `http://localhost:8081`。

## 部署

- 云端服务器设置 `DEEPSEEK_API_KEY`、`DB_PASSWORD` 等环境变量。
- 通过 Procfile 启动：

```
web: ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

- 上传目录通过 `app.upload-dir` 配置，生产环境建议放在数据盘。

