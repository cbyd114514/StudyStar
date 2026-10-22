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
        if (lang == "zh") "配置 AI 大模型可在刷课遇到视频穿插测试、章节测验与作业时，全自动调用大模型识别题目并填充提交高分答案。\n推荐使用 DeepSeek（题目理解力极强）或 硅基流动（免梯子，注册即赠送千万 Token）。"
        else "AI auto-solver automatically answers in-video quizzes, chapter tests, and assignments with high accuracy.\nRecommended: DeepSeek or SiliconFlow."

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
}
