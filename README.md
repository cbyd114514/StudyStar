<br />
<p align="center">
  <h2 align="center" style="font-weight: 600">学习星 (StudyStar)</h2>

  <p align="center">
    超星学习通全功能辅助引擎与现代 Google Material Design 3 极清高刷 Android 原生客户端
  </p>

  <p align="center">
    <a href="https://github.com/cbyd114514/StudyStar/releases/latest"><img src="https://img.shields.io/badge/Download%20APK-v1.5.8-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="下载最新版 APK" /></a>
  </p>
</p>

> [!TIP]
> **最新版本发布页（Android APK 下载）**：**[https://github.com/cbyd114514/StudyStar/releases](https://github.com/cbyd114514/StudyStar/releases)**

基于开源项目 [SuperStar_R](https://github.com/menghuanshiguang/SuperStar_R) 核心，融合 **Python 全自动任务引擎**（支持 CLI / Docker 运行）与严格遵循 Google **Material Design 3 (Material You)** 规范、现代 Android 原生架构及硬件安全标准的 **Jetpack Compose Android 客户端**。

---

## 核心特性一览

### 1. Android 原生客户端 (`android-app/`)
- **Google Material Design 3 (Material You) 规范全景重塑**
  - **动态取色与品牌色彩系统**：原生对接 Android 12+ (API 31+) Dynamic Color，内置 Oceanic Blue 品牌色调系统，完整覆盖 Primary、Secondary、Tertiary、Surface Container（Lowest/Low/Normal/High/Highest）、Outline 及 Error 全套语义角色；
  - **15 阶 Typography 与统一 Shapes**：完整遵循 M3 字体阶梯与 4dp~28dp 圆角比例，Pinned TopAppBar 协同嵌套滚动（NestedScroll），全控件保持 48dp 最小触控靶区。
- **高刷新率显示与 Compose 极速渲染管线**
  - **原子性 JNI 调度**：将高频轮询的 7 次离散 JNI 跨语言调用合并为单次原子性快照接口，结合协程非阻塞 `delay`，大幅降低 CPU 与 Python GIL 争用；
  - **@Immutable 与重组隔离**：核心状态模型均标注 `@Immutable`，`LazyColumn` 绑定稳定 `key` 与 `contentType`，将实时跳动的进度拆分为独立叶子 Composable，适配 **60Hz / 90Hz / 120Hz / 144Hz** 原生高刷屏幕。
- **全方位安全保障与硬件级凭据加密 (`CryptoSecurityHelper`)**
  - **Android KeyStore + AES-256-GCM**：用户登录凭据通过硬件级 KeyStore 派生密钥加密存储，杜绝明文泄密；
  - **账号级 SHA-256 会话隔离与防 ADB 备份**：Cookie 凭据按账号隔离并在登出时自动清理，禁用系统 ADB 物理备份，强制 TLS/HTTPS 网络策略 (`network_security_config.xml`)；
  - **实时日志隐私脱敏**：密码、API Key、Bearer Token、手机号（掩码为 `138****1234`）及 Cookie 核心 Token 全自动脱敏遮盖。
- **极简交互与双语支持 (`AppStrings.kt`)**
  - 原生支持 **简体中文** 与 **English** 全局无缝切换；
  - 开屏蓝白鲸鱼 Logo 绽放动效与登录水波纹全屏展开过渡；
  - 运行状态卡片实时展示具体课程、章节、分秒级视频进度与答题题号，异常报错经人文化语义转译并支持系统通知提醒 (`NotificationHelper`)。
- **16 KB 内存分页对齐与轻量化瘦身**
  - 专为 `arm64-v8a` 64 位架构优化打包（约 41MB）；Chaquopy 内核仅使用纯 Python 依赖（`requests`、`beautifulsoup4`、`fonttools` 及内置 `pyaes`），原生支持现代 Android 15 / 16+ **16 KB Memory Page Size** 物理内存对齐规范（`useLegacyPackaging = false`）。

### 2. Python 智能刷课与解码内核 (`api/` & `android-app/src/main/python/`)
- **双流水线并发执行**：视频任务点与章节测验同步异步推进，底层引入 HTTP Keep-Alive 连接池复用 TCP/TLS 通道；
- **加密字体实时解码 (`cxsecret_font.py` & `font_decoder.py`)**：通过 `fonttools` 解析超星学习通加密字体映射表 (`resource/font_map_table.json`)，还原乱码题目与选项；
- **AI 大模型智能答题与重做机制**：支持配置 **DeepSeek** 或 **硅基流动 (SiliconFlow)** API Key（提供独立视觉红绿连通性检测与账号独占绑定）；测验未及格（< 60 分）且允许重交时自动重新作答；
- **课程讨论区自动参与**：自动检索课程 Topic / BBS 讨论帖并生成贴合内容的发言回帖。

---

## 项目结构

```text
StudyStar/
├── android-app/                      # Android Studio 原生工程 (Kotlin + Compose + Chaquopy)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/superstar/android/
│   │   │   │   ├── MainActivity.kt          # 主入口，Edge-to-Edge、高刷与开屏动效
│   │   │   │   ├── security/                # CryptoSecurityHelper 硬件加密与日志脱敏
│   │   │   │   ├── notification/            # NotificationHelper 系统异常通知
│   │   │   │   ├── license/                 # 许可证管理模块
│   │   │   │   ├── python/                  # PythonBridge JNI 桥接适配器
│   │   │   │   ├── viewmodel/               # MainViewModel 状态与持久化管理
│   │   │   │   └── ui/
│   │   │   │       ├── i18n/                # AppStrings 中英双语词典
│   │   │   │       ├── theme/               # Material Design 3 主题与色彩令牌
│   │   │   │       └── screens/             # Login / Dashboard / Settings / LegalNotice
│   │   │   ├── python/                      # 嵌入端侧运行的 Python 3.12 引擎
│   │   │   └── res/                         # 自适应图标、字体、安全配置等资源
│   │   └── build.gradle.kts                 # Gradle 构建配置 (minSdk 26, targetSdk 34, v1.5.6)
│   └── build.gradle.kts
├── api/                              # Python 核心请求、加密、字体解码与任务流水线
├── resource/                         # 加密字体映射表
├── main.py                           # 命令行 CLI 运行入口
├── app.py                            # 服务端 / Web 交互入口
├── config_template.ini               # 配置文件模板
├── pyproject.toml & requirements.txt # Python 依赖与项目元数据
└── Dockerfile                        # 容器化部署镜像配置
```

---

## 构建与运行

### 方式一：Android 客户端安装与构建
- **直接安装**：前往 [Releases 页面](https://github.com/cbyd114514/StudyStar/releases) 下载 `StudyStars.apk`（支持 Android 8.0 / API 26 及以上 `arm64-v8a` 设备）。
- **源码编译**：需配置 **JDK 17**、**Android SDK 34** 及本地 **Python 3.12**（供 Chaquopy 打包依赖），使用 Android Studio 打开 `android-app/` 目录即可构建。

### 方式二：Python CLI / Docker 运行
```bash
# 1. 安装依赖（需 Python >= 3.12）
pip install -r requirements.txt

# 2. 复制并配置 config_template.ini，或使用命令行参数运行
python main.py

# 或使用 Docker 构建运行
docker build -t studystar .
docker run -it --rm studystar
```

---

## AI 智能答题配置说明

在客户端主界面的「AI 智能答题配置」中填入对应平台 API Key 即可使用（密钥按账号加密隔离保存）：
- **DeepSeek 官方开放平台**：[https://platform.deepseek.com/api_keys](https://platform.deepseek.com/api_keys)
- **硅基流动 (SiliconFlow)**：[https://cloud.siliconflow.cn/account/ak](https://cloud.siliconflow.cn/account/ak)（支持 DeepSeek-V3 / R1 等模型）

---

## 基于中华人民共和国法律的合规与免责声明

> [!IMPORTANT]
> **特别提示**：本项目仅供计算机网络协议、Material Design 3 移动架构、JNI 调度优化与端侧硬件安全加密技术的学术研究与个人学习交流使用。用户勾选同意或运行本项目即视为完全知悉并自愿遵守以下合规约定。

1. **严禁违背学术诚信与危害网络安全**：严禁将本项目用于违反《中华人民共和国刑法》（第 285、286 条）、《网络安全法》、《数据安全法》、《个人信息保护法》及教育部《普通高等学校学生管理规定》（第 41 号令）的行为；严禁实施高频并发攻击、DDOS、有偿代刷牟利或恶意二次篡改传播。
2. **本地纯端侧安全架构**：客户端不设立收集用户账号密码与课程数据的中央云端服务器，所有凭证经 Android KeyStore 本地硬件加密并强制通过 TLS/HTTPS 与官方服务端通信。
3. **知识产权与免责 (AS-IS)**：超星学习通（ChaoXing / SuperStar）相关权益归北京世纪超星信息技术发展有限责任公司所有，本项目与官方无任何隶属或授权关系。本项目按“现状 (AS-IS)”交付，因使用产生的任何风险与后果均由用户自行承担。

---

## 致谢与贡献者 (Acknowledgements & Contributors)

- **作者**：John MICHAEL ([@cbyd114514](https://github.com/cbyd114514))
- **AI 协作**：Gemini (Google DeepMind)
- **上游内核致谢**：[SuperStar_R](https://github.com/menghuanshiguang/SuperStar_R)
