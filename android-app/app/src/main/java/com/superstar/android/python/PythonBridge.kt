package com.superstar.android.python

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Python 桥接层 - 通过 Chaquopy 调用 Python 代码
 *
 * 使用 android_study 模块（Android 专用适配器），
 * 仅依赖 requests + pyaes + beautifulsoup4，不触发 multiprocessing。
 */
object PythonBridge {

    private var initialized = false
    private var filesDir: String = ""

    /**
     * 初始化 Python 环境
     */
    fun initialize(context: Context) {
        val appCtx = context.applicationContext ?: context
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(appCtx))
        }
        // 保存应用私有目录路径，用于 cookies 持久化
        filesDir = appCtx.filesDir.absolutePath
        // 设置 Python 侧的 cookies 保存目录
        try {
            val py = Python.getInstance()
            val studyModule = py.getModule("android_study")
            studyModule.callAttr("set_cookies_dir", filesDir)
        } catch (_: Throwable) {
            // 忽略非关键配置
        }
        initialized = true
    }

    fun isInitialized(): Boolean = initialized

    fun getModule(name: String) = Python.getInstance().getModule(name)

    /**
     * 从 Python 端获取缓存的日志并发送到 onLog 回调
     * 由于 Chaquopy 无法直接将 Kotlin lambda 作为回调传给 Python，
     * Python 端将所有日志缓存到全局 _log_buffer 列表中，
     * Kotlin 端在每次调用后通过此函数获取并清空缓冲区。
     */
    private fun flushPythonLogs(studyModule: com.chaquo.python.PyObject, onLog: (String) -> Unit) {
        try {
            val logs = studyModule.callAttr("get_and_clear_logs")
            if (logs != null) {
                val logList = logs.asList()
                for (i in 0 until logList.size) {
                    val msg = logList[i]?.toString() ?: continue
                    onLog(msg)
                }
            }
        } catch (e: Exception) {
            // 忽略获取日志时的错误
        }
    }

    /**
     * 从 Python 端获取实时刷课日志（轮询用）
     * 与 flushPythonLogs 不同，此函数获取的是 _study_log_buffer 中的日志，
     * 这些日志在刷课过程中实时产生，供 Kotlin 轮询刷新 UI。
     */
    private fun pollStudyLogs(studyModule: com.chaquo.python.PyObject, onLog: (String) -> Unit) {
        try {
            val logs = studyModule.callAttr("get_study_logs")
            if (logs != null) {
                val logList = logs.asList()
                for (i in 0 until logList.size) {
                    val msg = logList[i]?.toString() ?: continue
                    onLog(msg)
                }
            }
        } catch (e: Exception) {
            // 忽略获取日志时的错误
        }
    }

    /**
     * 执行登录验证
     */
    suspend fun executeLogin(
        username: String,
        password: String,
        onLog: (String) -> Unit = {},
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        try {
            val py = Python.getInstance()
            val studyModule = py.getModule("android_study")

            onLog("正在登录: $username")

            val result = studyModule.callAttr("login_only", username, password)
            val resultStr = result?.toString() ?: ""

            // 获取 Python 端缓存的日志
            flushPythonLogs(studyModule, onLog)

            val (isSuccess, responseMsg) = try {
                if (resultStr.startsWith("{")) {
                    val json = org.json.JSONObject(resultStr)
                    val success = json.optBoolean("success", false)
                    val msg = json.optString("msg", if (success) "登录成功" else "用户名或密码错误")
                    Pair(success, msg)
                } else if (resultStr == "success") {
                    Pair(true, "登录成功")
                } else {
                    val msg = if (resultStr.isBlank() || resultStr == "failure") "用户名或密码错误" else resultStr
                    Pair(false, msg)
                }
            } catch (_: Throwable) {
                val success = (resultStr == "success")
                Pair(success, if (success) "登录成功" else "用户名或密码错误")
            }

            if (isSuccess) {
                onLog("登录成功!")
                onComplete(true, "登录成功")
            } else {
                onLog("登录提示: $responseMsg")
                onComplete(false, responseMsg)
            }
        } catch (e: Exception) {
            val msg = "登录异常: ${e.message}"
            onLog(msg)
            onComplete(false, e.message ?: "未知错误")
        }
    }

    /**
     * 获取课程列表
     */
    suspend fun executeGetCourses(
        username: String,
        password: String,
        onLog: (String) -> Unit = {},
        onComplete: (String) -> Unit = { _ -> }
    ) = withContext(Dispatchers.IO) {
        try {
            val py = Python.getInstance()
            val studyModule = py.getModule("android_study")

            onLog("正在获取课程列表...")

            val result = studyModule.callAttr("get_courses", username, password)
            val resultStr = result?.toString() ?: ""

            // 获取 Python 端缓存的日志
            flushPythonLogs(studyModule, onLog)

            onComplete(resultStr)
        } catch (e: Exception) {
            val msg = "获取课程列表异常: ${e.message}"
            onLog(msg)
            onComplete("")
        }
    }

    suspend fun executeTestLlmConnection(
        llmConfigJson: String,
        onComplete: (String) -> Unit = { _ -> }
    ) = withContext(Dispatchers.IO) {
        try {
            val py = Python.getInstance()
            val studyModule = py.getModule("android_study")
            val result = studyModule.callAttr("test_llm_connection", llmConfigJson)
            val resultStr = result?.toString() ?: ""
            // 清理 Python 端缓冲区，避免残留在刷课日志中
            try {
                studyModule.callAttr("get_and_clear_logs")
            } catch (_: Exception) {}
            onComplete(resultStr)
        } catch (e: Exception) {
            val errJson = org.json.JSONObject().apply {
                put("success", false)
                put("message", e.message ?: "未知异常")
            }.toString()
            onComplete(errJson)
        }
    }

    /**
     * 执行刷课任务（异步轮询模式）
     *
     * start_study() 在 Python 后台线程中运行刷课任务并立即返回 "started"。
     * 此函数通过轮询 is_study_running() / get_study_logs() / get_study_progress()
     * 实时获取日志和进度，实现 UI 实时刷新。
     */
    suspend fun executeStudyTask(
        username: String,
        password: String,
        courseIds: String? = null,
        speed: Double = 1.0,
        jobs: Int = 4,
        llmConfig: String? = null,
        coursesJson: String? = null,
        onLog: (String) -> Unit = {},
        onProgress: (Float) -> Unit = {},
        onStatusUpdate: (progress: Float, course: String, chapter: String, videoProgress: String, quizProgress: String) -> Unit = { _, _, _, _, _ -> },
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        try {
            val py = Python.getInstance()
            val studyModule = py.getModule("android_study")

            onLog("正在初始化 Python 刷课引擎...")
            onLog("参数: 速度=${speed}x, 并发数=$jobs")
            if (!courseIds.isNullOrBlank()) {
                onLog("指定课程ID: $courseIds")
            }
            if (!llmConfig.isNullOrBlank()) {
                onLog("大模型答题已配置")
            }
            if (!coursesJson.isNullOrBlank()) {
                onLog("使用已获取的课程列表，避免重复请求")
            }

            // 启动刷课任务（异步，在 Python 后台线程中运行）
            val startResult = studyModule.callAttr(
                "start_study",
                username,
                password,
                courseIds ?: "",
                speed,
                jobs,
                llmConfig ?: "",
                coursesJson ?: ""
            )
            val startResultStr = startResult?.toString() ?: "failed"

            if (startResultStr == "already_running") {
                onLog("刷课任务已在运行中")
                onComplete(false, "任务已在运行中")
                return@withContext
            }

            if (startResultStr != "started") {
                onLog("启动刷课任务失败: $startResultStr")
                onComplete(false, startResultStr)
                return@withContext
            }

            onLog("刷课任务已启动，开始轮询日志...")

            // 启动前先微等待 500ms，确保 Python 端后台线程初始化完毕
            delay(500)

            // 高性能轮询等待刷课任务完成（单次原子性获取状态与增量日志，大幅压缩 JNI 与线程开销）
            var lastProgress = 0f
            var lastCourse = ""
            var lastChapter = ""
            var lastVideoProgress = ""
            var lastQuizProgress = ""
            var noProgressCount = 0
            val maxNoProgressCount = 1000 // 1000 * 600ms = 600s = 10 分钟无进度则超时

            while (true) {
                // 单次 JNI 调度获取完整运行状态与增量日志，杜绝 7 次重复跨语言调用与 GIL 争用
                val statusStr = studyModule.callAttr("get_study_status")?.toString() ?: "{}"
                val statusJson = org.json.JSONObject(statusStr)

                val isRunning = statusJson.optBoolean("running", false)
                val progressFloat = statusJson.optDouble("progress", 0.0).toFloat()
                val currentCourse = statusJson.optString("course", "")
                val currentChapter = statusJson.optString("chapter", "")
                val videoProgress = statusJson.optString("video_progress", "")
                val quizProgress = statusJson.optString("quiz_progress", "")
                val logsArr = statusJson.optJSONArray("logs")

                // 获取实时增量日志并回调
                if (logsArr != null && logsArr.length() > 0) {
                    for (i in 0 until logsArr.length()) {
                        val msg = logsArr.optString(i, "")
                        if (msg.isNotBlank()) onLog(msg)
                    }
                }

                // 核心防抖优化：仅在状态真正发生变化时才触发 onStatusUpdate，杜绝无效重绘
                if (progressFloat != lastProgress || currentCourse != lastCourse ||
                    currentChapter != lastChapter || videoProgress != lastVideoProgress ||
                    quizProgress != lastQuizProgress
                ) {
                    lastCourse = currentCourse
                    lastChapter = currentChapter
                    lastVideoProgress = videoProgress
                    lastQuizProgress = quizProgress
                    onStatusUpdate(progressFloat, currentCourse, currentChapter, videoProgress, quizProgress)
                }

                if (progressFloat > lastProgress) {
                    onProgress(progressFloat)
                    lastProgress = progressFloat
                    noProgressCount = 0
                } else {
                    noProgressCount++
                }

                if (!isRunning) {
                    delay(600)
                    val doubleCheck = studyModule.callAttr("get_study_status")?.toString() ?: "{}"
                    val doubleCheckJson = org.json.JSONObject(doubleCheck)
                    if (!doubleCheckJson.optBoolean("running", false)) {
                        val currResult = doubleCheckJson.optString("result", "")
                        if (currResult != "running") {
                            onLog("刷课任务已结束")
                            break
                        }
                    }
                }

                if (noProgressCount >= maxNoProgressCount) {
                    onLog("刷课任务超时（10分钟无进度更新），强制停止")
                    try {
                        studyModule.callAttr("stop_study")
                    } catch (_: Exception) {}
                    break
                }

                delay(600)
            }

            // 任务结束，获取最终结果和剩余日志
            flushPythonLogs(studyModule, onLog)

            val result = studyModule.callAttr("get_study_result")?.toString() ?: "unknown"

            if (result == "success") {
                onLog("刷课任务完成!")
                onProgress(1.0f)
                onComplete(true, "刷课任务完成")
            } else if (result == "login_failed") {
                onLog("刷课任务失败: 登录失败，请检查账号密码或网络连接")
                onComplete(false, "登录失败")
            } else if (result == "no_courses") {
                onLog("刷课任务失败: 没有可学习的课程")
                onComplete(false, "没有可学习的课程")
            } else {
                onLog("刷课任务失败: $result")
                onComplete(false, result)
            }

        } catch (e: Exception) {
            val msg = "刷课异常: ${e.message}"
            onLog(msg)
            onComplete(false, e.message ?: "未知错误")
        }
    }

    /**
     * 测试 Python 环境
     */
    fun testEnvironment(): String {
        return try {
            val py = Python.getInstance()
            val studyModule = py.getModule("android_study")
            val result = studyModule.callAttr("test_environment")
            result?.toString() ?: "unknown"
        } catch (e: Exception) {
            "Python 环境错误: ${e.message}"
        }
    }

    /**
     * 清除指定账号在 Python 侧持久化的会话凭证（退出登录时彻底清除残留，保障账号安全隔离）
     */
    fun clearUserCookies(username: String?) {
        if (username.isNullOrBlank()) return
        try {
            val py = Python.getInstance()
            val studyModule = py.getModule("android_study")
            studyModule.callAttr("clear_user_cookies", username)
        } catch (_: Throwable) {}
    }
}
