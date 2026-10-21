<br />
<p align="center">
  <h2 align="center" style="font-weight: 600">🌟 学习星 (StudyStar) Android</h2>

  <p align="center">
    超星学习通全功能辅助客户端 · 现代 Material Design 3 极清高刷 Android 原生重塑版
  </p>
</p>

基于开源项目 [SuperStar_R](https://github.com/menghuanshiguang/SuperStar_R) 核心，采用 Google 最新 **Jetpack Compose** 与 **Material Design 3 (Material You)** 标准全面重塑的 Android 客户端。

---

## 🌟 特性一览

- 📱 **原生 Android 规范与沉浸全面屏**
  - 采用 Google Jetpack Compose 全响应式重绘，取消繁琐底部导航栏，右上角直通常用设置与内置日志控制台；
  - 完美适配异形挖孔全面屏短边延展（`shortEdges` Display Cutout）与沉浸式 Edge-to-Edge 视觉视界。
- 🎬 **开场品牌动画与登录展开全屏动效**
  - **开屏品牌动效**：应用启动时呈现轻柔弹性的蓝白鲸鱼少女 Logo 绽放动画与标语，平滑过渡至主界面；
  - **登录按钮展开全屏动效**：点击登录成功后，以登录按钮为中心展开圆形水波涟漪，无缝展开并揭示整个全屏控制台。
- 🔐 **自动登录小勾（自主可控）**
  - 自动登录默认**不勾选**，登录按钮下方提供独立的小勾选框；
  - 勾选后下次启动自动进入；不勾选则仅记住账号密码，等待手动点击；
  - 点击“退出登录”后自动锁定防自动重连，彻底避免登出后死循环重新登录。
- 🌐 **全应用无缝双语切换 (Bilingual Support)**
  - 深度支持 **简体中文 (Simplified Chinese)** 与 **English** 全局无缝切换；
  - 控制台、登录页、设置面板、卡片与提示文案全场景 100% 对应动态翻译，随时在设置中一键自由切换。
- ⚡ **120Hz / 144Hz 满帧高刷新率 & 课程列表虚拟化优化**
  - 彻底优化课程选择展开后的卡片测量与渲染，采用 Compose 顶层虚拟化惰性加载（Lazy Virtualization），彻底消除长列表滑动卡顿与掉帧；
  - 原生对接 Android 11+ 及 Android 14~17 高帧率物理显示通道，解除澎湃 OS 省电锁 60Hz 限制，开启全局硬件加速。
- 🪶 **深度轻量化瘦身（约 41MB）**
  - 专为现代 Android 64 位芯片深度定制打包（`arm64-v8a` 架构），体积相比上一版缩减超 30%，内存占用低，冷启动极速。
- 🚀 **16 KB 内存分页 & 最新平台支持**
  - 核心底层升级 Chaquopy 17.1.0 与 Python 3.12，全面原生支持 **16 KB Memory Page Size**；
  - 完美适配高通骁龙 8 至尊版（Snapdragon 8 Elite / Oryon）、**红米 K90 MAX / 小米澎湃 OS (HyperOS) / Android 15、16、17** 等最新旗舰设备与前瞻系统。
- 📍 **实时课时与章节状态动态追踪**
  - 任务运行时，控制台实时动态醒目呈现**当前正在刷取的具体课程名称**与**当前进行章节任务点**，进度一目了然。
- 🌐 **学习通网页版课程地址核对**
  - 课程选择卡片处内置学习通网页版交互中心直达链接（`https://mooc1-1.chaoxing.com/visit/interaction`），一键在浏览器打开，查看对应地址栏的 `courseId` 进行精准核对。
- 🔔 **系统级异常报错通知提醒**
  - 遇到网络中断、账号下线、刷课报错或异常时，自动唤起 Android 系统级悬浮与状态栏通知（内置节流防骚扰保护），锁屏及后台状态下也能即刻获知。
- 🤖 **AI 大模型智能答题**
  - 支持一键直达 DeepSeek 官网与硅基流动（SiliconFlow）平台获取 API Key；
  - 全自动识别随堂视频测试题、章节测验与课后作业，自动解析并提交高分答案。
- ⚙️ **设置与防抖实时日志中心**
  - 位于主界面右上角齿轮按钮直达；
  - 内置 GitHub 官方开源仓库直跳卡片；
  - 实时日志控制台支持上下自由拖动查阅（用户交互时自动暂停滚动，杜绝跳屏抖动）、一键复制全部日志与清空日志。

---

## 📁 项目结构

```
SuperStar_R_Android/
├── app-debug.apk         # 预编译好的最新「学习星」Release/Debug 安装包
├── android-app/          # Android Studio 项目工程
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/superstar/android/
│   │   │   │   ├── MainActivity.kt      # 主入口，Edge-to-Edge 与开屏动画
│   │   │   │   ├── notification/        # NotificationHelper 系统级报错通知
│   │   │   │   ├── license/             # 远程/本地许可证管理（平滑放行）
│   │   │   │   ├── python/              # PythonBridge 桥接适配器
│   │   │   │   ├── viewmodel/           # MainViewModel 业务、持久化与自动登录控制
│   │   │   │   └── ui/
│   │   │   │       ├── i18n/            # AppStrings 全局双语国际化字典
│   │   │   │       ├── theme/           # Material Design 3 Ocean Blue 主题规范
│   │   │   │       └── screens/         # Dashboard / Login / Settings 界面
│   │   │   ├── python/                  # Python 刷课内核适配脚本
│   │   │   └── res/                     # 全密度自适应图标、字体与资源
│   │   └── build.gradle.kts
│   └── build.gradle.kts
├── api/                  # Python 核心 API
├── main.py               # 原生 Python 调试入口
└── requirements.txt      # Python 依赖
```

---

## 🛠️ 构建与运行要求

- **系统与硬件兼容性**：
  - 支持 Android 8.0 (API 26) 及以上全版本；
  - **全面兼容 16 KB 内存分页架构**：完美适配高通骁龙 8 至尊版（Snapdragon 8 Elite / Oryon 架构）、**红米 K90 MAX、小米澎湃 OS (Xiaomi HyperOS) 以及 Android 15 / 16 / 17+**；
  - 所有原生动态链接库（.so）均按照 ELF 16 KB (`0x4000`) 标准完成对齐打包，杜绝 Bionic Linker 动态链接报错与闪退。
- **构建环境**：
  - JDK 17 (推荐 OpenJDK 17)
  - Android SDK 34 / 35
  - Python 3.12 (与 Chaquopy 17.1.0 完美匹配)

---

## 📲 安装与使用指引

### 桌面图标刷新说明（小米澎湃 OS 专解）
如果安装新版后桌面快捷方式仍显示五角星，这是小米澎湃 OS（HyperOS）桌面数据库对旧快捷方式的持久化缓存引起的。可通过以下任意一种简便方法立即刷新为蓝白鲸鱼少女新图标：
- **方式一（推荐，无需关机）**：进入手机「设置」->「应用设置」->「应用管理」-> 搜索「系统桌面」-> 点击底部的「结束运行」（或清除缓存）。系统桌面重新加载时就会立即刷新为蓝白鲸鱼少女新图标！
- **方式二（最省心）**：直接**重启手机**，开机后系统桌面将自动重建图标索引；
- **方式三**：长按桌面的旧五角星图标选择「移除」，然后从应用抽屉（所有应用）中重新将「学习星」拖动到桌面上。

### AI 智能答题配置说明
在主界面的「AI 智能答题配置」中，支持一键点击官方网址直接前往注册并复制 API Key：
- **DeepSeek 官方开放平台**：`https://platform.deepseek.com/api_keys`（推理逻辑极强，题目识别准确率极高）；
- **硅基流动 (SiliconFlow)**：`https://cloud.siliconflow.cn/account/ak`（国内直连免梯子，注册即赠送 2000 万 Token，支持 DeepSeek-V3 / R1）。

### 常用操作步骤
1. 打开「学习星」，欣赏开屏品牌动画；
2. 输入**超星学习通账号（手机号/学号）**与**学习通密码**，可按需勾选下方“自动登录”小勾；
3. 点击“登录并进入控制台”，体验从按钮展开到全屏的平滑视觉过渡；
4. 在控制台调整播放倍速、并发章节数，或点击“从学习通获取我的课程列表”选择指定课程（可点击下方网页版链接核对 ID）；
5. 点击右下角 **“开始刷课”** 按钮，卡片将动态高亮呈现正在刷取的**具体课程**与**具体章节**；
6. 如需查阅日志或切换语言，点击右上角齿轮图标进入「设置与控制」即可。

---

## ⚠️ 免责声明

本项目仅供个人学习与技术交流研究使用，请勿用于任何商业用途或违反学校规定的行为。使用本项目产生的一切后果由使用者自行承担。

---

## 🤝 致谢

- [SuperStar_R](https://github.com/menghuanshiguang/SuperStar_R) - 原始 Python 项目内核
