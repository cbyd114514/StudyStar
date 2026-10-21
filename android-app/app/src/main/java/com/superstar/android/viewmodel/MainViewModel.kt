package com.superstar.android.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.superstar.android.notification.NotificationHelper
import com.superstar.android.python.PythonBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// DataStore 实例
private val Context.configDataStore by preferencesDataStore(name = "app_config")

// DataStore 键
private val SPEED_KEY = doublePreferencesKey("speed")
private val JOBS_KEY = intPreferencesKey("jobs")
private val COURSE_IDS_KEY = stringPreferencesKey("course_ids")
private val USERNAME_KEY = stringPreferencesKey("saved_username")
private val PASSWORD_KEY = stringPreferencesKey("saved_password")
private val LLM_CONFIG_KEY = stringPreferencesKey("llm_config")
private val LANGUAGE_KEY = stringPreferencesKey("app_language")
private val AUTO_LOGIN_KEY = booleanPreferencesKey("auto_login")

/**
 * 应用主 ViewModel
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // 登录状态
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // 刷课状态
    private val _studyState = MutableStateFlow(StudyState())
    val studyState: StateFlow<StudyState> = _studyState.asStateFlow()

    // 原始日志（全部保留，用于 UI 直接显示）
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    // 去重后的日志（连续重复的日志合并计数，用于需要精简视图的场景）
    private val _deduplicatedLogs = MutableStateFlow<List<LogEntry>>(emptyList())
    val deduplicatedLogs: StateFlow<List<LogEntry>> = _deduplicatedLogs.asStateFlow()

    // 课程列表
    private val _courses = MutableStateFlow<List<CourseInfo>>(emptyList())
    val courses: StateFlow<List<CourseInfo>> = _courses.asStateFlow()

    private val _isLoadingCourses = MutableStateFlow(false)
    val isLoadingCourses: StateFlow<Boolean> = _isLoadingCourses.asStateFlow()

    // Python 环境状态
    private val _pythonState = MutableStateFlow("未初始化")
    val pythonState: StateFlow<String> = _pythonState.asStateFlow()

    // 当前页面
    private val _currentPage = MutableStateFlow(Page.LOGIN)
    val currentPage: StateFlow<Page> = _currentPage.asStateFlow()

    // 界面语言状态 (zh / en)
    private val _currentLanguage = MutableStateFlow("zh")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    // 自动登录勾选状态 (默认不勾选 false)
    private val _autoLogin = MutableStateFlow(false)
    val autoLogin: StateFlow<Boolean> = _autoLogin.asStateFlow()

    // 大模型连通性独立测试状态（不写入日志，直接在 UI 板块变色反馈）
    private val _llmTestState = MutableStateFlow<LlmTestState>(LlmTestState.Idle)
    val llmTestState: StateFlow<LlmTestState> = _llmTestState.asStateFlow()

    fun clearLlmTestState() {
        _llmTestState.value = LlmTestState.Idle
    }

    // 标记用户是否手动点击了登出（登出后绝对禁止静默自动登录）
    val userManuallyLoggedOut = MutableStateFlow(false)

    fun setAutoLogin(enabled: Boolean) {
        _autoLogin.value = enabled
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                context.configDataStore.edit { prefs ->
                    prefs[AUTO_LOGIN_KEY] = enabled
                }
            } catch (_: Exception) {}
        }
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                context.configDataStore.edit { prefs ->
                    prefs[LANGUAGE_KEY] = lang
                }
            } catch (_: Exception) {}
        }
    }

    fun logout() {
        viewModelScope.launch {
            stopStudy()
            userManuallyLoggedOut.value = true
            _loginState.value = LoginState(
                savedUsername = _loginState.value.savedUsername,
                savedPassword = _loginState.value.savedPassword
            )
            _currentPage.value = Page.LOGIN
            addLog("已退出登录", LogType.INFO)
        }
    }

    // 底部导航栏当前选中的 tab (保留向后兼容)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        initializePython()
        loadConfig()
    }

    private fun initializePython() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _pythonState.value = "正在初始化 Python 环境..."
                PythonBridge.initialize(getApplication())
                val version = PythonBridge.testEnvironment()
                _pythonState.value = version
                addLog("Python 运行环境就绪: $version", LogType.INFO)
            } catch (t: Throwable) {
                _pythonState.value = "初始化异常: ${t.message}"
                addLog("Python 环境初始化提示: ${t.message}", LogType.ERROR)
            }
        }
    }

    /**
     * 从 DataStore 加载持久化的配置和账号
     */
    private fun loadConfig() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val data = context.configDataStore.data.first()
                val savedSpeed = data[SPEED_KEY] ?: 1.0
                val savedJobs = data[JOBS_KEY] ?: 4
                val savedCourseIds = data[COURSE_IDS_KEY]
                val savedUsername = data[USERNAME_KEY]
                val savedPassword = data[PASSWORD_KEY]
                val savedLlmConfig = data[LLM_CONFIG_KEY]
                val savedLanguage = data[LANGUAGE_KEY] ?: "zh"
                val savedAutoLogin = data[AUTO_LOGIN_KEY] ?: false
                _currentLanguage.value = savedLanguage
                _autoLogin.value = savedAutoLogin

                _studyState.value = _studyState.value.copy(
                    speed = savedSpeed,
                    jobs = savedJobs,
                    courseIds = savedCourseIds,
                    llmConfig = savedLlmConfig
                )

                // 如果有保存的账号密码，自动填充到登录状态
                if (!savedUsername.isNullOrBlank() && !savedPassword.isNullOrBlank()) {
                    _loginState.value = _loginState.value.copy(
                        savedUsername = savedUsername,
                        savedPassword = savedPassword
                    )
                    addLog("已加载保存的账号: $savedUsername", LogType.INFO)
                }

                addLog("已加载保存的配置: 速度=${savedSpeed}x, 并发=${savedJobs}", LogType.INFO)
                if (!savedLlmConfig.isNullOrBlank()) {
                    addLog("已加载大模型配置", LogType.INFO)
                }
            } catch (t: Throwable) {
                addLog("加载配置失败: ${t.message}", LogType.WARNING)
            }
        }
    }

    /**
     * 更新并持久化配置
     */
    fun updateConfig(
        speed: Double,
        jobs: Int,
        courseIds: String?,
        llmConfig: String? = null
    ) {
        viewModelScope.launch {
            _studyState.value = _studyState.value.copy(
                speed = speed,
                jobs = jobs,
                courseIds = courseIds,
                llmConfig = llmConfig ?: _studyState.value.llmConfig
            )

            try {
                val context = getApplication<Application>()
                context.configDataStore.edit { prefs ->
                    prefs[SPEED_KEY] = speed
                    prefs[JOBS_KEY] = jobs
                    if (courseIds != null) {
                        prefs[COURSE_IDS_KEY] = courseIds
                    } else {
                        prefs.remove(COURSE_IDS_KEY)
                    }
                    if (llmConfig != null) {
                        prefs[LLM_CONFIG_KEY] = llmConfig
                    }
                }
                addLog("配置已保存: 速度=${speed}x, 并发=${jobs}", LogType.SUCCESS)
            } catch (e: Exception) {
                addLog("保存配置失败: ${e.message}", LogType.ERROR)
            }
        }
    }

    /**
     * 保存账号密码到本地（登录状态保持）
     */
    private fun saveCredentials(username: String, password: String) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                context.configDataStore.edit { prefs ->
                    prefs[USERNAME_KEY] = username
                    prefs[PASSWORD_KEY] = password
                }
            } catch (e: Exception) {
                // 静默处理，不影响用户体验
            }
        }
    }


    fun clearLoginError() {
        _loginState.value = _loginState.value.copy(error = null)
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)
            addLog("正在登录...", LogType.INFO)

            try {
                PythonBridge.executeLogin(
                    username = username,
                    password = password,
                    onLog = { msg -> addLog(msg, LogType.INFO) },
                    onComplete = { success, msg ->
                        if (success) {
                            // 登录成功后保存账号密码到本地
                            saveCredentials(username, password)
                            userManuallyLoggedOut.value = false
                            _loginState.value = _loginState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                username = username,
                                password = password,
                                error = null // 关键：彻底清除任何残留错误，杜绝出现 failure
                            )
                            addLog("登录成功", LogType.SUCCESS)
                            _currentPage.value = Page.MAIN
                        } else {
                            val displayError = if (msg == "failure" || msg.isBlank()) "用户名或密码错误，请检查" else msg
                            _loginState.value = _loginState.value.copy(
                                isLoading = false,
                                error = displayError
                            )
                            addLog("登录失败: $displayError", LogType.ERROR)
                            NotificationHelper.notifyError(
                                getApplication(),
                                "学习星 登录失败",
                                displayError
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    error = e.message
                )
                addLog("登录异常: ${e.message}", LogType.ERROR)
            }
        }
    }

    fun startStudy(
        courseIds: String? = null,
        speed: Double = 1.0,
        jobs: Int = 4,
        llmConfig: String? = null
    ) {
        viewModelScope.launch {
            val login = _loginState.value
            if (!login.isLoggedIn || login.username.isNullOrBlank()) {
                addLog("请先登录", LogType.ERROR)
                return@launch
            }

            val actualCourseIds = courseIds ?: _studyState.value.courseIds
            val actualSpeed = speed.takeIf { it > 0 } ?: _studyState.value.speed
            val actualJobs = jobs.takeIf { it > 0 } ?: _studyState.value.jobs
            val actualLlmConfig = llmConfig ?: _studyState.value.llmConfig

            // 如果已经获取过课程列表，将其序列化为 JSON 传递给 Python 端，避免重复请求
            val coursesJson = if (_courses.value.isNotEmpty()) {
                try {
                    val arr = org.json.JSONArray()
                    for (c in _courses.value) {
                        val obj = org.json.JSONObject()
                        obj.put("title", c.title)
                        obj.put("courseId", c.courseId)
                        obj.put("clazzId", c.clazzId)
                        // 注意：cpi 字段在 UI 的 CourseInfo 中没有，需要从原始数据获取
                        // 这里只传递已有字段，Python 端会补充完整信息
                        arr.put(obj)
                    }
                    arr.toString()
                } catch (_: Exception) { null }
            } else null

            _studyState.value = _studyState.value.copy(
                isRunning = true,
                progress = 0f,
                courseIds = actualCourseIds,
                speed = actualSpeed,
                jobs = actualJobs,
                llmConfig = actualLlmConfig
            )
            addLog("开始刷课任务...", LogType.INFO)
            if (!actualLlmConfig.isNullOrBlank()) {
                addLog("大模型答题已配置", LogType.INFO)
            }
            if (coursesJson != null) {
                addLog("使用已获取的课程列表，避免重复请求", LogType.INFO)
            }

            PythonBridge.executeStudyTask(
                username = login.username.orEmpty(),
                password = login.password.orEmpty(),
                courseIds = actualCourseIds,
                speed = actualSpeed,
                jobs = actualJobs,
                llmConfig = actualLlmConfig,
                coursesJson = coursesJson,
                onLog = { msg -> addLog(msg) },
                onProgress = { progress ->
                    _studyState.value = _studyState.value.copy(progress = progress)
                },
                onStatusUpdate = { progress, course, chapter ->
                    _studyState.value = _studyState.value.copy(
                        progress = progress,
                        currentCourse = course,
                        currentChapter = chapter
                    )
                },
                onComplete = { success, msg ->
                    _studyState.value = _studyState.value.copy(
                        isRunning = false,
                        isCompleted = success,
                        currentCourse = "",
                        currentChapter = ""
                    )
                    if (success) {
                        addLog("刷课任务全部完成!", LogType.SUCCESS)
                    } else {
                        val errorTip = "刷课任务未完成或异常: $msg"
                        addLog(errorTip, LogType.ERROR)
                        NotificationHelper.notifyError(
                            getApplication(),
                            "学习星 刷课异常提醒",
                            errorTip
                        )
                    }
                }
            )
        }
    }

    /**
     * 获取课程列表
     */
    fun fetchCourses() {
        viewModelScope.launch {
            val login = _loginState.value
            if (!login.isLoggedIn || login.username.isNullOrBlank()) {
                addLog("请先登录", LogType.ERROR)
                return@launch
            }

            _isLoadingCourses.value = true
            addLog("正在获取课程列表...", LogType.INFO)

            PythonBridge.executeGetCourses(
                username = login.username.orEmpty(),
                password = login.password.orEmpty(),
                onLog = { msg -> addLog(msg, LogType.INFO) },
                onComplete = { resultStr ->
                    _isLoadingCourses.value = false
                    if (resultStr.isBlank()) {
                        addLog("获取课程列表失败", LogType.ERROR)
                        return@executeGetCourses
                    }
                    try {
                        val jsonArray = JSONArray(resultStr)
                        val list = mutableListOf<CourseInfo>()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            list.add(
                                CourseInfo(
                                    title = obj.optString("title", ""),
                                    courseId = obj.optString("courseId", ""),
                                    clazzId = obj.optString("clazzId", ""),
                                    cpi = obj.optString("cpi", "")
                                )
                            )
                        }
                        _courses.value = list
                        addLog("获取到 ${list.size} 门课程", LogType.SUCCESS)
                    } catch (e: Exception) {
                        addLog("解析课程列表失败: ${e.message}", LogType.ERROR)
                    }
                }
            )
        }
    }

    fun testLlmConnection(llmConfigJson: String?) {
        viewModelScope.launch {
            if (llmConfigJson.isNullOrBlank()) {
                _llmTestState.value = LlmTestState.Failure("请先配置大模型参数")
                return@launch
            }
            _llmTestState.value = LlmTestState.Testing
            PythonBridge.executeTestLlmConnection(
                llmConfigJson = llmConfigJson,
                onComplete = { resultStr ->
                    if (resultStr.isBlank()) {
                        _llmTestState.value = LlmTestState.Failure("大模型连接超时或无响应")
                        return@executeTestLlmConnection
                    }
                    try {
                        val json = org.json.JSONObject(resultStr)
                        val success = json.optBoolean("success", false)
                        val message = json.optString("message", if (success) "连通成功" else "连通失败")
                        if (success) {
                            _llmTestState.value = LlmTestState.Success(message)
                        } else {
                            _llmTestState.value = LlmTestState.Failure(message)
                        }
                    } catch (e: Exception) {
                        _llmTestState.value = LlmTestState.Failure(e.message ?: "解析测试结果失败")
                    }
                }
            )
        }
    }

    fun stopStudy() {
        _studyState.value = _studyState.value.copy(isRunning = false)
        addLog("刷课任务已停止", LogType.WARNING)
        // 通知 Python 端停止刷课
        try {
            PythonBridge.getModule("android_study").callAttr("stop_study")
        } catch (e: Exception) {
            // 忽略
        }
    }

    fun navigateTo(page: Page) {
        _currentPage.value = page
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    private var nextLogId = 1L
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var lastErrorNotifyTime = 0L

    private fun parseLogType(msg: String): LogType {
        val lower = msg.lowercase()
        return when {
            lower.contains("[error]") || lower.contains("error:") || lower.contains("异常") || lower.contains("失败") || lower.contains("报错") -> LogType.ERROR
            lower.contains("[warning]") || lower.contains("warning:") || lower.contains("警告") -> LogType.WARNING
            lower.contains("成功") || lower.contains("完成") || lower.contains("[success]") -> LogType.SUCCESS
            else -> LogType.INFO
        }
    }

    /**
     * 添加日志
     * - 保留最近 1000 条原始日志
     * - 高性能 O(1) 增量更新去重日志列表，杜绝全量扫描导致的 GC 停顿与卡顿
     * - 遇到关键报错时自动触发系统通知
     */
    private fun addLog(message: String, type: LogType? = null) {
        val actualType = type ?: parseLogType(message)
        val time = timeFormat.format(Date())
        val newEntry = LogEntry(id = nextLogId++, message = message, type = actualType, timestamp = time)

        // 报错时触发系统通知提醒用户（节流保护：至少间隔 8 秒）
        if (actualType == LogType.ERROR && _studyState.value.isRunning) {
            val now = System.currentTimeMillis()
            if (now - lastErrorNotifyTime > 8000L) {
                lastErrorNotifyTime = now
                NotificationHelper.notifyError(
                    getApplication(),
                    "学习星 任务报错提醒",
                    message
                )
            }
        }

        // 1. 增量去重更新（极速 O(1)，无循环遍历，极大减轻主线程开销与掉帧）
        val currentDedup = _deduplicatedLogs.value.toMutableList()
        if (currentDedup.isEmpty()) {
            currentDedup.add(newEntry)
        } else {
            val last = currentDedup.last()
            val rawMsg = last.message.replace(Regex(""" \(×\d+\)$"""), "")
            if (rawMsg == message && last.type == actualType) {
                val currentCount = Regex("""\(×(\d+)\)$""").find(last.message)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                val newCount = currentCount + 1
                currentDedup[currentDedup.size - 1] = last.copy(message = "$rawMsg (×$newCount)")
            } else {
                currentDedup.add(newEntry)
            }
        }
        if (currentDedup.size > 1000) {
            currentDedup.removeAt(0)
        }
        _deduplicatedLogs.value = currentDedup

        // 2. 原始日志记录
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(newEntry)
        if (currentLogs.size > 1000) {
            currentLogs.removeAt(0)
        }
        _logs.value = currentLogs
    }

    fun clearLogs() {
        _logs.value = emptyList()
        _deduplicatedLogs.value = emptyList()
    }

    data class LoginState(
        val isLoading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val username: String? = null,
        val password: String? = null,
        val error: String? = null,
        val savedUsername: String? = null,  // 从本地加载的已保存账号
        val savedPassword: String? = null   // 从本地加载的已保存密码
    )

    data class StudyState(
        val isRunning: Boolean = false,
        val isCompleted: Boolean = false,
        val progress: Float = 0f,
        val courseIds: String? = null,
        val speed: Double = 1.0,
        val jobs: Int = 4,
        val llmConfig: String? = null,
        val currentCourse: String = "",
        val currentChapter: String = ""
    )

    data class LogEntry(
        val id: Long = 0L,
        val message: String,
        val type: LogType,
        val timestamp: String = ""
    )

    enum class LogType {
        INFO, SUCCESS, WARNING, ERROR
    }

    sealed class LlmTestState {
        object Idle : LlmTestState()
        object Testing : LlmTestState()
        data class Success(val message: String) : LlmTestState()
        data class Failure(val message: String) : LlmTestState()
    }

    enum class Page {
        LOGIN, MAIN
    }
}

/**
 * 课程信息
 */
@androidx.compose.runtime.Immutable
data class CourseInfo(
    val title: String,
    val courseId: String,
    val clazzId: String,
    val cpi: String = ""
)
