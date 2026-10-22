# -*- coding: utf-8 -*-
"""
Android 刷课适配器 - 通过 Chaquopy 在 Android 上执行刷课任务
严格遵循 https://github.com/menghuanshiguang/SuperStar_R 的实现

关键：必须在任何其他导入之前，先 mock multiprocessing 以防止 sem_open 错误。
Android 不支持 POSIX 信号量，multiprocessing 模块会触发：
  OSError: This platform lacks a functioning sem_open implementation
"""
import sys

# ===== 关键修复：在导入任何其他模块之前，先 mock multiprocessing =====
# Android 不支持 sem_open，multiprocessing 会在导入时崩溃
# 通过提前注入 mock，阻止 sem_open 被调用
import types as _types
_multiprocessing_mock = _types.ModuleType('multiprocessing')
_multiprocessing_mock.Process = None
_multiprocessing_mock.Pool = None
_multiprocessing_mock.Lock = None
_multiprocessing_mock.Queue = None
_multiprocessing_mock.set_start_method = lambda method: None
_multiprocessing_mock.current_process = lambda: None
_multiprocessing_mock.cpu_count = lambda: 1
sys.modules['multiprocessing'] = _multiprocessing_mock

# 同样 mock multiprocessing.pool
_mp_pool_mock = _types.ModuleType('multiprocessing.pool')
_mp_pool_mock.ThreadPool = None
sys.modules['multiprocessing.pool'] = _mp_pool_mock

# mock concurrent.futures 也使用 multiprocessing
_cf_mock = _types.ModuleType('concurrent.futures')
_cf_mock.ProcessPoolExecutor = None
_cf_mock.ThreadPoolExecutor = None
sys.modules['concurrent.futures'] = _cf_mock

# ===== 结束 multiprocessing mock =====

import json
import time
import hashlib
import random
import re
import threading
import queue
import os
import os.path
from typing import Optional, Any
from base64 import b64encode
from enum import Enum
from collections import deque
from dataclasses import dataclass

import requests
from bs4 import BeautifulSoup


# ==================== 全局日志系统 ====================
# 用于在 save_cookies/use_cookies 等全局函数以及 login_only/start_study 中输出日志
# 支持两种方式：
#   1. 回调模式（set_log_callback）：由 AndroidStudyEngine.__init__() 设置
#   2. 缓冲区模式（get_and_clear_logs）：由 PythonBridge.kt 在调用后获取
#   3. 实时日志缓冲区（_study_log_buffer）：由 start_study() 在后台线程中定期填充，
#      由 PythonBridge.kt 通过 get_study_logs() 轮询获取，实现实时日志刷新

_log_callback = None
_log_buffer: list = []
_study_log_buffer: list = []  # 实时日志缓冲区，供 Kotlin 轮询
_study_running = False       # 标记刷课任务是否正在运行
_study_progress = 0.0        # 当前进度 0.0~1.0
_study_result = None         # 任务结果


def set_log_callback(callback):
    """设置全局日志回调"""
    global _log_callback
    _log_callback = callback


def get_and_clear_logs() -> list:
    """获取并清空日志缓冲区，供 Kotlin 端调用"""
    global _log_buffer
    result = list(_log_buffer)
    _log_buffer.clear()
    return result


def _log(msg: str):
    """输出日志（通过全局回调或缓冲区）"""
    # 始终追加到缓冲区
    _log_buffer.append(msg)
    # 也追加到实时日志缓冲区（供 Kotlin 轮询）
    _study_log_buffer.append(msg)
    # 如果有回调，也通过回调发送
    if _log_callback:
        try:
            _log_callback(msg)
        except Exception:
            pass


def get_study_logs() -> list:
    """获取并清空实时日志缓冲区，供 Kotlin 端轮询调用"""
    global _study_log_buffer
    result = list(_study_log_buffer)
    _study_log_buffer.clear()
    return result


def is_study_running() -> bool:
    """返回刷课任务是否正在运行"""
    return _study_running


def get_study_progress() -> float:
    """返回当前刷课进度 0.0~1.0"""
    return _study_progress


def get_study_result() -> str:
    """返回刷课任务结果"""
    return _study_result or "unknown"


def stop_study():
    """停止正在运行的刷课任务"""
    global _study_running
    _study_running = False
    clear_current_video()
    clear_current_quiz()


# ==================== 视频与答题进度实时状态 ====================
_current_video_progress = ""
_current_video_name = ""
_current_quiz_progress = ""
_current_quiz_title = ""

def format_time_mmss(seconds):
    try:
        s = max(0, int(seconds)) if seconds else 0
        m = s // 60
        rem_s = s % 60
        return f"{m:02d}分{rem_s:02d}秒"
    except Exception:
        return "00分00秒"

def set_current_video(name, curr_time, tot_time):
    global _current_video_progress, _current_video_name
    _current_video_name = str(name) if name else ""
    _current_video_progress = f"{curr_time} / {tot_time}"

def clear_current_video():
    global _current_video_progress, _current_video_name
    _current_video_progress = ""
    _current_video_name = ""

def get_video_progress():
    global _current_video_progress
    return _current_video_progress

def get_video_name():
    global _current_video_name
    return _current_video_name

def set_current_quiz(title, progress_str):
    global _current_quiz_progress, _current_quiz_title
    _current_quiz_title = str(title) if title else ""
    _current_quiz_progress = str(progress_str) if progress_str else ""

def clear_current_quiz():
    global _current_quiz_progress, _current_quiz_title
    _current_quiz_progress = ""
    _current_quiz_title = ""

def get_quiz_progress():
    global _current_quiz_progress
    return _current_quiz_progress

def get_quiz_title():
    global _current_quiz_title
    return _current_quiz_title


# ==================== Cookie 持久化 ====================
# 与原始项目 api/cookies.py 一致
# 在 Android 上，cookies 保存在应用私有目录中

COOKIES_FILENAME = "chaoxing_cookies.txt"
_cookies_dir = None  # 由 set_cookies_dir() 设置


def set_cookies_dir(dir_path: str):
    """设置 cookies 文件保存目录（由 Kotlin 侧传入 filesDir）"""
    global _cookies_dir
    _cookies_dir = dir_path


def _get_cookies_path() -> str:
    """获取 cookies 文件完整路径"""
    if _cookies_dir:
        return os.path.join(_cookies_dir, COOKIES_FILENAME)
    return COOKIES_FILENAME


def save_cookies(session: requests.Session):
    """保存 session cookies 到文件，与原始项目 api/cookies.py save_cookies() 一致"""
    buffer = ""
    path = _get_cookies_path()
    try:
        with open(path, "w") as f:
            for k, v in session.cookies.items():
                buffer += f"{k}={v};"
            buffer = buffer.removesuffix(";")
            f.write(buffer)
        _log(f"[Cookie] 已保存 {len(session.cookies.items())} 个 cookies 到 {path}")
    except Exception as e:
        _log(f"[Cookie] 保存 cookies 失败: {e}")


def use_cookies() -> dict:
    """从文件加载 cookies，与原始项目 api/cookies.py use_cookies() 一致"""
    path = _get_cookies_path()
    if not os.path.exists(path):
        _log(f"[Cookie] cookies 文件不存在: {path}")
        return {}

    cookies = {}
    try:
        with open(path, "r") as f:
            buffer = f.read().strip()
            if not buffer:
                _log(f"[Cookie] cookies 文件为空: {path}")
                return {}
            for item in buffer.split(";"):
                item = item.strip()
                if "=" in item:
                    k, v = item.split("=", 1)
                    cookies[k] = v
        _log(f"[Cookie] 已加载 {len(cookies)} 个 cookies 从 {path}")
        _log(f"[Cookie] cookies 内容: {dict(list(cookies.items())[:5])}...")  # 只显示前5个
    except Exception as e:
        _log(f"[Cookie] 加载 cookies 失败: {e}")

    return cookies


class AESCipher:
    """AES 加密（使用 pyaes，与 api.cipher 兼容）"""
    def __init__(self):
        import pyaes
        self.pyaes = pyaes
        self.key = b"u2oh6Vu^HWe4_AES"
        self.iv = b"u2oh6Vu^HWe4_AES"

    @staticmethod
    def _pkcs7_padding(s: bytes, block_size=16) -> bytes:
        bs = block_size
        return s + (bs - len(s) % bs) * chr(bs - len(s) % bs).encode()

    @staticmethod
    def _split_to_blocks(byte_str: bytes, block_size=16) -> list:
        return [byte_str[i:i+block_size] for i in range(0, len(byte_str), block_size)]

    def encrypt(self, text: str) -> str:
        plaintext = text.encode("utf-8")
        padded = self._pkcs7_padding(plaintext)
        cbc = self.pyaes.AESModeOfOperationCBC(self.key, self.iv)
        ciphertext = b""
        for b in self._split_to_blocks(padded):
            ciphertext += cbc.encrypt(b)
        return b64encode(ciphertext).decode("utf8")


class StudyResult(Enum):
    """学习结果枚举，与原始项目一致"""
    SUCCESS = 0
    FORBIDDEN = 1
    ERROR = 2
    TIMEOUT = 3

    def is_success(self):
        return self == StudyResult.SUCCESS

    def is_failure(self):
        return self != StudyResult.SUCCESS


class ChapterResult(Enum):
    """章节处理结果枚举，与 main.py ChapterResult 一致"""
    SUCCESS = 0
    NOT_OPEN = 1
    ERROR = 2
    PENDING = 3


@dataclass(order=True)
class ChapterTask:
    """章节任务，与 main.py ChapterTask 一致"""
    index: int
    point: dict
    result: ChapterResult = ChapterResult.PENDING
    tries: int = 0


class RateLimiter:
    """速率限制器，与原始项目一致"""
    def __init__(self, call_interval):
        self.last_call = time.time()
        self.lock = threading.Lock()
        self.call_interval = call_interval

    def limit_rate(self, random_time=False, random_min=0.0, random_max=1.0):
        with self.lock:
            if random_time:
                wait_time = random.uniform(random_min, random_max)
                time.sleep(wait_time)
            now = time.time()
            time_elapsed = now - self.last_call
            if time_elapsed <= self.call_interval:
                time.sleep(self.call_interval - time_elapsed)
                self.last_call = time.time()
                return
            self.last_call = now
            return


class AndroidJobProcessor:
    """
    章节级并行处理器，与 main.py JobProcessor 一致
    使用 threading.Thread + queue.Queue 实现并行处理
    （Android Chaquopy 环境不支持 concurrent.futures.ThreadPoolExecutor）
    """
    def __init__(self, engine: 'AndroidStudyEngine', course: dict, tasks, speed: float, jobs: int = 4):
        self.engine = engine
        self.course = course
        self.speed = speed
        self.max_tries = 5
        self.tasks = tasks
        self.failed_tasks = []
        self.task_queue = queue.PriorityQueue()
        self.retry_queue = queue.PriorityQueue()
        self.threads = []
        self.worker_num = jobs if jobs else 4

    def run(self):
        """启动所有工作线程并等待完成，与 main.py JobProcessor.run() 一致"""
        for task in self.tasks:
            self.task_queue.put(task)

        for i in range(self.worker_num):
            thread = threading.Thread(target=self._worker_thread, daemon=True)
            self.threads.append(thread)
            thread.start()

        threading.Thread(target=self._retry_thread, daemon=True).start()

        self.task_queue.join()
        time.sleep(0.5)

    def _worker_thread(self):
        """工作线程：从队列获取章节任务并处理，与 main.py JobProcessor.worker_thread() 一致"""
        while True:
            try:
                task = self.task_queue.get(timeout=1)
            except queue.Empty:
                # 队列为空且所有任务已完成，退出线程
                if self.task_queue.unfinished_tasks == 0:
                    return
                continue
            except Exception:
                return

            # 处理章节
            self.engine.log(f"[并行] 线程处理章节: {task.point.get('title', '未知')}")
            task.result = self.engine._process_chapter_internal(self.course, task.point, self.speed)

            if task.result == ChapterResult.SUCCESS:
                self.engine.log(f"[并行] 章节完成: {task.point.get('title', '未知')}")
                self.task_queue.task_done()

            elif task.result == ChapterResult.NOT_OPEN:
                if task.tries >= self.max_tries:
                    self.engine.log(f"[并行] 章节未开放已达最大重试次数: {task.point.get('title', '未知')}")
                    self.task_queue.task_done()
                    continue
                self.retry_queue.put(task)

            elif task.result == ChapterResult.ERROR:
                task.tries += 1
                if task.tries >= self.max_tries:
                    self.engine.log(f"[并行] 章节处理失败已达最大重试次数: {task.point.get('title', '未知')}")
                    self.failed_tasks.append(task)
                    self.task_queue.task_done()
                    continue
                self.engine.log(f"[并行] 重试章节 {task.point.get('title', '未知')} ({task.tries}/{self.max_tries})")
                self.retry_queue.put(task)

            else:
                self.engine.log(f"[并行] 未知章节状态: {task.result}")
                self.failed_tasks.append(task)
                self.task_queue.task_done()

    def _retry_thread(self):
        """重试线程：将需要重试的任务重新放回队列，与 main.py JobProcessor.retry_thread() 一致"""
        try:
            while True:
                try:
                    task = self.retry_queue.get(timeout=1)
                except queue.Empty:
                    if self.task_queue.unfinished_tasks == 0 and self.retry_queue.unfinished_tasks == 0:
                        return
                    continue
                self.task_queue.put(task)
                self.task_queue.task_done()
                time.sleep(1)
        except Exception:
            pass


class AndroidStudyEngine:
    """
    Android 刷课引擎
    严格遵循原始 SuperStar_R 项目的实现逻辑
    """

    HEADERS = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
        "sec-ch-ua": '"Chromium";v="118", "Google Chrome";v="118", "Not=A?Brand";v="99"',
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": '"Windows"'
    }

    VIDEO_HEADERS = {
        "Referer": "https://mooc1.chaoxing.com/ananas/modules/video/index.html?v=2025-0725-1842",
    }

    AUDIO_HEADERS = {
        "Referer": "https://mooc1.chaoxing.com/ananas/modules/audio/index_new.html?v=2025-0725-1842",
    }

    def __init__(self, username: str, password: str,
                 on_log=None,
                 on_progress=None,
                 llm_config=None):
        self.username = username
        self.password = password
        self.on_log = on_log or (lambda msg: None)
        self.on_progress = on_progress or (lambda p: None)
        self._running = False
        self._worker_count = 4  # 默认并行工作线程数，由 run() 的 jobs 参数覆盖
        # 设置全局日志回调，使 save_cookies/use_cookies 的日志也能显示在 UI 上
        set_log_callback(self.log)
        self.session = requests.Session()
        self.session.headers.update(self.HEADERS)
        # 加载已保存的 cookies（登录状态保持）
        # 与原始项目 SessionManager.__init__() 中 self._session.cookies.update(use_cookies()) 一致
        self.log("[Cookie] 正在检查已保存的登录状态...")
        saved_cookies = use_cookies()
        if saved_cookies:
            self.session.cookies.update(saved_cookies)
            self.log(f"[Cookie] 已加载 {len(saved_cookies)} 个 cookies")
            # 检查关键 cookie
            has_uid = "_uid" in saved_cookies
            has_fid = "fid" in saved_cookies
            self.log(f"[Cookie] 关键 cookies: _uid={has_uid}, fid={has_fid}")
        else:
            self.log("[Cookie] 未找到已保存的登录状态")
        self.cipher = AESCipher()
        self.rate_limiter = RateLimiter(0.5)
        self.video_log_limiter = RateLimiter(2)
        # 初始化大模型 provider
        self.llm_provider = None
        if llm_config:
            try:
                import json
                config_dict = json.loads(llm_config) if isinstance(llm_config, str) else llm_config
                self.llm_provider = create_llm_provider(config_dict)
                if self.llm_provider:
                    self.log(f"[配置] 大模型初始化成功: {type(self.llm_provider).__name__}")
                else:
                    self.log("[配置] 大模型初始化失败: create_llm_provider 返回 None")
            except Exception as e:
                self.log(f"[配置] 大模型初始化异常: {e}")

    def log(self, msg: str):
        """输出日志"""
        self.on_log(msg)
        # 同时写入全局日志缓冲区，供 Kotlin 端通过 get_and_clear_logs() 获取
        _log(msg)

    def get_timestamp(self) -> str:
        return str(int(time.time() * 1000))

    def get_fid(self):
        return self.session.cookies.get("fid")

    def get_uid(self):
        if "_uid" in self.session.cookies:
            return self.session.cookies["_uid"]
        if "UID" in self.session.cookies:
            return self.session.cookies["UID"]
        raise ValueError("Cannot get uid !")

    # ==================== 登录 ====================

    def login(self, login_with_cookies=False) -> bool:
        """
        登录超星学习通
        与原始项目 api/base.py Chaoxing.login() 一致
        支持 login_with_cookies 参数：优先使用已保存的 cookies 登录
        """
        if login_with_cookies:
            self.log("[Cookie] ===== 尝试使用已保存的登录状态 =====")
            # 重新从文件加载 cookies（可能有更新）
            saved_cookies = use_cookies()
            if saved_cookies:
                self.session.cookies.update(saved_cookies)
                self.log(f"[Cookie] 已更新 session cookies，当前共 {len(self.session.cookies)} 个 cookies")
            else:
                self.log("[Cookie] 没有已保存的 cookies，将使用账号密码登录")
                if self.username and self.password:
                    return self.login(login_with_cookies=False)
                self.log("[Cookie] 无账号密码，登录失败")
                return False

            if self._validate_cookie_session():
                self.log("[Cookie] ===== 登录状态有效，跳过密码登录 =====")
                return True
            else:
                self.log("[Cookie] ===== 登录状态已失效，尝试使用账号密码重新登录 =====")
                if self.username and self.password:
                    return self.login(login_with_cookies=False)
                self.log("[Cookie] 无账号密码，登录失败")
                return False

        self.log(f"[登录] 正在使用账号密码登录: {self.username}")
        try:
            url = "https://passport2.chaoxing.com/fanyalogin"
            data = {
                "fid": "-1",
                "uname": self.cipher.encrypt(self.username),
                "password": self.cipher.encrypt(self.password),
                "refer": "https%3A%2F%2Fi.chaoxing.com",
                "t": True,
                "forbidotherlogin": 0,
                "validate": "",
                "doubleFactorLogin": 0,
                "independentId": 0,
            }
            self.log("[登录] 发送登录请求...")
            resp = self.session.post(url, data=data, timeout=30)
            self.log(f"[登录] 响应状态码: {resp.status_code}")
            result = resp.json()
            self.log(f"[登录] 响应内容: {result}")
            if result.get("status") == True:
                self.log("[登录] ===== 登录成功! =====")
                # 保存 cookies 到文件（登录状态保持）
                self.log(f"[Cookie] 正在保存 {len(self.session.cookies)} 个 cookies...")
                save_cookies(self.session)
                self.log("[Cookie] ===== 登录状态已持久化 =====")
                return True
            else:
                self.last_login_msg = result.get('msg2', '用户名或密码错误，请检查')
                self.log(f"[登录] 登录失败: {self.last_login_msg}")
                return False
        except Exception as e:
            self.last_login_msg = f"网络请求异常: {e}"
            self.log(f"[登录] 登录异常: {type(e).__name__}: {e}")
            return False

    def _validate_cookie_session(self) -> bool:
        """
        验证已保存的 cookies 是否仍然有效
        与原始项目 api/base.py Chaoxing._validate_cookie_session() 一致
        """
        cookies_dict = self.session.cookies.get_dict()
        self.log(f"[Cookie] 验证登录状态: 当前 session 共 {len(cookies_dict)} 个 cookies")
        self.log(f"[Cookie] cookies 中的 key: {list(cookies_dict.keys())}")

        _uid = self.session.cookies.get("_uid")
        if not _uid:
            self.log("[Cookie] 缺少 _uid cookie，登录状态无效")
            return False
        self.log(f"[Cookie] _uid = {_uid}")

        # 使用独立的 session 进行测试，避免污染主 session
        test_session = requests.Session()
        test_session.headers.update(self.HEADERS)
        test_session.cookies.update(cookies_dict)

        self.log("[Cookie] 发送验证请求到 courselistdata 接口...")
        try:
            resp = test_session.post(
                "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/courselistdata",
                data={"courseType": 1, "courseFolderId": 0, "query": "", "superstarClass": 0},
                timeout=8,
            )
        except requests.RequestException as exc:
            self.log(f"[Cookie] 验证请求失败: {exc}")
            return False

        self.log(f"[Cookie] 验证响应状态码: {resp.status_code}")
        if resp.status_code != 200:
            self.log(f"[Cookie] 验证请求返回 {resp.status_code}，登录状态无效")
            return False

        # 检查是否被重定向到登录页
        if "passport2.chaoxing.com" in resp.text or "login" in resp.text.lower():
            self.log("[Cookie] 被重定向到登录页，cookies 已失效")
            return False

        # 检查响应中是否包含课程数据
        if "courseId" in resp.text or "data" in resp.text:
            self.log("[Cookie] 响应中包含课程数据，登录状态有效")
        else:
            self.log("[Cookie] 响应中未检测到课程数据，但未重定向，视为有效")

        self.log("[Cookie] ===== 登录状态验证通过 =====")
        return True

    # ==================== 课程列表 ====================

    def get_course_list(self) -> list:
        """
        获取课程列表，使用 BeautifulSoup 解析 HTML
        与原始项目 api/base.py Chaoxing.get_course_list() 一致
        """
        self.log("正在获取课程列表...")
        try:
            # 主课程列表
            url = "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/courselistdata"
            data = {"courseType": 1, "courseFolderId": 0, "query": "", "superstarClass": 0}
            headers = {
                "Referer": "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/interaction?moocDomain=https://mooc1-1.chaoxing.com/mooc-ans",
            }
            resp = self.session.post(url, headers=headers, data=data, timeout=30)
            course_list = self._decode_course_list(resp.text)

            # 二级课程列表（文件夹）
            interaction_url = "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/interaction"
            interaction_resp = self.session.get(interaction_url, timeout=30)
            course_folder = self._decode_course_folder(interaction_resp.text)
            for folder in course_folder:
                folder_data = {
                    "courseType": 1,
                    "courseFolderId": folder["id"],
                    "query": "",
                    "superstarClass": 0,
                }
                folder_resp = self.session.post(url, data=folder_data, timeout=30)
                course_list += self._decode_course_list(folder_resp.text)

            self.log(f"获取到 {len(course_list)} 门课程")
            return course_list
        except Exception as e:
            self.log(f"获取课程列表失败: {e}")
            return []

    def _decode_course_list(self, html_text: str) -> list:
        """
        解析课程列表 HTML
        与原始项目 api/decode.py decode_course_list() 一致
        """
        soup = BeautifulSoup(html_text, "html.parser")
        raw_courses = soup.select("div.course")
        course_list = []

        for course in raw_courses:
            # 跳过未开放课程
            if course.select_one("a.not-open-tip") or course.select_one("div.not-open-tip"):
                continue

            try:
                course_detail = {
                    "id": course.attrs.get("id", ""),
                    "info": course.attrs.get("info", ""),
                    "roleid": course.attrs.get("roleid", ""),
                    "clazzId": course.select_one("input.clazzId").attrs["value"],
                    "courseId": course.select_one("input.courseId").attrs["value"],
                    "cpi": re.findall(r"cpi=(.*?)&", course.select_one("a").attrs["href"])[0],
                    "title": course.select_one("span.course-name").attrs["title"],
                    "desc": course.select_one("p.margint10").attrs["title"] if course.select_one("p.margint10") else "",
                    "teacher": course.select_one("p.color3").attrs["title"]
                }
                course_list.append(course_detail)
            except (AttributeError, IndexError) as e:
                self.log(f"  解析课程条目失败: {e}")
                continue

        return course_list

    def _decode_course_folder(self, html_text: str) -> list:
        """
        解析二级课程列表 HTML
        与原始项目 api/decode.py decode_course_folder() 一致
        """
        soup = BeautifulSoup(html_text, "html.parser")
        raw_courses = soup.select("ul.file-list>li")
        course_folder_list = []

        for course in raw_courses:
            if not course.attrs.get("fileid"):
                continue
            course_folder_detail = {
                "id": course.attrs["fileid"],
                "rename": course.select_one("input.rename-input").attrs["value"]
            }
            course_folder_list.append(course_folder_detail)

        return course_folder_list

    # ==================== 章节列表 ====================

    def get_course_point(self, courseid, clazzid, cpi) -> dict:
        """
        获取课程章节列表
        与原始项目 api/base.py Chaoxing.get_course_point() 一致
        """
        url = f"https://mooc2-ans.chaoxing.com/mooc2-ans/mycourse/studentcourse?courseid={courseid}&clazzid={clazzid}&cpi={cpi}&ut=s"
        self.log("  正在读取课程章节...")
        resp = self.session.get(url, timeout=30)
        return self._decode_course_point(resp.text)

    def _decode_course_point(self, html_text: str) -> dict:
        """
        解析章节列表 HTML
        与原始项目 api/decode.py decode_course_point() 一致
        """
        soup = BeautifulSoup(html_text, "html.parser")
        course_point = {
            "hasLocked": False,
            "points": [],
        }

        for chapter_unit in soup.find_all("div", class_="chapter_unit"):
            points = self._extract_points_from_chapter(chapter_unit)
            for point in points:
                if point.get("need_unlock", False):
                    course_point["hasLocked"] = True
            course_point["points"].extend(points)

        return course_point

    def _extract_points_from_chapter(self, chapter_unit) -> list:
        """
        从章节单元中提取章节点信息
        与原始项目 api/decode.py _extract_points_from_chapter() 一致
        """
        point_list = []
        raw_points = chapter_unit.find_all("li")

        for raw_point in raw_points:
            point = raw_point.div
            if "id" not in point.attrs:
                continue

            try:
                point_id = re.findall(r"^cur(\d{1,20})$", point.attrs["id"])[0]
            except IndexError:
                continue

            point_title = point.select_one("a.clicktitle").text.replace("\n", "").strip()

            # 提取任务数量
            job_count = 1
            need_unlock = False
            if point.select_one("input.knowledgeJobCount"):
                job_count = point.select_one("input.knowledgeJobCount").attrs["value"]
            elif point.select_one("span.bntHoverTips") and "解锁" in point.select_one("span.bntHoverTips").text:
                need_unlock = True

            # 判断是否已完成
            is_finished = False
            if point.select_one("span.bntHoverTips") and "已完成" in point.select_one("span.bntHoverTips").text:
                is_finished = True

            point_detail = {
                "id": point_id,
                "title": point_title,
                "jobCount": job_count,
                "has_finished": is_finished,
                "need_unlock": need_unlock
            }
            point_list.append(point_detail)

        return point_list

    # ==================== 任务点列表 ====================

    def get_job_list(self, course: dict, point: dict) -> tuple:
        """
        获取章节的所有任务点
        与原始项目 api/base.py Chaoxing.get_job_list() 一致
        """
        self.rate_limiter.limit_rate()
        job_list = []
        job_info = {}

        cards_params = {
            "clazzid": course["clazzId"],
            "courseid": course["courseId"],
            "knowledgeid": point["id"],
            "ut": "s",
            "cpi": course["cpi"],
            "v": "2025-0424-1038-3",
            "mooc2": 1
        }

        for possible_num in "0123456":
            cards_params.update({"num": possible_num})
            resp = self.session.get(
                "https://mooc1.chaoxing.com/mooc-ans/knowledge/cards",
                params=cards_params,
                timeout=30
            )
            if resp.status_code != 200:
                self.log(f"    获取任务点失败: {resp.status_code}")
                return [], {}

            _job_list, _job_info = self._decode_course_card(resp.text)
            if _job_info.get("notOpen", False):
                self.log("    该章节未开放")
                return [], _job_info

            job_list += _job_list
            job_info.update(_job_info)

        if not job_list:
            self.study_emptypage(course, point)

        return job_list, job_info

    def _decode_course_card(self, html_text: str) -> tuple:
        """
        解析任务点列表 HTML
        与原始项目 api/decode.py decode_course_card() 一致
        """
        # 检查章节是否未开放
        if "章节未开放" in html_text:
            return [], {"notOpen": True}

        # 提取 mArg 参数
        temp = re.findall(r"mArg=\{(.*?)\};", html_text.replace(" ", ""))
        if not temp:
            return [], {}

        # 解析 JSON 数据
        cards_data = json.loads("{" + temp[0] + "}")
        if not cards_data:
            return [], {}

        # 提取任务信息
        job_info = self._extract_job_info(cards_data)

        # 处理所有附件任务
        cards = cards_data.get("attachments", [])
        job_list = self._process_attachment_cards(cards)

        return job_list, job_info

    def _extract_job_info(self, cards_data: dict) -> dict:
        """
        从卡片数据中提取任务基本信息
        与原始项目 api/decode.py _extract_job_info() 一致
        """
        defaults = cards_data.get("defaults", {})
        if not defaults:
            return {}

        return {
            "ktoken": defaults.get("ktoken", ""),
            "mtEnc": defaults.get("mtEnc", ""),
            "reportTimeInterval": defaults.get("reportTimeInterval", 60),
            "defenc": defaults.get("defenc", ""),
            "cardid": defaults.get("cardid", ""),
            "cpi": defaults.get("cpi", ""),
            "qnenc": defaults.get("qnenc", ""),
            "knowledgeid": defaults.get("knowledgeid", "")
        }

    def _process_attachment_cards(self, cards: list) -> list:
        """
        处理所有附件任务卡片
        与原始项目 api/decode.py _process_attachment_cards() 一致
        """
        job_list = []

        for card in cards:
            # 跳过已通过的任务
            if card.get("isPassed", False):
                continue

            # 处理无 job 字段的特殊任务（阅读任务）
            if card.get("job") is None:
                read_job = self._process_read_task(card)
                if read_job:
                    job_list.append(read_job)
                continue

            # 清理 otherInfo 字段
            if "otherInfo" in card:
                card["otherInfo"] = card["otherInfo"].split("&")[0]

            # 多维度判断任务类型
            card_type = card.get("type", "").lower()
            property_data = card.get("property", {})
            prop_type = property_data.get("type", "").lower()
            resource_type = property_data.get("resourceType", "").lower()

            is_live = (
                "live" in card_type
                or "live" in prop_type
                or "live" in resource_type
                or "livestream" in card_type
                or property_data.get("liveId") is not None
                or property_data.get("streamName") is not None
                or property_data.get("vdoid") is not None
            )

            if is_live:
                live_job = self._process_live_task(card)
                if live_job:
                    job_list.append(live_job)
            elif card_type == "video":
                video_job = self._process_video_task(card)
                if video_job:
                    job_list.append(video_job)
            elif card_type == "document":
                doc_job = self._process_document_task(card)
                if doc_job:
                    job_list.append(doc_job)
            elif card_type == "workid":
                work_job = self._process_work_task(card)
                if work_job:
                    job_list.append(work_job)
            else:
                self.log(f"    未知任务类型: {card_type}")

        return job_list

    def _process_video_task(self, card: dict) -> Optional[dict]:
        """处理视频类型任务"""
        try:
            return {
                "type": "video",
                "jobid": card.get("jobid", ""),
                "name": card.get("property", {}).get("name", ""),
                "otherinfo": card.get("otherInfo", ""),
                "mid": card["mid"],
                "objectid": card.get("objectId", ""),
                "aid": card.get("aid", ""),
                "playTime": card.get("playTime", 0),
                "rt": card.get("property", {}).get("rt", ""),
                "attDuration": card.get("attDuration", ""),
                "attDurationEnc": card.get("attDurationEnc", ""),
                "videoFaceCaptureEnc": card.get("videoFaceCaptureEnc", ""),
            }
        except KeyError:
            self.log("    出现转码失败视频，已跳过...")
            return None

    def _process_document_task(self, card: dict) -> Optional[dict]:
        """处理文档类型任务"""
        return {
            "type": "document",
            "jobid": card.get("jobid", ""),
            "otherinfo": card.get("otherInfo", ""),
            "jtoken": card.get("jtoken", ""),
            "mid": card.get("mid", ""),
            "enc": card.get("enc", ""),
            "aid": card.get("aid", ""),
            "objectid": card.get("property", {}).get("objectid", "")
        }

    def _process_work_task(self, card: dict) -> Optional[dict]:
        """处理作业类型任务"""
        return {
            "type": "workid",
            "jobid": card.get("jobid", ""),
            "otherinfo": card.get("otherInfo", ""),
            "mid": card.get("mid", ""),
            "enc": card.get("enc", ""),
            "aid": card.get("aid", "")
        }

    def _process_read_task(self, card: dict) -> Optional[dict]:
        """处理阅读类型任务"""
        if not (card.get("type") == "read" and not card.get("property", {}).get("read", False)):
            return None

        return {
            "title": card.get("property", {}).get("title", ""),
            "type": "read",
            "id": card.get("property", {}).get("id", ""),
            "jobid": card.get("jobid", ""),
            "jtoken": card.get("jtoken", ""),
            "mid": card.get("mid", ""),
            "otherinfo": card.get("otherInfo", ""),
            "enc": card.get("enc", ""),
            "aid": card.get("aid", "")
        }

    def _process_live_task(self, card: dict) -> Optional[dict]:
        """处理直播类型任务"""
        try:
            property_data = card.get("property", {})
            return {
                "type": "live",
                "jobid": card.get("jobid", str(card.get("id", ""))),
                "name": property_data.get("title", property_data.get("name", "未知直播")),
                "otherinfo": card.get("otherInfo", ""),
                "property": property_data,
                "mid": card.get("mid", ""),
                "objectid": card.get("objectId", ""),
                "aid": card.get("aid", ""),
                "liveId": property_data.get("liveId"),
                "streamName": property_data.get("streamName")
            }
        except Exception as e:
            self.log(f"    解析直播任务失败: {e}")
            return None

    # ==================== 视频学习 ====================

    def get_enc(self, clazzId, jobid, objectId, playingTime, duration, userid):
        """生成视频进度上报的 enc 参数，与原始项目一致"""
        return hashlib.md5(
            f"[{clazzId}][{userid}][{jobid}][{objectId}][{playingTime * 1000}][d_yHJ!$pdA~5][{duration * 1000}][0_{duration}]".encode()
        ).hexdigest()

    def video_progress_log(self, _course, _job, _job_info, _dtoken, _duration, _playingTime, _type="Video", headers=None):
        """
        上报视频进度
        与原始项目 api/base.py Chaoxing.video_progress_log() 一致
        """
        if headers is None:
            self.log("[视频] headers 为空，使用默认 VIDEO_HEADERS")
            headers = self.VIDEO_HEADERS if _type == "Video" else self.AUDIO_HEADERS

        self.video_log_limiter.limit_rate(random_time=True, random_max=2)

        if "courseId" in _job.get("otherinfo", ""):
            self.log(f"[视频] otherinfo 中包含 courseId: {_job['otherinfo']}")
            raise RuntimeError("this is not possible")

        enc = self.get_enc(_course["clazzId"], _job["jobid"], _job["objectid"], _playingTime, _duration, self.get_uid())
        params = {
            "clazzId": _course["clazzId"],
            "playingTime": _playingTime,
            "duration": _duration,
            "clipTime": f"0_{_duration}",
            "objectId": _job["objectid"],
            "otherInfo": _job["otherinfo"],
            "courseId": _course["courseId"],
            "jobid": _job["jobid"],
            "userid": self.get_uid(),
            "isdrag": "3",
            "view": "pc",
            "enc": enc,
            "dtype": _type
        }

        _url = (
            f"https://mooc1.chaoxing.com/mooc-ans/multimedia/log/a/"
            f"{_course['cpi']}/"
            f"{_dtoken}"
        )

        face_capture_enc = _job.get("videoFaceCaptureEnc", "")
        att_duration = _job.get("attDuration", "")
        att_duration_enc = _job.get("attDurationEnc", "")

        if face_capture_enc:
            params["videoFaceCaptureEnc"] = face_capture_enc
        if att_duration:
            params["attDuration"] = att_duration
        if att_duration_enc:
            params["attDurationEnc"] = att_duration_enc

        rt = _job.get("rt", "")
        if not rt:
            rt_search = re.search(r"-rt_([1d])", _job.get("otherinfo", ""))
            if rt_search:
                rt_char = rt_search.group(1)
                rt = "0.9" if rt_char == "d" else "1"
                self.log(f"[视频] 从 otherinfo 获取 rt: {rt}")

        if rt:
            self.log(f"[视频] 使用 rt: {rt}")
            params.update({"rt": rt, "_t": self.get_timestamp()})
            resp = self.session.get(_url, params=params, headers=headers)
        else:
            self.log("[视频] 未能获取 rt，尝试 0.9 和 1")
            for rt_val in [0.9, 1]:
                params.update({"rt": rt_val, "_t": self.get_timestamp()})
                resp = self.session.get(_url, params=params, headers=headers)
                if resp.status_code == 200:
                    self.log(f"[视频] rt={rt_val} 成功")
                    try:
                        return resp.json()["isPassed"], 200
                    except (ValueError, KeyError):
                        return False, 200
                elif resp.status_code == 403:
                    self.log("[视频] 出现403报错，正常尝试切换rt")
                else:
                    self.log(f"[视频] 未知错误 jobid={_job.get('jobid')}, status_code={resp.status_code}")
                    break

        if resp.status_code == 200:
            try:
                return resp.json()["isPassed"], 200
            except (ValueError, KeyError):
                return False, 200
        elif resp.status_code == 403:
            self.log(f"[视频] 视频进度上报返回403, jobid={_job.get('jobid')}")
            self.log("[视频] 出现403报错，尝试修复无效，正在跳过当前任务点")
            return False, 403

        self.log(f"[视频] 未知错误: {resp.status_code}")
        return False, resp.status_code

    # ==================== 视频学习 ====================

    def study_video(self, _course, _job, _job_info, _speed=1.0, _type="Video"):
        """
        学习视频/音频任务
        与原始项目 api/base.py Chaoxing.study_video() 一致
        """
        headers = self.VIDEO_HEADERS if _type == "Video" else self.AUDIO_HEADERS
        _info_url = f"https://mooc1.chaoxing.com/ananas/status/{_job['objectid']}?k={self.get_fid()}&flag=normal"
        _video_info = self.session.get(_info_url, headers=headers).json()

        if _video_info.get("status") != "success":
            self.log(f"[视频] 未知状态: {_video_info.get('status')}")
            return StudyResult.ERROR

        _dtoken = _video_info["dtoken"]

        duration = int(_video_info["duration"])
        play_time = int(_job.get("playTime", 0)) // 1000
        video_name = _job.get('name', '视频任务')
        last_log_time = 0
        last_iter = time.time()
        wait_time = int(random.uniform(30, 90))

        set_current_video(video_name, format_time_mmss(play_time), format_time_mmss(duration))
        self.log(f"[视频] 开始任务: {video_name}, 总时长: {format_time_mmss(duration)}, 已进行: {format_time_mmss(play_time)}")

        forbidden_retry = 0
        max_forbidden_retry = 2

        # 先发送两次进度（开始和结束），检查是否已完成
        passed, state = self.video_progress_log(_course, _job, _job_info, _dtoken, duration, play_time, _type, headers=headers)
        passed, state = self.video_progress_log(_course, _job, _job_info, _dtoken, duration, duration, _type, headers=headers)

        if passed:
            set_current_video(video_name, format_time_mmss(duration), format_time_mmss(duration))
            self.log(f"[视频] 任务瞬间完成: {video_name}")
            return StudyResult.SUCCESS

        while not passed:
            # 当播放时间达到上报间隔或等于总时长时上报进度
            if play_time - last_log_time >= wait_time or play_time >= duration:
                passed, state = self.video_progress_log(_course, _job, _job_info, _dtoken, duration,
                                                        int(play_time), _type, headers=headers)

                if state == 403:
                    if forbidden_retry >= max_forbidden_retry:
                        self.log("[视频] 403重试失败，跳过当前任务")
                        return StudyResult.FORBIDDEN
                    forbidden_retry += 1
                    self.log(f"[视频] 出现403报错，正在尝试刷新会话状态 (第{forbidden_retry}次)")
                    time.sleep(random.uniform(2, 4))
                    try:
                        refreshed = self.session.get(_info_url, headers=headers).json()
                        if refreshed.get("status") == "success":
                            _dtoken = refreshed.get("dtoken", _dtoken)
                            duration = int(refreshed.get("duration", duration))
                            play_time = int(refreshed.get("playTime", play_time))
                            set_current_video(video_name, format_time_mmss(play_time), format_time_mmss(duration))
                            self.log(f"[视频] 刷新token成功: dtoken={_dtoken}")
                            continue
                    except Exception:
                        self.log("[视频] 刷新视频状态失败")
                        continue

                elif not passed and state != 200:
                    return StudyResult.ERROR

                wait_time = int(random.uniform(30, 90))
                last_log_time = play_time

            dt = (time.time() - last_iter) * _speed
            last_iter = time.time()
            play_time = min(duration, play_time + dt)
            set_current_video(video_name, format_time_mmss(play_time), format_time_mmss(duration))

            time.sleep(1)

        set_current_video(video_name, format_time_mmss(duration), format_time_mmss(duration))
        self.log(f"[视频] 任务完成: {video_name}")
        return StudyResult.SUCCESS

    # ==================== 文档学习 ====================

    def study_document(self, _course, _job) -> StudyResult:
        """
        学习文档任务
        与原始项目 api/base.py Chaoxing.study_document() 一致
        """
        _url = (f"https://mooc1.chaoxing.com/ananas/job/document?"
                f"jobid={_job['jobid']}&"
                f"knowledgeid={re.findall(r'nodeId_(.*?)-', _job['otherinfo'])[0]}&"
                f"courseid={_course['courseId']}&"
                f"clazzid={_course['clazzId']}&"
                f"jtoken={_job['jtoken']}&"
                f"_dc={self.get_timestamp()}")
        _resp = self.session.get(_url)
        if _resp.status_code != 200:
            self.log(f"[文档] 文档学习失败: {_resp.status_code}")
            return StudyResult.ERROR
        else:
            self.log("[文档] 文档学习成功")
            return StudyResult.SUCCESS

    # ==================== 作业答题 ====================

    def study_work(self, _course, _job, _job_info) -> StudyResult:
        """
        作业答题任务
        与原始项目 api/base.py Chaoxing.study_work() 一致
        """
        self.log(f"[作业] 开始答题: jobid={_job['jobid']}")
        _ORIGIN_HTML_CONTENT = ""  # 用于配合输出网页源码, 帮助修复#391错误

        def random_answer(options: str) -> str:
            """随机选择答案"""
            answer = ""
            if not options:
                return answer

            if q["type"] == "multiple":
                self.log(f"[作业] 当前选项列表[cut前] -> {options}")
                _op_list = multi_cut(options)
                self.log(f"[作业] 当前选项列表[cut后] -> {_op_list}")
                if not _op_list:
                    self.log("[作业] 选项为空，未能正确提取题目选项信息! 请反馈并提供以上信息")
                    return answer

                available_options = len(_op_list)
                select_count = 0

                if available_options <= 1:
                    select_count = available_options
                else:
                    max_possible = min(4, available_options)
                    min_possible = min(2, available_options)
                    weights_map = {
                        2: [1.0],
                        3: [0.3, 0.7],
                        4: [0.1, 0.5, 0.4],
                        5: [0.1, 0.4, 0.3, 0.2],
                    }
                    weights = weights_map.get(max_possible, [0.3, 0.4, 0.3])
                    possible_counts = list(range(min_possible, max_possible + 1))
                    weights = weights[:len(possible_counts)]
                    weights_sum = sum(weights)
                    if weights_sum > 0:
                        weights = [w / weights_sum for w in weights]
                    select_count = random.choices(possible_counts, weights=weights, k=1)[0]

                selected_options = random.sample(_op_list, select_count) if select_count > 0 else []
                for option in selected_options:
                    answer += option[:1]
                answer = "".join(sorted(answer))
            elif q["type"] == "single":
                answer = random.choice(options.split("\n"))[:1]
            elif q["type"] == "judgement":
                answer = "true" if random.choice([True, False]) else "false"
            self.log(f"[作业] 随机选择 -> {answer}")
            return answer

        def multi_cut(answer: str):
            """切割多选题答案，与原始项目 api/answer_check.py cut() 一致"""
            if answer is None:
                return None
            answer = str(answer)
            cut_char = [
                "\n", ",", "，", "|", "\r", "\t", "#", "*", "-", "_",
                "+", "@", "~", "/", "\\", ".", "&", " ", "、",
            ]
            for char in cut_char:
                if char not in answer:
                    continue
                result = [r.strip() for r in answer.split(char) if r.strip()]
                if result:
                    return result
            stripped = answer.strip()
            if stripped:
                return [stripped]
            self.log(f"[作业] 未能从网页中提取题目信息, 以下为相关信息：\n\t{answer}\n\n{_ORIGIN_HTML_CONTENT}\n")
            self.log("[作业] 未能正确提取题目选项信息! 请反馈并提供以上信息")
            return None

        def clean_res(res):
            """清理答案结果，与原始项目一致"""
            cleaned_res = []
            if isinstance(res, str):
                res = [res]
            for c in res:
                cleaned = re.sub(r'^[A-Za-z]|[.,!?;:，。！？；：]', '', c) if len(c) > 1 else c
                cleaned_res.append(cleaned.strip())
            return cleaned_res

        def is_subsequence(a, o):
            """检查 a 是否是 o 的子序列"""
            iter_o = iter(o)
            return all(c in iter_o for c in a)

        # 获取作业页面
        _url = "https://mooc1.chaoxing.com/mooc-ans/api/work"
        try:
            resp = self.session.get(
                _url,
                params={
                    "api": "1",
                    "workId": _job["jobid"].replace("work-", ""),
                    "jobid": _job["jobid"],
                    "originJobId": _job["jobid"],
                    "needRedirect": "true",
                    "skipHeader": "true",
                    "knowledgeid": str(_job_info.get("knowledgeid", "")),
                    "ktoken": _job_info.get("ktoken", ""),
                    "cpi": _job_info.get("cpi", ""),
                    "ut": "s",
                    "clazzId": _course["clazzId"],
                    "type": "",
                    "enc": _job.get("enc", ""),
                    "mooc2": "1",
                    "courseid": _course["courseId"],
                },
                timeout=30
            )
        except Exception as e:
            self.log(f"[作业] 请求失败: {e}")
            return StudyResult.ERROR

        if '教师未创建完成该测验' in resp.text:
            self.log("[作业] 教师未创建完成该测验，跳过")
            return StudyResult.SUCCESS

        _ORIGIN_HTML_CONTENT = resp.text  # 用于配合输出网页源码, 帮助修复#391错误

        # 解析题目
        questions = self._decode_questions_info(resp.text)
        if not questions.get("questions"):
            self.log("[作业] 未获取到题目，跳过")
            return StudyResult.SUCCESS

        # 搜题/答题
        total_questions = len(questions["questions"])
        found_answers = 0
        quiz_name = _job.get('name') or "章节测验"

        for idx, q in enumerate(questions["questions"], 1):
            set_current_quiz(quiz_name, f"第 {idx}/{total_questions} 题")
            self.log(f"[作业] 当前题目 (第 {idx}/{total_questions} 题): {q.get('title', '未知')[:50]}...")
            answer = ""
            res = None

            # 如果有 llm_provider，使用大模型答题
            if hasattr(self, 'llm_provider') and self.llm_provider:
                try:
                    res = self.llm_provider.query(q)
                    if res:
                        self.log(f"[作业] 大模型返回答案: {res}")
                except Exception as e:
                    self.log(f"[作业] 大模型查询失败: {e}")

            if not res:
                # 随机答题
                answer = random_answer(q.get("options", ""))
                q[f'answerSource{q["id"]}'] = "random"
            else:
                # 根据响应结果选择答案
                if q["type"] == "multiple":
                    options_list = multi_cut(q.get("options", ""))
                    res_list = multi_cut(res)
                    if res_list is not None and options_list is not None:
                        for _a in clean_res(res_list):
                            for o in options_list:
                                if is_subsequence(_a, o):
                                    answer += o[:1]
                                    break
                        answer = "".join(sorted(answer))
                elif q["type"] == "single":
                    options_list = multi_cut(q.get("options", ""))
                    if options_list is not None:
                        t_res = clean_res(res)
                        for o in options_list:
                            if is_subsequence(t_res[0], o):
                                answer = o[:1]
                                break
                elif q["type"] == "judgement":
                    answer = "true" if random.choice([True, False]) else "false"
                elif q["type"] == "completion":
                    answer = res if isinstance(res, str) else "".join(res) if isinstance(res, list) else str(res)
                else:
                    answer = res

                if not answer:
                    self.log(f"[作业] 找到答案但未能匹配 -> {res}，随机选择答案")
                    answer = random_answer(q.get("options", ""))
                    q[f'answerSource{q["id"]}'] = "random"
                else:
                    self.log(f"[作业] 成功获取到答案：{answer}")
                    q[f'answerSource{q["id"]}'] = "cover"
                    found_answers += 1

            # 填充答案
            q["answerField"][f'answer{q["id"]}'] = answer
            self.log(f'[作业] {q.get("title", "未知")[:30]} 填写答案为 {answer}')

        cover_rate = (found_answers / total_questions) * 100
        self.log(f"[作业] 章节检测题库覆盖率：{cover_rate:.0f}%")

        # 提交模式：pyFlag=="1" 保存但不提交，pyFlag=="" 直接提交
        # 与原始项目 api/base.py study_work() 动态逻辑一致
        # 当覆盖率 >= COVER_RATE (默认60%) 时提交，否则保存
        COVER_RATE = 0.6  # 与原始项目默认值一致
        if cover_rate >= COVER_RATE * 100:
            questions["pyFlag"] = ""
            self.log(f"[作业] 题库覆盖率 {cover_rate:.0f}% >= {COVER_RATE*100:.0f}%，将直接提交答案")
        else:
            questions["pyFlag"] = "1"
            self.log(f"[作业] 题库覆盖率 {cover_rate:.0f}% < {COVER_RATE*100:.0f}%，仅保存不提交")

        # 组建提交表单
        if questions["pyFlag"] == "1":
            for q in questions["questions"]:
                questions.update({
                    f'answer{q["id"]}':
                        q["answerField"][f'answer{q["id"]}'] if q[f'answerSource{q["id"]}'] == "cover" else '',
                    f'answertype{q["id"]}': q["answerField"][f'answertype{q["id"]}'],
                })
        else:
            for q in questions["questions"]:
                questions.update({
                    f'answer{q["id"]}': q["answerField"][f'answer{q["id"]}'],
                    f'answertype{q["id"]}': q["answerField"][f'answertype{q["id"]}'],
                })

        del questions["questions"]

        # 提交答案
        try:
            res = self.session.post(
                "https://mooc1.chaoxing.com/mooc-ans/work/addStudentWorkNew",
                data=questions,
                headers={
                    "Host": "mooc1.chaoxing.com",
                    "sec-ch-ua-platform": '"Windows"',
                    "X-Requested-With": "XMLHttpRequest",
                    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36 Edg/129.0.0.0",
                    "Accept": "application/json, text/javascript, */*; q=0.01",
                    "sec-ch-ua": '"Microsoft Edge";v="129", "Not=A?Brand";v="8", "Chromium";v="129"',
                    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
                    "sec-ch-ua-mobile": "?0",
                    "Origin": "https://mooc1.chaoxing.com",
                    "Sec-Fetch-Site": "same-origin",
                    "Sec-Fetch-Mode": "cors",
                    "Sec-Fetch-Dest": "empty",
                    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6,ja;q=0.5",
                },
                timeout=30
            )
            if res.status_code == 200:
                res_json = res.json()
                if res_json.get("status"):
                    self.log(f'[作业] {"提交" if questions["pyFlag"] == "" else "保存"}答题成功 -> {res_json.get("msg", "")}')
                else:
                    self.log(f'[作业] {"提交" if questions["pyFlag"] == "" else "保存"}答题失败 -> {res_json.get("msg", "")}')
                    return StudyResult.ERROR
            else:
                self.log(f'[作业] {"提交" if questions["pyFlag"] == "" else "保存"}答题失败 -> {res.text[:200]}')
                return StudyResult.ERROR
        except Exception as e:
            self.log(f"[作业] 提交异常: {e}")
            return StudyResult.ERROR

        set_current_quiz(quiz_name, f"第 {total_questions}/{total_questions} 题 (答题完毕)")
        return StudyResult.SUCCESS

    def _decode_questions_info(self, html_content: str) -> dict:
        """
        解析作业题目信息
        与原始项目 api/decode.py decode_questions_info() 一致
        支持超星学习通字体加密解码
        """
        from bs4.element import NavigableString

        soup = BeautifulSoup(html_content, "html.parser")
        form_data = self._extract_form_data(soup)
        if not form_data:
            return {}

        # 检查是否存在字体加密，与原始项目 api/decode.py decode_questions_info() 一致
        has_font_encryption = bool(soup.find("style", id="cxSecretStyle"))
        font_decoder = None

        if has_font_encryption:
            try:
                from font_decoder import FontDecoder
                font_decoder = FontDecoder(html_content)
                self.log("[解析] 检测到字体加密，已初始化字体解码器")
            except Exception as e:
                self.log(f"[解析] 初始化字体解码器失败: {e}")
        else:
            self.log("[解析] 未检测到字体加密")

        questions = []
        # 与原始项目 api/decode.py decode_questions_info() 一致，在 <form> 内查找 singleQuesId
        form_tag = soup.find("form")
        if not form_tag:
            self.log("[解析] 未找到 form 标签")
            return {}
        for div_tag in form_tag.find_all("div", class_="singleQuesId"):
            try:
                q = self._process_question(div_tag, font_decoder)
                if q:
                    questions.append(q)
            except Exception as e:
                self.log(f"[解析] 处理题目失败: {e}")
                continue

        form_data["questions"] = questions
        # 添加 answerwqbid，与原始项目一致
        form_data["answerwqbid"] = ",".join([q["id"] for q in questions]) + ","
        return form_data

    def _extract_form_data(self, soup: BeautifulSoup) -> dict:
        """提取表单数据，与原始项目 api/decode.py _extract_form_data 一致"""
        form_data = {}
        form_tag = soup.find("form")
        if not form_tag:
            return form_data

        # 提取所有非答案字段的 input，与原始项目一致
        for input_tag in form_tag.find_all("input"):
            if "name" not in input_tag.attrs or "answer" in input_tag.attrs["name"]:
                continue
            form_data[input_tag.attrs["name"]] = input_tag.attrs.get("value", "")
        return form_data

    def _process_question(self, div_tag, font_decoder=None) -> dict:
        """处理单个题目，与原始项目 api/decode.py _process_question 一致"""
        from bs4.element import NavigableString

        # 提取问题ID和题目类型，与原始项目一致
        question_id = div_tag.attrs.get("data", "")
        # 从嵌套的 TiMu div 中获取 type code，与原始项目一致
        timu_div = div_tag.find("div", class_="TiMu")
        q_type_code = timu_div.attrs.get("data", "") if timu_div else ""
        q_type = self._get_question_type(q_type_code)

        # 提取标题div和选项列表，与原始项目一致
        title_div = div_tag.find("div", class_="Zy_TItle")
        options_list = div_tag.find("ul").find_all("li") if div_tag.find("ul") else []

        # 解析题目和选项（传入 font_decoder 以支持字体加密解码）
        q_title = self._extract_title(title_div, font_decoder)
        q_options = []
        for li in options_list:
            q_options.append(self._extract_choices(li, font_decoder))
        q_options.sort()
        q_options_str = '\n'.join(q_options)

        return {
            "id": question_id,
            "title": q_title,
            "options": q_options_str,
            "type": q_type,
            "answerField": {
                f"answer{question_id}": "",
                f"answertype{question_id}": q_type_code,
            },
        }

    def _get_question_type(self, type_code: str) -> str:
        """获取题目类型，与原始项目一致"""
        type_map = {
            "1": "single",
            "2": "multiple",
            "3": "judgement",
            "4": "completion",
            "5": "shortanswer",
            "6": "comprehensive",
        }
        return type_map.get(type_code, "unknown")

    def _extract_title(self, element, font_decoder=None) -> str:
        """提取题目标题，与原始项目 api/decode.py _extract_title 一致"""
        if not element:
            return ""

        from bs4.element import NavigableString

        # 使用 descendants 遍历所有后代节点，与原始项目一致
        content = []
        for item in element.descendants:
            if isinstance(item, NavigableString):
                content.append(item.string or "")
            elif item.name == "img":
                img_url = item.get("src", "")
                content.append(f'<img src="{img_url}">')

        raw_content = "".join(content)
        cleaned_content = raw_content.replace("\r", "").replace("\t", "").replace("\n", "")

        # 如果有字体解码器，进行解码，与原始项目一致
        if font_decoder:
            try:
                cleaned_content = font_decoder.decode(cleaned_content)
            except Exception as e:
                self.log(f"[解析] 字体解码失败: {e}")

        return cleaned_content

    def _extract_choices(self, element, font_decoder=None) -> str:
        """提取题目选项，与原始项目 api/decode.py _extract_choices 一致"""
        if not element:
            return ""

        # 提取 aria-label 属性值作为选项，与原始项目一致
        choice = element.get("aria-label") or element.get_text()
        if not choice:
            return ""

        cleaned_content = re.sub(r"[\r\t\n]", "", choice)

        # 如果有字体解码器，进行解码，与原始项目一致
        if font_decoder:
            try:
                cleaned_content = font_decoder.decode(cleaned_content)
            except Exception as e:
                self.log(f"[解析] 选项字体解码失败: {e}")

        cleaned_content = cleaned_content.strip()
        if cleaned_content.endswith("选择"):
            cleaned_content = cleaned_content[:-2].rstrip()

        return cleaned_content

    # ==================== 阅读学习 ====================

    def study_read(self, _course, _job, _job_info) -> StudyResult:
        """
        阅读任务学习，仅完成任务点，并不增长时长
        与原始项目 api/base.py Chaoxing.study_read() 一致
        """
        _resp = self.session.get(
            url="https://mooc1.chaoxing.com/ananas/job/readv2",
            params={
                "jobid": _job["jobid"],
                "knowledgeid": _job_info.get("knowledgeid", ""),
                "jtoken": _job.get("jtoken", ""),
                "courseid": _course["courseId"],
                "clazzid": _course["clazzId"],
            },
        )
        if _resp.status_code != 200:
            self.log(f"[阅读] 阅读任务学习失败 -> [{_resp.status_code}]{_resp.text[:100]}")
            return StudyResult.ERROR
        else:
            _resp_json = _resp.json()
            self.log(f"[阅读] 阅读任务学习 -> {_resp_json.get('msg', '')}")
            return StudyResult.SUCCESS

    # ==================== 空页面学习 ====================

    def study_emptypage(self, _course, point):
        """
        空页面任务学习
        与原始项目 api/base.py Chaoxing.study_emptypage() 一致
        """
        _resp = self.session.get(
            url="https://mooc1.chaoxing.com/mooc-ans/mycourse/studentstudyAjax",
            params={
                "courseId": _course["courseId"],
                "clazzid": _course["clazzId"],
                "chapterId": point["id"],
                "cpi": _course["cpi"],
                "verificationcode": "",
                "mooc2": 1,
                "microTopicId": 0,
                "editorPreview": 0,
            },
        )
        if _resp.status_code != 200:
            self.log(f"[空页面] 空页面任务失败 -> [{_resp.status_code}]{point['title']}")
            return StudyResult.ERROR
        else:
            self.log(f"[空页面] 空页面任务完成 -> {point['title']}")
            return StudyResult.SUCCESS

    # ==================== 任务处理 ====================

    def process_job(self, course, job, job_info, speed):
        """
        处理单个任务点
        与 main.py process_job() 一致
        """
        job_type = job.get("type", "")
        self.log(f"  处理任务: type={job_type}, jobid={job.get('jobid', '')}")

        if job_type == "video":
            self.log(f"  识别到视频任务, 任务章节: {course.get('title', '未知')} 任务ID: {job.get('jobid', '')}")
            # 超星的接口没有返回当前任务是否为Audio音频任务
            video_result = self.study_video(course, job, job_info, _speed=speed, _type="Video")
            if video_result.is_failure():
                self.log("  当前任务非视频任务, 正在尝试音频任务解码")
                video_result = self.study_video(course, job, job_info, _speed=speed, _type="Audio")
            if video_result.is_failure():
                self.log(f"  出现异常任务 -> 任务章节: {course.get('title', '未知')} 任务ID: {job.get('jobid', '')}, 已跳过")
            return video_result
        elif job_type == "document":
            self.log(f"  识别到文档任务, 任务章节: {course.get('title', '未知')} 任务ID: {job.get('jobid', '')}")
            return self.study_document(course, job)
        elif job_type == "workid":
            self.log(f"  识别到章节检测任务, 任务章节: {course.get('title', '未知')}")
            return self.study_work(course, job, job_info)
        elif job_type == "read":
            self.log(f"  识别到阅读任务, 任务章节: {course.get('title', '未知')}")
            return self.study_read(course, job, job_info)
        elif job_type == "live":
            self.log(f"  识别到直播任务, 任务章节: {course.get('title', '未知')} 任务ID: {job.get('jobid', '')}")
            # 直播任务简化处理：直接标记成功（Android 环境不处理直播）
            self.log(f"  直播任务，跳过: {job.get('name', '未知')}")
            return StudyResult.SUCCESS
        else:
            self.log(f"  未知任务类型: {job_type}")
            return StudyResult.ERROR

    def _process_chapter_internal(self, course, point, speed):
        """
        处理单个章节，返回 ChapterResult
        供 AndroidJobProcessor 和 process_chapter 内部调用
        与 main.py process_chapter() 一致
        """
        chapter_title = point.get('title') or point.get('name') or f"章节 (id={point.get('id', '')})"
        set_current_chapter(chapter_title)
        self.log(f"当前章节: {chapter_title} (id={point.get('id', '')})")

        if point.get("has_finished", False):
            self.log(f"章节: {point.get('title', '未知')} 已完成所有任务点")
            return ChapterResult.SUCCESS

        # 随机等待，避免请求过快
        self.rate_limiter.limit_rate(random_time=True, random_min=0, random_max=0.2)

        # 获取当前章节的所有任务点
        job_info = None
        jobs, job_info = self.get_job_list(course, point)

        # 发现未开放章节
        if job_info.get("notOpen", False):
            self.log(f"章节未开放: {point.get('title', '未知')}")
            return ChapterResult.NOT_OPEN

        if not jobs:
            self.log(f"章节无任务: {point.get('title', '未知')}")
            return ChapterResult.SUCCESS

        # 使用 threading.Thread 并行处理章节内的任务点
        # （Android Chaquopy 不支持 concurrent.futures.ThreadPoolExecutor）
        # 与 main.py process_chapter() 的 ThreadPoolExecutor 逻辑一致
        job_results = []
        job_results_lock = threading.Lock()
        job_threads = []

        def _run_job(job):
            result = self.process_job(course, job, job_info, speed)
            with job_results_lock:
                job_results.append(result)

        for job in jobs:
            t = threading.Thread(target=_run_job, args=(job,), daemon=True)
            job_threads.append(t)
            t.start()

        for t in job_threads:
            t.join()

        for result in job_results:
            if result.is_failure():
                self.log(f"章节 {point.get('title', '未知')} 存在失败任务")
                return ChapterResult.ERROR

        self.log(f"章节处理完成: {point.get('title', '未知')}")
        return ChapterResult.SUCCESS

    def process_chapter(self, course, point, speed):
        """
        处理单个章节（对外接口，不返回值）
        内部调用 _process_chapter_internal
        """
        self._process_chapter_internal(course, point, speed)

    def process_course(self, course, speed):
        """
        处理单个课程的所有章节：
        刷视频流水线与答题流水线同时进行（并发处理），
        实时上报当前课时、当前章节、视频分秒进度与答题题号进度
        """
        course_name = course.get('title') or course.get('name') or course.get('coursename') or '未知课程'
        set_current_course(course_name)
        self.log(f"开始学习课程: {course_name}")

        # 获取当前课程的所有章节
        course_point = self.get_course_point(course["courseId"], course["clazzId"], course["cpi"])
        points = course_point.get("points", [])

        if not points:
            self.log(f"课程无章节: {course_name}")
            return

        self.log(f"正在扫描并准备章节任务 (共 {len(points)} 个章节)...")

        point_data = {}
        point_lock = threading.Lock()

        def _scan_point(p):
            if p.get("has_finished", False):
                return
            try:
                jobs, job_info = self.get_job_list(course, p)
                if jobs:
                    with point_lock:
                        point_data[p["id"]] = (p, jobs, job_info)
            except Exception as e:
                self.log(f"扫描章节 {p.get('title', '未知')} 失败: {e}")

        # 限制并发扫描线程数，平滑请求
        scan_threads = []
        for p in points:
            if not self._running:
                break
            t = threading.Thread(target=_scan_point, args=(p,), daemon=True)
            scan_threads.append(t)
            t.start()
            if len(scan_threads) >= 6:
                for st in scan_threads:
                    st.join()
                scan_threads = []
        for st in scan_threads:
            st.join()

        if not self._running:
            self.log("刷课已被停止")
            return

        # 整理视频、测验与其他任务
        video_tasks = []
        quiz_tasks = []
        other_tasks = []

        for p in points:
            if p["id"] in point_data:
                p_item, jobs, job_info = point_data[p["id"]]
                for job in jobs:
                    j_type = job.get("type", "")
                    if j_type == "video":
                        video_tasks.append((p_item, job, job_info))
                    elif j_type == "workid":
                        quiz_tasks.append((p_item, job, job_info))
                    else:
                        other_tasks.append((p_item, job, job_info))

        self.log(f"[双流水线] 视频任务 {len(video_tasks)} 个, 测验答题 {len(quiz_tasks)} 个, 其他任务 {len(other_tasks)} 个")
        self.log("[双流水线] 刷视频和答题已配置为同时进行！")

        total_tasks = max(1, len(video_tasks) + len(quiz_tasks) + len(other_tasks))
        done_tasks = 0
        done_lock = threading.Lock()

        def _record_done():
            nonlocal done_tasks
            with done_lock:
                done_tasks += 1
                prog = min(1.0, done_tasks / total_tasks)
                set_study_progress(prog)
                self.on_progress(prog)

        def _run_video_pipeline():
            for p, job, job_info in video_tasks:
                if not self._running:
                    break
                p_title = p.get('title') or p.get('name') or "视频章节"
                set_current_chapter(p_title)
                self.log(f"[视频流水线] 开始任务: {job.get('name', '视频')} (章节: {p_title})")
                try:
                    self.process_job(course, job, job_info, speed)
                except Exception as e:
                    self.log(f"[视频流水线] 任务异常: {e}")
                _record_done()
            clear_current_video()

        def _run_quiz_pipeline():
            for p, job, job_info in quiz_tasks:
                if not self._running:
                    break
                p_title = p.get('title') or p.get('name') or "测验章节"
                self.log(f"[答题流水线] 开始答题: {job.get('name', '章节测验')} (章节: {p_title})")
                try:
                    self.process_job(course, job, job_info, speed)
                except Exception as e:
                    self.log(f"[答题流水线] 答题异常: {e}")
                _record_done()
            clear_current_quiz()

        def _run_other_pipeline():
            for p, job, job_info in other_tasks:
                if not self._running:
                    break
                try:
                    self.process_job(course, job, job_info, speed)
                except Exception as e:
                    self.log(f"[其他任务] 异常: {e}")
                _record_done()

        # 并发启动视频流水线、答题流水线与其他任务流水线（同时进行）
        v_thread = threading.Thread(target=_run_video_pipeline, daemon=True)
        q_thread = threading.Thread(target=_run_quiz_pipeline, daemon=True)
        o_thread = threading.Thread(target=_run_other_pipeline, daemon=True)

        v_thread.start()
        q_thread.start()
        o_thread.start()

        v_thread.join()
        q_thread.join()
        o_thread.join()

        # 更新进度（所有章节处理完成后）
        self.on_progress(1.0)
        set_study_progress(1.0)
        clear_current_video()
        clear_current_quiz()
        self.log(f"课程学习完成: {course_name}")

    def run(self, course_ids=None, speed=1.0, jobs=4):
        """
        运行刷课任务
        与参考实现一致：先登录，获取课程列表，过滤，然后逐个处理
        """
        self.log("===== 开始刷课任务 =====")
        self._running = True
        self._worker_count = jobs if jobs else 4

        # 登录
        login_success = self.login(login_with_cookies=True)
        if not login_success:
            self.log("[错误] 登录失败，无法开始刷课")
            self._running = False
            return

        # 获取课程列表
        courses = self.get_course_list()
        if not courses:
            self.log("[错误] 未获取到课程列表")
            self._running = False
            return

        # 过滤课程
        if course_ids:
            id_list = [c.strip() for c in course_ids.split(",")]
            courses = [c for c in courses if str(c.get("courseId", "")) in id_list]
            self.log(f"过滤后剩余 {len(courses)} 门课程")

        # 逐个处理课程
        for course in courses:
            if not self._running:
                self.log("刷课任务被用户停止")
                break
            self.process_course(course, speed)

        self._running = False
        self.log("===== 刷课任务完成 =====")


# ==================== 全局进度管理 ====================
_current_course_name = ""
_current_chapter_name = ""

def set_current_course(name):
    global _current_course_name
    _current_course_name = str(name) if name else ""

def set_current_chapter(name):
    global _current_chapter_name
    _current_chapter_name = str(name) if name else ""

def get_current_course():
    global _current_course_name
    return _current_course_name

def get_current_chapter():
    global _current_chapter_name
    return _current_chapter_name

def get_study_status():
    global _study_running, _study_progress, _current_course_name, _current_chapter_name, _study_result
    global _current_video_progress, _current_video_name, _current_quiz_progress, _current_quiz_title
    return json.dumps({
        "running": bool(_study_running),
        "progress": float(_study_progress),
        "course": _current_course_name,
        "chapter": _current_chapter_name,
        "video_progress": _current_video_progress,
        "video_name": _current_video_name,
        "quiz_progress": _current_quiz_progress,
        "quiz_title": _current_quiz_title,
        "result": str(_study_result or "")
    })

def set_study_progress(progress):
    """更新全局刷课进度"""
    global _study_progress
    _study_progress = progress


# ==================== 后台任务入口 ====================
def _run_study_task(username, password, course_ids=None, speed=1.0, jobs=4,
                    llm_config="", courses_json=""):
    """
    在后台线程中执行刷课任务
    与参考实现 start_study 逻辑一致
    接受 Kotlin 端传入的 llm_config 和 courses_json 参数
    """
    import traceback
    global _study_running, _study_result
    _study_running = True
    _study_result = None

    try:
        if llm_config:
            _log(f"[配置] 使用大模型配置: {llm_config[:50]}...")
        if courses_json:
            _log(f"[配置] 使用已获取的课程列表 ({len(courses_json)} 字符)")

        engine = AndroidStudyEngine(username, password, llm_config=llm_config)
        engine.on_progress = set_study_progress
        engine.run(course_ids=course_ids, speed=speed, jobs=jobs)
        _study_result = "success"
    except Exception as e:
        tb = traceback.format_exc()
        _log(f"[错误] 刷课任务异常: {e}")
        _log(f"[错误] 完整堆栈:\n{tb}")
        _study_result = f"error: {e}"
    finally:
        _study_running = False


def start_study(username, password, course_ids=None, speed=1.0, jobs=4, llm_config="", courses_json=""):
    """
    启动刷课任务（在后台线程中运行）
    与参考实现一致：启动后台线程执行 _run_study_task
    接受 Kotlin 端传入的 llm_config 和 courses_json 参数
    """
    global _study_running
    if _study_running:
        _log("[警告] 刷课任务已在运行中")
        return "already_running"

    # 清空实时日志缓冲区与进度
    global _study_log_buffer
    _study_log_buffer = []
    clear_current_video()
    clear_current_quiz()

    if llm_config:
        _log(f"[配置] 收到大模型配置: {llm_config[:50]}...")
    if courses_json:
        _log(f"[配置] 收到已获取的课程列表: {len(courses_json)} 字符")

    thread = threading.Thread(
        target=_run_study_task,
        args=(username, password),
        kwargs={"course_ids": course_ids, "speed": speed, "jobs": jobs,
                "llm_config": llm_config, "courses_json": courses_json},
        daemon=True,
    )
    thread.start()
    _log("刷课任务已启动")
    return "started"


# ==================== 单次操作入口 ====================
def login_only(username, password):
    """
    仅执行登录操作，用于测试账号有效性
    返回 JSON 字符串，供 Kotlin 端判断详细提示
    """
    engine = AndroidStudyEngine(username, password)
    engine.on_progress = set_study_progress
    success = engine.login()
    if success:
        _log("登录成功")
        return json.dumps({"success": True, "msg": "登录成功"})
    else:
        err_msg = getattr(engine, 'last_login_msg', '用户名或密码错误，请检查') or '用户名或密码错误，请检查'
        _log(f"登录失败: {err_msg}")
        return json.dumps({"success": False, "msg": err_msg})


def get_courses(username, password):
    """
    获取课程列表，用于展示
    与参考实现一致
    """
    engine = AndroidStudyEngine(username, password)
    engine.on_progress = set_study_progress
    if not engine.login():
        _log("登录失败，无法获取课程列表")
        return []
    courses = engine.get_course_list()
    _log(f"获取到 {len(courses)} 门课程")
    return courses


def test_environment():
    """
    测试 Python 环境是否正常
    与参考实现一致
    """
    try:
        import requests
        from bs4 import BeautifulSoup
        _log("环境检测通过")
        return "ok"
    except ImportError as e:
        _log(f"环境检测失败: {e}")
        return f"error: {e}"


# ==================== 大模型答题支持 ====================
class AndroidAI:
    """
    Android 版 AI 答题实现
    与 api/answer.py 中的 AI 类功能一致，使用 requests.post() 直接 HTTP 请求
    （不依赖 openai 库，避免 jiter 在 Chaquopy Python 3.8 环境无法安装的问题）
    """

    def __init__(self, config: dict):
        # 兼容 Kotlin 端传入的字段名 (endpoint/key) 和 Python 端字段名 (api_url/api_key)
        self.api_key = config.get("api_key") or config.get("key", "")
        raw_url = config.get("api_url") or config.get("endpoint", "https://api.openai.com/v1")
        self.api_url = raw_url
        # 如果 endpoint 已经包含 /chat/completions，不再追加
        if "/chat/completions" in raw_url:
            self.endpoint = raw_url
        else:
            self.endpoint = raw_url.rstrip("/") + "/chat/completions"
        self.model = config.get("model", "gpt-3.5-turbo")
        self.timeout = config.get("timeout", 60)
        self.max_retries = config.get("max_retries", 3)
        self.log_func = None

    def set_log_func(self, func):
        """设置日志回调函数"""
        self.log_func = func

    def _log(self, msg: str):
        """内部日志"""
        if self.log_func:
            self.log_func(msg)

    def query(self, q_info: dict) -> str:
        """
        向 AI 提问并获取答案
        使用 requests.post() 直接调用 OpenAI 兼容 API，与原始项目 AI._query() 功能一致
        """
        def remove_md_json_wrapper(md_str):
            """去除 Markdown 代码块包装，与原始项目一致"""
            import re as _re
            pattern = r'^\s*```(?:json)?\s*(.*?)\s*```\s*$'
            match = _re.search(pattern, md_str, _re.DOTALL)
            return match.group(1).strip() if match else md_str.strip()

        title = q_info.get("title", "")
        options = q_info.get("options", "")
        q_type = q_info.get("type", "")

        # 去除选项字母（ABCD），防止大模型直接输出字母而非内容，与原始项目一致
        if options:
            options_list = options.split('\n')
            import re as _re
            cleaned_options = [_re.sub(r"^[A-Z]\s*", "", option) for option in options_list]
            options = "\n".join(cleaned_options)

        # 按题目类型构造 system prompt 和 user content，与原始项目 AI._query() 一致
        if q_type == "single":
            system_prompt = "本题为单选题，你只能选择一个选项，请根据题目和选项回答问题，以json格式输出正确的选项内容，示例回答：{\"Answer\": [\"答案\"]}。除此之外不要输出任何多余的内容，也不要使用MD语法。如果你使用了互联网搜索，也请不要返回搜索的结果和参考资料"
            user_content = f"题目：{title}\n选项：{options}"
        elif q_type == "multiple":
            system_prompt = "本题为多选题，你必须选择两个或以上选项，请根据题目和选项回答问题，以json格式输出正确的选项内容，示例回答：{\"Answer\": [\"答案1\",\n\"答案2\",\n\"答案3\"]}。除此之外不要输出任何多余的内容，也不要使用MD语法。如果你使用了互联网搜索，也请不要返回搜索的结果和参考资料"
            user_content = f"题目：{title}\n选项：{options}"
        elif q_type == "completion":
            system_prompt = "本题为填空题，你必须根据语境和相关知识填入合适的内容，请根据题目回答问题，以json格式输出正确的答案，示例回答：{\"Answer\": [\"答案\"]}。除此之外不要输出任何多余的内容，也不要使用MD语法。如果你使用了互联网搜索，也请不要返回搜索的结果和参考资料"
            user_content = f"题目：{title}"
        elif q_type == "judgement":
            system_prompt = "本题为判断题，你只能回答正确或者错误，请根据题目回答问题，以json格式输出正确的答案，示例回答：{\"Answer\": [\"正确\"]}。除此之外不要输出任何多余的内容，也不要使用MD语法。如果你使用了互联网搜索，也请不要返回搜索的结果和参考资料"
            user_content = f"题目：{title}"
        else:
            system_prompt = "本题为简答题，你必须根据语境和相关知识填入合适的内容，请根据题目回答问题，以json格式输出正确的答案，示例回答：{\"Answer\": [\"这是我的答案\"]}。除此之外不要输出任何多余的内容，也不要使用MD语法。如果你使用了互联网搜索，也请不要返回搜索的结果和参考资料"
            user_content = f"题目：{title}"

        # 构造请求头和请求体，使用 requests.post() 直接调用 API
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_content},
            ],
        }

        for attempt in range(self.max_retries):
            try:
                self._log(f"[AI] 正在请求 {self.model}... (第{attempt+1}次尝试)")
                response = requests.post(
                    self.endpoint,
                    headers=headers,
                    json=payload,
                    timeout=self.timeout,
                )

                if response.status_code == 200:
                    result = response.json()
                    content = result['choices'][0]['message']['content'].strip()
                    # 解析 JSON 格式的返回，与原始项目一致
                    try:
                        import json as _json
                        parsed = _json.loads(remove_md_json_wrapper(content))
                        answer = "\n".join(parsed['Answer']).strip()
                    except Exception:
                        self._log(f"[AI] 无法解析大模型输出内容: {content[:100]}")
                        answer = content
                    self._log(f"[AI] 获取到答案: {answer[:80]}...")
                    return answer
                else:
                    self._log(f"[AI] API请求失败: {response.status_code} {response.text[:100]}")
            except Exception as e:
                self._log(f"[AI] 请求失败: {e}")
                if attempt < self.max_retries - 1:
                    import time as _time
                    _time.sleep(2)
                else:
                    self._log("[AI] 已达最大重试次数")
                    return ""

    def check_connection(self) -> bool:
        """测试 AI 连接是否正常，使用 requests.post() 直接调用"""
        try:
            headers = {
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json"
            }
            payload = {
                "model": self.model,
                "messages": [{'role': 'user', 'content': '你好，请回答：1+1 等于几？只回答数字。'}],
                "max_tokens": 10,
            }
            response = requests.post(
                self.endpoint,
                headers=headers,
                json=payload,
                timeout=10,
            )
            if response.status_code == 200:
                result = response.json()
                return bool(result.get('choices') and result['choices'][0].get('message', {}).get('content'))
            return False
        except Exception:
            return False


class AndroidSiliconFlow:
    """
    Android 版 SiliconFlow 大模型答题实现
    与 api/answer.py 中的 SiliconFlow 类完全一致
    """

    def __init__(self, config: dict):
        # 兼容 Kotlin 端传入的字段名 (endpoint/key) 和 Python 端字段名 (api_url/api_key)
        self.api_key = config.get("api_key") or config.get("key", "")
        raw_url = config.get("api_url") or config.get("endpoint", "https://api.siliconflow.cn/v1/chat/completions")
        self.api_url = raw_url
        self.api_endpoint = raw_url  # 与原始项目 SiliconFlow._init_tiku() 中的 self.api_endpoint 对应
        self.model = config.get("model", "Qwen/Qwen2-7B-Instruct")
        self.model_name = self.model  # 与原始项目 SiliconFlow._init_tiku() 中的 self.model_name 对应
        self.timeout = config.get("timeout", 60)
        self.max_retries = config.get("max_retries", 3)
        self.log_func = None

    def set_log_func(self, func):
        """设置日志回调函数"""
        self.log_func = func

    def _log(self, msg: str):
        """内部日志"""
        if self.log_func:
            self.log_func(msg)

    def query(self, q_info: dict) -> str:
        """
        向 SiliconFlow 提问并获取答案
        与原始项目 api/answer.py SiliconFlow._query() 完全一致
        """
        def remove_md_json_wrapper(md_str):
            """去除 Markdown 代码块包装，与原始项目一致"""
            import re as _re
            pattern = r'^\s*```(?:json)?\s*(.*?)\s*```\s*$'
            match = _re.search(pattern, md_str, _re.DOTALL)
            return match.group(1).strip() if match else md_str.strip()

        title = q_info.get("title", "")
        options = q_info.get("options", "")
        q_type = q_info.get("type", "")

        # 构造请求头，与原始项目一致
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }

        # 构造系统提示词，与原始项目 SiliconFlow._query() 一致
        system_prompt = ""
        if q_type == "single":
            system_prompt = "本题为单选题，请根据题目和选项选择唯一正确答案，输出的是选项的具体内容，而不是内容前的ABCD，并以JSON格式输出：示例回答：{\"Answer\": [\"正确选项内容\"]}。除此之外不要输出任何多余的内容，也不要使用MD语法。如果你使用了互联网搜索，也请不要返回搜索的结果和参考资料"
        elif q_type == "multiple":
            system_prompt = "本题为多选题，请选择所有正确选项，输出的是选项的具体内容，而不是内容前的ABCD，以JSON格式输出：示例回答：{\"Answer\": [\"选项1\",\"选项2\"]}。除此之外不要输出任何多余的内容，也不要使用MD语法。如果你使用了互联网搜索，也请不要返回搜索的结果和参考资料"
        elif q_type == "completion":
            system_prompt = "本题为填空题，请直接给出填空内容，以JSON格式输出：示例回答：{\"Answer\": [\"答案文本\"]}。除此之外不要输出任何多余的内容，也不要使用MD语法。如果你使用了互联网搜索，也请不要返回搜索的结果和参考资料"
        elif q_type == "judgement":
            system_prompt = "本题为判断题，请回答'正确'或'错误'，以JSON格式输出：示例回答：{\"Answer\": [\"正确\"]}。除此之外不要输出任何多余的内容，也不要使用MD语法。如果你使用了互联网搜索，也请不要返回搜索的结果和参考资料"

        # 构造请求体，与原始项目一致
        payload = {
            "model": self.model_name,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"题目：{title}\n选项：{options}"},
            ],
            "stream": False,
            "max_tokens": 4096,
            "temperature": 0.7,
            "top_p": 0.7,
            "response_format": {"type": "text"},
        }

        for attempt in range(self.max_retries):
            try:
                self._log(f"[SiliconFlow] 正在请求 {self.model_name}... (第{attempt+1}次尝试)")
                response = requests.post(
                    self.api_endpoint,
                    headers=headers,
                    json=payload,
                    timeout=self.timeout,
                )

                if response.status_code == 200:
                    result = response.json()
                    content = result['choices'][0]['message']['content']
                    import json as _json
                    parsed = _json.loads(remove_md_json_wrapper(content))
                    answer = "\n".join(parsed['Answer']).strip()
                    self._log(f"[SiliconFlow] 获取到答案: {answer[:80]}...")
                    return answer
                else:
                    self._log(f"[SiliconFlow] API请求失败: {response.status_code} {response.text[:100]}")
            except Exception as e:
                self._log(f"[SiliconFlow] 请求异常: {e}")
                if attempt < self.max_retries - 1:
                    import time as _time
                    _time.sleep(2)
                else:
                    self._log("[SiliconFlow] 已达最大重试次数")
                    return ""

    def check_connection(self) -> bool:
        """测试 SiliconFlow 连接是否正常"""
        try:
            resp = requests.get(
                "https://api.siliconflow.cn/v1/models",
                headers={"Authorization": f"Bearer {self.api_key}"},
                timeout=10,
            )
            return resp.status_code == 200
        except Exception:
            return False


def create_llm_provider(config: dict):
    """
    根据配置创建大模型答题提供者
    与 api/answer.py 中的 create_llm_provider 兼容

    config 格式:
    {
        "provider": "ai" | "siliconflow",
        "api_key": "...",
        "api_url": "...",
        "model": "...",
        "timeout": 60,
        "max_retries": 3
    }
    """
    provider_type = config.get("provider", "ai").lower()
    if provider_type == "siliconflow":
        return AndroidSiliconFlow(config)
    else:
        return AndroidAI(config)


def test_llm_connection(config_json: str) -> str:
    """
    测试大模型连接是否正常
    由 Kotlin 通过 PythonBridge 调用

    Args:
        config_json: JSON 字符串，包含 provider, api_key, api_url, model 等

    Returns:
        JSON 字符串 {"success": true/false, "message": "..."}，供 Kotlin 端解析
    """
    try:
        config = json.loads(config_json)
        provider = create_llm_provider(config)
        if provider.check_connection():
            _log("大模型连接测试成功")
            return json.dumps({"success": True, "message": "大模型连接测试成功"})
        else:
            _log("大模型连接测试失败")
            return json.dumps({"success": False, "message": "连接失败，请检查 API Key 和 API URL 是否正确"})
    except json.JSONDecodeError as e:
        msg = f"配置解析失败: {e}"
        _log(msg)
        return json.dumps({"success": False, "message": msg})
    except Exception as e:
        msg = f"连接测试异常: {e}"
        _log(msg)
        return json.dumps({"success": False, "message": msg})
