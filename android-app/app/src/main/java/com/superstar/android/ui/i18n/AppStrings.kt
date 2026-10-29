package com.superstar.android.ui.i18n

/**
 * 学习星 (StudyStar) 全局双语国际化资源字典 (中 / 英)
 */
object AppStrings {

    const val GITHUB_REPO_URL = "https://github.com/cbyd114514/StudyStar"
    const val GITHUB_REPO_NAME = "cbyd114514/StudyStar"

    fun appName(lang: String): String = if (lang == "zh") "学习星" else "StudyStar"

    // --- 登录页 Login ---
    fun loginSubtitle(lang: String): String =
        if (lang == "zh") "超星学习通全功能辅助客户端" else "SuperStar All-in-One Client"

    fun loginNotice(lang: String): String =
        if (lang == "zh") "请注意：以下账号为超星学习通账号，密码为学习通密码。"
        else "Notice: Enter your Xuexitong username and password below."

    fun usernameLabel(lang: String): String =
        if (lang == "zh") "学习通账号 (手机号/学号)" else "Xuexitong Username (Phone/ID)"

    fun usernamePlaceholder(lang: String): String =
        if (lang == "zh") "请输入学习通账号" else "Enter your Xuexitong account"

    fun passwordLabel(lang: String): String =
        if (lang == "zh") "学习通密码" else "Xuexitong Password"

    fun passwordPlaceholder(lang: String): String =
        if (lang == "zh") "请输入学习通密码" else "Enter your password"

    fun autoLoginCheckbox(lang: String): String =
        if (lang == "zh") "自动登录 (下次启动自动进入)" else "Auto Login (Log in automatically next time)"

    fun legalCheckboxPrefix(lang: String): String =
        if (lang == "zh") "我已阅读并同意" else "I have read and agree to "

    fun legalTermsTitle(lang: String): String =
        if (lang == "zh") "《法律合规与免责声明》" else "Legal Compliance & Disclaimers"

    fun legalAgreementRequired(lang: String): String =
        if (lang == "zh") "请先阅读并勾选同意《法律合规与免责声明》"
        else "Please read and tick to agree to the Legal Compliance & Disclaimers first"

    fun legalDialogTitle(lang: String): String =
        if (lang == "zh") "基于中华人民共和国法律的合规与免责声明"
        else "Legal Compliance & Disclaimer Statement (PRC Law)"

    fun legalDialogAgree(lang: String): String =
        if (lang == "zh") "我已阅读并完全同意" else "I Have Read and Agree"

    fun legalDialogClose(lang: String): String =
        if (lang == "zh") "关闭" else "Close"

    fun settingsLegalCardTitle(lang: String): String =
        if (lang == "zh") "法律合规与免责声明" else "Legal Compliance & Disclaimers"

    fun settingsLegalCardSubtitle(lang: String): String =
        if (lang == "zh") "依据《中华人民共和国网络安全法》、《刑法》及高校学籍规范的合规声明"
        else "Compliance terms pursuant to PRC Cybersecurity Law, Criminal Law, and academic integrity regulations"

    fun settingsLegalCardBtn(lang: String): String =
        if (lang == "zh") "查阅完整法律声明条款" else "Read Full Legal Terms"

    fun legalDialogContent(lang: String): String =
        if (lang == "zh") """【前言与特别提示】
欢迎使用“学习星 (StudyStar)”开源辅助工具。在您下载、安装、登录或使用本软件前，请务必仔细阅读并充分理解本《基于中华人民共和国法律的合规与免责声明》（以下简称“本声明”）。
当您勾选“我已阅读并同意”或实际登录、运行本软件，即视为您已完全知悉、理解并自愿承诺严格遵守本声明的全部条款。如您不同意本声明的任何条款，请勿勾选并立即退出卸载本软件。

一、法律依据与合规基准
本声明依据《中华人民共和国刑法》、《中华人民共和国民法典》、《中华人民共和国网络安全法》、《中华人民共和国数据安全法》、《中华人民共和国个人信息保护法》、《中华人民共和国计算机信息系统安全保护条例》及教育部《普通高等学校学生管理规定》（教育部令第41号）等国家现行法律法规制定。用户在使用本软件过程中的一切行为均受上述法律法规约束。

二、研发宗旨与非商业学术性质
1. 本软件为开源计算机技术探索与学术研究项目，旨在研究 Android 现代响应式架构、网络协议解析与异步高并发通信、Material Design 3 人机交互规范及端侧硬件级密钥加密存储方案。
2. 开发者从未、亦绝不鼓励或诱导任何用户利用本软件从事违背高校学术诚信、规避教学考核监管、虚构在线学习记录、代考代刷或逃避正当学业义务的行为。

三、严正禁止之违法违规行为
任何个人、机构或组织在使用本软件时，严禁从事以下行为：
1. 违反《中华人民共和国刑法》第二百八十五条（非法侵入计算机信息系统罪，非法获取计算机信息系统数据、非法控制计算机信息系统罪）、第二百八十六条（破坏计算机信息系统罪）及《网络安全法》第二十七条，利用本软件对第三方服务器实施超出合理负载的高频并发请求、恶意压力测试、DDOS攻击或任何破坏、干扰网络系统正常运行的非法行为；
2. 违反教育部及各级各类高等院校的学生学籍管理规定与考试纪律，将本软件用于不正当学业竞争、考试舞弊或弄虚作假；
3. 将本软件或其衍生版本用于牟利、商业倒卖、有偿代刷代课、打包出售或作为商业增值服务提供给第三方；
4. 对本软件实施逆向工程、反编译或篡改后植入恶意代码、后门程序并进行二次传播。

四、个人信息保护与本地安全离线存储
1. 本软件为纯本地客户端架构，不设立任何收集用户账号、密码、个人身份信息或课程数据的中央云端服务器。
2. 用户输入的超星学习通账号与密码，仅在用户本地设备上使用 Android KeyStore 硬件级派生密钥，通过 AES-256-GCM 算法进行高强度加密离线存储；网络通信全程强制采用 TLS/HTTPS 协议与官方服务器直连。
3. 用户因个人设备保管不当、Root越狱、安装来源不明的恶意软件或主动向第三方泄露账号密码导致的信息泄露或资产损失，由用户自行承担全部责任。

五、知识产权与第三方权益边界
超星学习通（ChaoXing / SuperStar）及其相关商标、标识、软件著作权与接口服务权属均归北京世纪超星信息技术发展有限责任公司及其关联权利人依法所有。本软件为独立的第三方开源技术探索作品，与超星公司不存在任何形式的官方隶属、合作、认可、背书或商业授权关系。

六、免责声明与风险自担（AS-IS）
1. 本软件按“现状 (AS-IS)”与“现有”基础提供，开发者明确不对本软件的功能完整性、持续可用性、适配性或无任何运行异常作任何明示或暗示的保证。
2. 用户基于完全自主意愿下载并使用本软件，自愿承担由于使用本软件可能产生的一切直接或间接风险与法律后果（包括但不限于账号被封禁、学习进度或测验成绩被作废、受到学校通报纪律处分、或因超出合规范围使用引发的行政与民事追责）。开发者不就任何用户的个人使用行为承担任何直接、间接、附带、连带或衍生性民事或刑事法律责任。

七、声明的接受与生效
用户在登录界面勾选同意本声明，即构成用户与开发者之间具有法律效力的约束性约定。若用户对本声明的任何条款持有异议，应立即停止使用并卸载本软件。"""
        else """[Special Notice]
Welcome to "StudyStar". Before downloading, installing, logging in, or using this application, please read and fully understand this Legal Compliance & User Agreement Statement under PRC Law (hereinafter "the Statement").
By ticking "I have read and agree" or by continuing to use the software, you acknowledge that you have read, understood, and voluntarily agree to comply with all terms herein. If you disagree, do not tick the box and immediately exit and uninstall the application.

1. Legal Basis & Scope:
This Statement is formulated in strict accordance with the Criminal Law, Civil Code, Cybersecurity Law, Data Security Law, and Personal Information Protection Law of the People's Republic of China, as well as Ministry of Education regulations on student academic integrity. All user conduct is bound by these laws.

2. Purpose & Academic Nature:
This software is an open-source technical research project designed to explore mobile concurrent architectures, automated protocol parsing, Material Design 3 guidelines, and on-device hardware encryption. The developers strictly prohibit and do not condone using this software to evade academic supervision, falsify study records, or cheat.

3. Prohibited Activities:
Users are strictly prohibited from:
- Violating Articles 285 and 286 of the PRC Criminal Law and Article 27 of the PRC Cybersecurity Law by launching malicious high-concurrency requests, DDoS, or disrupting learning platforms;
- Violating institutional academic regulations, exam integrity, or student disciplinary rules;
- Commercializing, reselling, or providing paid "proxy study" services using this software;
- Reverse-engineering, tampering, or injecting malware into this software for distribution.

4. Privacy & Local Storage:
This software operates entirely client-side without central telemetry servers. User credentials are encrypted locally on the device using Android KeyStore hardware-backed AES-256-GCM encryption. The user bears all responsibility for device security.

5. Third-Party Rights & Intellectual Property:
SuperStar (ChaoXing) trademarks and platform copyrights belong to Beijing Century ChaoXing Information Technology Co., Ltd. This is an independent open-source tool without official affiliation, endorsement, or partnership.

6. Disclaimer of Warranties & Limitation of Liability:
The software is provided "AS-IS" without warranty of any kind. Users assume all direct, indirect, and disciplinary risks resulting from its usage (including account suspension, grade revocation, academic penalties, or legal liabilities). The developers shall not be liable for any user misconduct.

7. Acceptance & Effectiveness:
Ticking agreement on the login interface constitutes a legally binding acknowledgment between the user and developers. Discontinuing use and uninstalling is required if you disagree with any terms."""

    fun loginButton(lang: String): String =
        if (lang == "zh") "登录并进入控制台" else "Log In to Dashboard"

    fun loggingIn(lang: String): String =
        if (lang == "zh") "正在登录..." else "Logging in..."

    fun loginFailed(lang: String): String =
        if (lang == "zh") "登录失败" else "Login Failed"

    fun verifyingLicense(lang: String): String =
        if (lang == "zh") "正在验证许可..." else "Verifying license..."

    fun appDisabledTitle(lang: String): String =
        if (lang == "zh") "应用已被停用" else "Application Disabled"

    fun exitApp(lang: String): String =
        if (lang == "zh") "退出应用" else "Exit App"

    // --- 控制台 Dashboard ---
    fun dashboardTitle(lang: String): String =
        if (lang == "zh") "学习星 控制台" else "StudyStar Dashboard"

    fun accountPrefix(lang: String): String =
        if (lang == "zh") "账号: " else "Account: "

    fun loggedIn(lang: String): String =
        if (lang == "zh") "已登录" else "Logged in"

    fun settingsTooltip(lang: String): String =
        if (lang == "zh") "设置与日志" else "Settings & Logs"

    fun statusReady(lang: String): String =
        if (lang == "zh") "空闲就绪" else "Ready"

    fun statusRunning(lang: String): String =
        if (lang == "zh") "任务进行中" else "Task in Progress"

    fun statusCompleted(lang: String): String =
        if (lang == "zh") "上次刷课已完成" else "Previous Task Completed"

    fun statusDescReady(lang: String): String =
        if (lang == "zh") "配置参数后，点击右下角按钮启动" else "Configure parameters and click start button"

    fun statusDescRunning(lang: String): String =
        if (lang == "zh") "正在并发自动执行任务点..." else "Executing task points concurrently..."

    fun statusDescCompleted(lang: String): String =
        if (lang == "zh") "所有章节任务点已刷取完毕" else "All chapter task points finished"

    fun currentCourseLabel(lang: String): String =
        if (lang == "zh") "正在刷取课程" else "Current Course"

    fun currentChapterLabel(lang: String): String =
        if (lang == "zh") "当前进行章节" else "Current Chapter"

    fun videoProgressLabel(lang: String): String =
        if (lang == "zh") "视频播放进度" else "Video Progress"

    fun quizProgressLabel(lang: String): String =
        if (lang == "zh") "测验答题进度" else "Quiz Progress"

    fun connectingCourse(lang: String): String =
        if (lang == "zh") "正在检索/连接课程数据..." else "Connecting to course data..."

    fun analyzingChapter(lang: String): String =
        if (lang == "zh") "正在分析任务点..." else "Analyzing task points..."

    fun completedSuffix(lang: String): String =
        if (lang == "zh") "% 已完成" else "% Completed"

    fun coreParams(lang: String): String =
        if (lang == "zh") "刷课参数配置" else "Parameters Configuration"

    fun playSpeed(lang: String): String =
        if (lang == "zh") "播放速度" else "Playback Speed"

    fun concurrencyJobs(lang: String): String =
        if (lang == "zh") "并发章节数" else "Concurrent Chapters"

    fun jobsUnit(lang: String, count: Int): String =
        if (lang == "zh") "$count 章节" else "$count Chapters"

    fun customCourseIdLabel(lang: String): String =
        if (lang == "zh") "已选课程 ID（多选）" else "Selected Course IDs (Multi-select)"

    fun customCourseIdPlaceholder(lang: String): String =
        if (lang == "zh") "可在下方课程列表中直接点击多选勾选" else "Select courses from the list below"

    fun saveConfigBtn(lang: String): String =
        if (lang == "zh") "保存参数配置" else "Save Parameters"

    fun selectAllCourses(lang: String): String =
        if (lang == "zh") "全选" else "Select All"

    fun clearSelectedCourses(lang: String): String =
        if (lang == "zh") "清空" else "Clear"

    fun selectedCoursesSummary(lang: String, selectedCount: Int, totalCount: Int): String =
        if (lang == "zh") "已选 $selectedCount / $totalCount 门课程" else "Selected $selectedCount / $totalCount courses"

    fun multiSelectTip(lang: String): String =
        if (lang == "zh") "点击课程即可多选勾选，勾选后自动保存参数" else "Click courses to multi-select. Parameters auto-save."

    fun issuesTitle(lang: String): String =
        if (lang == "zh") "运行问题与重点提醒" else "Issues & Alerts"

    fun noIssues(lang: String): String =
        if (lang == "zh") "当前运行状态正常，未检测到任何异常问题" else "Running smoothly, no issues detected."

    fun clearIssuesBtn(lang: String): String =
        if (lang == "zh") "清除提醒" else "Clear Alerts"

    fun saveApiKeyToAccount(lang: String): String =
        if (lang == "zh") "保存 API Key 到当前账号" else "Save API Key to Account"

    fun apiKeySaved(lang: String): String =
        if (lang == "zh") "已保存至当前账号" else "Saved to Account"

    fun courseSelection(lang: String): String =
        if (lang == "zh") "课程选择" else "Course Selection"

    fun courseCountUnit(lang: String, count: Int): String =
        if (lang == "zh") "${count}门" else "$count Courses"

    fun fetchCoursesBtn(lang: String): String =
        if (lang == "zh") "从学习通获取我的课程列表" else "Fetch My Course List from Xuexitong"

    fun fetchingCourses(lang: String): String =
        if (lang == "zh") "正在拉取课程列表..." else "Fetching course list..."

    fun webVerifyTitle(lang: String): String =
        if (lang == "zh") "学习通网页版课程地址（核对 ID）" else "Xuexitong Web URL (Verify Course ID)"

    fun webVerifyDesc(lang: String): String =
        if (lang == "zh") "可在网页端进入课程后，从浏览器地址栏查看 courseId 以确认精确 ID：\nhttps://mooc1-1.chaoxing.com/visit/interaction"
        else "Check the courseId in the browser address bar after entering course:\nhttps://mooc1-1.chaoxing.com/visit/interaction"

    fun openWebBtn(lang: String): String =
        if (lang == "zh") "打开学习通网页版核对课程" else "Open Xuexitong Web Portal"

    fun viewFetchedCourses(lang: String, count: Int): String =
        if (lang == "zh") "查看已获取课程 (${count}门)" else "View Fetched Courses ($count)"

    fun hideCourseList(lang: String): String =
        if (lang == "zh") "收起课程列表" else "Hide Course List"

    fun selectCourse(lang: String): String =
        if (lang == "zh") "选择" else "Select"

    fun selectedCourse(lang: String): String =
        if (lang == "zh") "已选择" else "Selected"

    fun aiConfigTitle(lang: String): String =
        if (lang == "zh") "AI 智能答题配置" else "AI Quiz Solver Configuration"

    fun aiEnabled(lang: String): String =
        if (lang == "zh") "已启用" else "Enabled"

    fun aiGuideTitle(lang: String): String =
        if (lang == "zh") "AI 智能答题使用说明" else "AI Quiz Solver Guide"

    fun aiGuideDesc(lang: String): String =
        if (lang == "zh") """一、功能简介：
配置 AI 大模型后，在刷课遇到视频穿插测试、章节测验与课后作业时，系统全自动识别题目并调用大模型计算答案，自动提交作答。

二、如何获取官方 API Key：
1. DeepSeek 官方开放平台：
网址：https://platform.deepseek.com
注册登录后，进入「API Keys」页面创建密钥。DeepSeek 逻辑推理强，答题准确率高。

2. 硅基流动 (SiliconFlow) 开放平台：
网址：https://cloud.siliconflow.cn
免翻墙，注册即赠送大量额度（相当于数千万 Token），足够刷完多门课程测验。在「API 密钥」中创建并复制。

三、配置与安全保障：
1. 选择预设提供商或自定义「其他AI」。
2. 粘贴 API Key，系统会自动通过 Android Keystore 硬件级 AES-256-GCM 隔离加密存储，即便 Root 手机也无法提取明文，保障账号安全。
3. 点击「测试大模型连通性」验证有效性，自动随当前账号保存。"""
        else """1. Overview:
AI auto-solver automatically answers in-video quizzes, chapter tests, and assignments with high accuracy using large language models.

2. Official API Key Registration:
- DeepSeek Portal: https://platform.deepseek.com (High accuracy)
- SiliconFlow Portal: https://cloud.siliconflow.cn (Free tokens upon sign-up)

3. Configuration & Security:
Select provider, paste your API Key. The key is encrypted at rest using Android Keystore AES-256-GCM hardware protection. Test connectivity to verify."""

    fun otherAi(lang: String): String =
        if (lang == "zh") "其他AI" else "Other AI"

    fun selectedCoursesCount(lang: String, count: Int): String =
        if (lang == "zh") "已选课程: $count 门" else "Selected: $count course(s)"

    fun noCourseSelected(lang: String): String =
        if (lang == "zh") "未选择课程（请在下方课程列表中勾选）" else "No course selected (select below)"

    fun closeBtn(lang: String): String =
        if (lang == "zh") "关闭" else "Close"

    fun aiQuickUrls(lang: String): String =
        if (lang == "zh") "快捷获取 API Key 官方网址" else "Official API Key Registration"

    fun deepseekBtn(lang: String): String =
        if (lang == "zh") "DeepSeek 官网" else "DeepSeek Portal"

    fun siliconFlowBtn(lang: String): String =
        if (lang == "zh") "硅基流动官网" else "SiliconFlow Portal"

    fun selectProviderTip(lang: String): String =
        if (lang == "zh") "选择大模型服务商（点击自动填入预设）" else "Select Provider (Auto-fill Presets)"

    fun endpointLabel(lang: String): String =
        if (lang == "zh") "API Endpoint (接口地址)" else "API Endpoint URL"

    fun apiKeyLabel(lang: String): String =
        if (lang == "zh") "API Key (粘贴从官网复制的密钥)" else "API Key"

    fun modelLabel(lang: String): String =
        if (lang == "zh") "模型代号 (Model)" else "Model Name"

    fun testLlmBtn(lang: String): String =
        if (lang == "zh") "测试大模型连通性" else "Test LLM Connectivity"

    fun llmTesting(lang: String): String =
        if (lang == "zh") "正在检测连通性..." else "Testing connection..."

    fun llmSuccess(lang: String): String =
        if (lang == "zh") "大模型连通成功！服务响应正常" else "LLM Connected Successfully!"

    fun llmFailed(lang: String): String =
        if (lang == "zh") "大模型连通失败" else "LLM Connection Failed"

    fun llmConnectedBadge(lang: String): String =
        if (lang == "zh") "连通正常" else "Connected"

    fun llmFailedBadge(lang: String): String =
        if (lang == "zh") "连通异常" else "Connection Failed"

    fun startStudyBtn(lang: String): String =
        if (lang == "zh") "开始刷课" else "Start Study"

    fun stopStudyBtn(lang: String): String =
        if (lang == "zh") "停止刷课" else "Stop Study"

    // --- 设置页 Settings ---
    fun settingsTitle(lang: String): String =
        if (lang == "zh") "系统设置与控制" else "Settings & Controls"

    fun accountCardTitle(lang: String): String =
        if (lang == "zh") "当前登录账号" else "Current Account"

    fun projectCardTitle(lang: String): String =
        if (lang == "zh") "开源项目主页 (GitHub)" else "Open Source Project (GitHub)"

    fun projectCardDesc(lang: String): String =
        if (lang == "zh") "学习星 (StudyStar) 官方开源代码仓库，采用 Material Design 3、16 KB 物理分页与 120Hz/144Hz 极清高刷架构。"
        else "StudyStar Official Repository, rebuilt with Material Design 3, 16 KB page size and 120Hz/144Hz high refresh rate."

    fun openGithubBtn(lang: String): String =
        if (lang == "zh") "前往 GitHub 仓库" else "Open GitHub Repository"

    fun languageTitle(lang: String): String =
        if (lang == "zh") "界面语言 / Language" else "Interface Language"

    fun langZh(lang: String): String =
        if (lang == "zh") "简体中文" else "Simplified Chinese"

    fun langEn(lang: String): String =
        if (lang == "zh") "English" else "English"

    fun logsTitle(lang: String): String =
        if (lang == "zh") "实时运行日志控制台" else "Real-time Log Console"

    fun autoScroll(lang: String): String =
        if (lang == "zh") "自动滚动" else "Auto-Scroll"

    fun copyLogs(lang: String): String =
        if (lang == "zh") "复制日志" else "Copy"

    fun clearLogs(lang: String): String =
        if (lang == "zh") "清空" else "Clear"

    fun logsEmpty(lang: String): String =
        if (lang == "zh") "暂无日志输出" else "No logs recorded yet"

    fun logoutBtn(lang: String): String =
        if (lang == "zh") "退出登录" else "Log Out"

    fun copiedToast(lang: String): String =
        if (lang == "zh") "日志已复制到剪贴板" else "Logs copied to clipboard"

    /**
     * 运行时日志与异常说明双语即时翻译器
     * 在英文模式下，自动将 Python 端与 ViewModel 的中文日志转化为专业地道的英文
     */
    fun translateLog(rawMessage: String, lang: String): String {
        if (lang == "zh") return rawMessage
        var msg = rawMessage

        // 1. 中括号标签翻译
        msg = msg.replace("[作业]", "[Quiz]")
            .replace("[视频]", "[Video]")
            .replace("[阅读]", "[Reading]")
            .replace("[讨论]", "[Discussion]")
            .replace("[空页面]", "[Page]")
            .replace("[Cookie]", "[Cookie]")
            .replace("[登录]", "[Login]")
            .replace("[并行]", "[Parallel]")
            .replace("[配置]", "[Config]")
            .replace("[错误]", "[Error]")
            .replace("[警告]", "[Warning]")
            .replace("[提示]", "[Tip]")
            .replace("[其他任务]", "[Task]")
            .replace("[AI]", "[AI]")
            .replace("[SiliconFlow]", "[SiliconFlow]")
            .replace("[系统]", "[System]")
            .replace("[解析]", "[Parser]")

        // 2. 作业与测验相关语句
        msg = msg.replace(Regex("""测验成绩:\s*([0-9.]+)\s*分，已达到及格线\(60分\)"""), "Quiz score: $1 pts, passed passing score (60 pts)")
        msg = msg.replace(Regex("""测验成绩:\s*([0-9.]+)\s*分，未达及格线\(60分\)，正在自动重做并重新提交\.\.\."""), "Quiz score: $1 pts, below passing score (60 pts), automatically retaking and resubmitting...")
        msg = msg.replace("测验成绩:", "Quiz score:")
        msg = msg.replace("分，已达到及格线(60分)", " pts, passed passing score (60 pts)")
        msg = msg.replace("分，未达及格线(60分)", " pts, below passing score (60 pts)")
        msg = msg.replace("正在自动重做并重新提交...", "automatically retaking and resubmitting...")
        msg = msg.replace("正在自动重做并重新提交", "automatically retaking and resubmitting")
        msg = msg.replace("重做作答完成，重新提交答案...", "Retake completed, resubmitting answers...")
        msg = msg.replace("不及格重做已达到最大轮次(4轮)，继续后续任务", "Reached max retake rounds (4 rounds), continuing next task")
        msg = msg.replace("该测验不支持重做或重做接口不可用", "Quiz does not support retake or retake API unavailable")
        msg = msg.replace("正在提交答案至", "Submitting answers to")
        msg = msg.replace("提交答题成功", "Quiz submitted successfully")
        msg = msg.replace("提交答题失败", "Quiz submission failed")
        msg = msg.replace("已通过大模型作答完成", "Answered via LLM")
        msg = msg.replace("未配置大模型或作答失败，随机选择保底", "LLM not configured or failed, fallback to random choice")
        msg = msg.replace("正在解析题目...", "Parsing quiz questions...")
        msg = msg.replace("成功解析题目，准备作答...", "Questions parsed successfully, preparing to answer...")
        msg = msg.replace("未获取到题目", "No quiz questions available")
        msg = msg.replace("教师未创建完成该测验", "Instructor has not published this quiz")
        msg = msg.replace("测验题目参数已更新或未到开放作答时间，已自动记录并继续后续学习", "Quiz parameters updated or not yet open; automatically recorded and proceeding")

        // 3. 视频播放相关语句
        msg = msg.replace("开始播放:", "Start playing:")
        msg = msg.replace("开始播放", "Start playing")
        msg = msg.replace("播放完成:", "Playback completed:")
        msg = msg.replace("播放完成", "Playback completed")
        msg = msg.replace("视频播放进度:", "Video progress:")
        msg = msg.replace("正在跳过", "Skipping")

        // 4. 讨论与阅读
        msg = msg.replace("自动参与讨论成功", "Participated in discussion successfully")
        msg = msg.replace("自动参与讨论容错:", "Discussion fallback:")
        msg = msg.replace("讨论已发表", "Discussion posted")

        // 5. 章节与并发执行
        msg = msg.replace("线程处理章节:", "Thread processing chapter:")
        msg = msg.replace("章节完成:", "Chapter completed:")
        msg = msg.replace("重试章节", "Retrying chapter")
        msg = msg.replace("未知章节状态:", "Unknown chapter status:")
        msg = msg.replace("章节未开放已达最大重试次数:", "Chapter not open, reached max retries:")
        msg = msg.replace("章节处理失败已达最大重试次数:", "Chapter failed, reached max retries:")
        msg = msg.replace("章节未开放", "Chapter not open")

        // 6. 课程与刷课总任务
        msg = msg.replace("===== 开始刷课任务 =====", "===== Starting study tasks =====")
        msg = msg.replace("===== 刷课任务完成 =====", "===== All study tasks completed =====")
        msg = msg.replace("刷课任务全部完成!", "All study tasks finished successfully!")
        msg = msg.replace("刷课任务已启动", "Study task started")
        msg = msg.replace("刷课任务已停止", "Study task stopped")
        msg = msg.replace("刷课任务被用户停止", "Study task stopped by user")
        msg = msg.replace("刷课任务未完成或异常:", "Study task not finished or exception:")
        msg = msg.replace("课程学习完成:", "Course completed:")
        msg = msg.replace("开始学习课程:", "Starting course:")
        msg = msg.replace("正在获取课程列表...", "Fetching course list...")
        msg = msg.replace("获取课程列表失败", "Failed to fetch course list")
        msg = msg.replace(Regex("""获取到\s*(\d+)\s*门课程"""), "Fetched $1 courses")
        msg = msg.replace(Regex("""已按多选配置筛选出\s*(\d+)\s*门待学课程"""), "Filtered $1 courses to study by selection")
        msg = msg.replace("未勾选任何课程，已取消默认全选刷课模式。请在课程列表中勾选课程后重试", "No courses selected. Please select courses in the list and retry.")
        msg = msg.replace("勾选的课程 ID 在当前账号课程列表中未匹配到，请重新勾选", "Selected course IDs not matched in course list, please select again")
        msg = msg.replace("使用已获取的课程列表，避免重复请求", "Using cached course list to avoid repeated requests")
        msg = msg.replace("请在课程列表中勾选需要学习的课程（支持多选）", "Please select courses from the list (multi-selection supported)")
        msg = msg.replace("未检测到可选课程，请检查课程是否已结课或已全部完成", "No courses found; please check if courses have ended or are already completed")
        msg = msg.replace("当前章节所有可处理任务点已顺利完成", "All available task points in current chapter completed smoothly")

        // 7. 登录与会话凭据
        msg = msg.replace("正在检查已保存的登录状态...", "Checking saved login status...")
        msg = msg.replace(Regex("""已加载\s*(\d+)\s*个 cookies"""), "Loaded $1 cookies")
        msg = msg.replace("关键凭据状态:", "Key credentials status:")
        msg = msg.replace("未找到已保存的登录状态", "No saved login status found")
        msg = msg.replace("===== 尝试使用已保存的登录状态 =====", "===== Trying saved login status =====")
        msg = msg.replace("===== 登录状态有效，跳过密码登录 =====", "===== Login status valid, skipping password login =====")
        msg = msg.replace("===== 登录状态已失效，尝试使用账号密码重新登录 =====", "===== Login status expired, retrying login with credentials =====")
        msg = msg.replace("正在使用账号密码登录:", "Logging in with credentials:")
        msg = msg.replace("登录成功", "Login successful")
        msg = msg.replace("登录失败:", "Login failed:")
        msg = msg.replace("登录失败，无法开始刷课", "Login failed, unable to start study")
        msg = msg.replace("登录失败，无法获取课程列表", "Login failed, unable to fetch courses")
        msg = msg.replace("已退出登录", "Logged out")
        msg = msg.replace("请先登录", "Please log in first")
        msg = msg.replace("学习通账号登录失效，请在登录页面检查手机号与密码", "Login session expired or invalid credentials; please check your phone/student ID and password")
        msg = msg.replace(Regex("""已安全持久化\s*(\d+)\s*项会话凭据"""), "Safely persisted $1 session credentials")
        msg = msg.replace(Regex("""已安全载入\s*(\d+)\s*项专属会话凭据"""), "Safely loaded $1 session credentials")
        msg = msg.replace("已清除本地会话缓存", "Cleared local session cache")
        msg = msg.replace("保存会话凭据失败:", "Failed to save session credentials:")
        msg = msg.replace("载入会话凭据失败:", "Failed to load session credentials:")
        msg = msg.replace("清除会话缓存失败:", "Failed to clear session cache:")
        msg = msg.replace("已加载保存的账号:", "Loaded saved account:")

        // 8. 大模型与参数配置
        msg = msg.replace("大模型初始化成功:", "LLM initialized successfully:")
        msg = msg.replace("大模型初始化失败:", "LLM initialization failed:")
        msg = msg.replace("大模型初始化异常:", "LLM initialization exception:")
        msg = msg.replace("大模型答题已配置", "LLM quiz solver configured")
        msg = msg.replace("大模型连接测试成功", "LLM connection test succeeded")
        msg = msg.replace("大模型连接测试失败", "LLM connection test failed")
        msg = msg.replace("已恢复此账号关联的 API Key 大模型配置", "Restored LLM API Key config for this account")
        msg = msg.replace("已恢复此账号历史勾选的课程", "Restored selected courses for this account")
        msg = msg.replace("API Key 与大模型配置已加密保存至当前账号", "API Key and LLM config safely encrypted and saved to account")
        msg = msg.replace("配置已保存:", "Configuration saved:")
        msg = msg.replace("已加载保存的配置:", "Loaded saved configuration:")
        msg = msg.replace("速度=", "Speed=")
        msg = msg.replace("并发=", "Concurrency=")
        msg = msg.replace("收到大模型配置:", "Received LLM configuration:")
        msg = msg.replace("使用大模型配置:", "Using LLM configuration:")
        msg = msg.replace("收到已获取的课程列表:", "Received cached course list:")
        msg = msg.replace("使用已获取的课程列表", "Using cached course list")
        msg = msg.replace("Python 运行环境就绪:", "Python runtime ready:")
        msg = msg.replace("Python 环境初始化提示:", "Python environment notice:")
        msg = msg.replace("环境检测通过", "Environment check passed")
        msg = msg.replace("环境检测失败:", "Environment check failed:")
        msg = msg.replace("正在请求", "Requesting")
        msg = msg.replace("获取到答案:", "Answer received:")
        msg = msg.replace("API请求失败:", "API request failed:")
        msg = msg.replace("请求异常:", "Request exception:")
        msg = msg.replace("请求失败:", "Request failed:")
        msg = msg.replace("已达最大重试次数", "Reached max retries")
        msg = msg.replace("无法解析大模型输出内容:", "Cannot parse LLM response:")

        // 9. 异常与网络容错
        msg = msg.replace("学习通服务器访问受限，请稍候几分钟再试或切换手机网络", "Server access temporarily restricted; please wait a few minutes or switch network")
        msg = msg.replace("网络连接中断，请检查手机 Wi-Fi 或移动数据网络", "Network disconnected; please check your Wi-Fi or mobile data")
        msg = msg.replace("网络请求超时，系统正在自动重试", "Network request timed out; automatically retrying")
        msg = msg.replace("学习通服务器返回数据格式异常，已自动跳过并继续", "Server response format abnormal; automatically skipped and continuing")
        msg = msg.replace("已有刷课任务在后台运行中，无需重复开启", "A study task is already running in background")
        msg = msg.replace("任务遇到微小网络波动，系统正在自动容错处理", "Minor network glitch detected; automatically handling error")
        msg = msg.replace("刷课任务已在运行中", "Study task is already running")
        msg = msg.replace("刷课任务异常:", "Study task exception:")
        msg = msg.replace("完整堆栈:", "Full stack trace:")
        msg = msg.replace("解析课程列表失败:", "Failed to parse course list:")
        msg = msg.replace("加载配置失败:", "Failed to load config:")

        return msg
    }
}
