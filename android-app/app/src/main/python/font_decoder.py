import re
from typing import Dict, Optional

from bs4 import BeautifulSoup

import cxsecret_font as cxfont
from exceptions import FontDecodeError


class FontDecoder:
    """超星加密字体解码器。

    用于解码超星平台使用特殊字体加密的内容。
    经过全方位强化：支持各类单双引号、无引号或多行换行的 Base64 字体数据，
    支持多位置自动查找 `@font-face` 声明，且在字体映射未生成时安全回退，绝不导致答题流程崩溃。
    """

    # 正则表达式：贪婪/非贪婪兼顾提取 base64 字体字符
    FONT_BASE64_PATTERN = r"base64,([A-Za-z0-9+/=\r\n\t ]+)"

    def __init__(self, html_content: Optional[str] = None):
        """初始化字体解码器。

        Args:
            html_content: 包含加密字体信息的HTML内容
        """
        self.html_content = html_content
        self.__font_map: Optional[Dict] = None

        if html_content:
            self.__init_font_map(html_content)

    def is_initialized(self) -> bool:
        """检查解码器是否已成功就绪"""
        return bool(self.__font_map)

    def __init_font_map(self, html_content: str) -> None:
        """从HTML内容中提取字体信息并初始化字体映射。"""
        try:
            soup = BeautifulSoup(html_content, "html.parser")
            style_text = ""

            # 1. 优先查找官方常见标志标签 <style id="cxSecretStyle">
            style_tag = soup.find("style", id="cxSecretStyle")
            if style_tag and style_tag.text:
                style_text = style_tag.text
            else:
                # 2. 备用策略：检索所有含 base64 或 cxsecret 的 style 标签
                for st in soup.find_all("style"):
                    txt = st.text or ""
                    if "cxsecret" in txt or "base64," in txt:
                        style_text = txt
                        break

            # 3. 终极备用策略：从整个原始 HTML 中直接通过正则匹配 @font-face 块
            if not style_text:
                match = re.search(r"@font-face\s*\{[^}]*base64,[A-Za-z0-9+/=\r\n\t ]+[^}]*\}", html_content)
                if match:
                    style_text = match.group(0)

            if not style_text:
                # 当前页面未声明自定义加密字体，将映射设为空字典（后续直通，不报错）
                self.__font_map = {}
                return

            match = re.search(self.FONT_BASE64_PATTERN, style_text)
            if not match:
                self.__font_map = {}
                return

            # 清理所有不可见空白与闭合引号
            font_base64 = match.group(1).replace("\r", "").replace("\n", "").replace("\t", "").replace(" ", "").strip().strip("'\"();")
            if not font_base64:
                self.__font_map = {}
                return

            self.__font_map = cxfont.font2map(font_base64)
        except Exception as e:
            print(f"[FontDecoder] 初始化字体映射警告: {e}")
            self.__font_map = {}

    def decode(self, target_str: str) -> str:
        """解码加密字符串。

        若字体映射未生成或为空，平滑返回原始字符串，保证答题流程坚固不中断。
        """
        if not target_str or not self.__font_map:
            return target_str

        try:
            return cxfont.decrypt(self.__font_map, target_str)
        except Exception as e:
            print(f"[FontDecoder] 单项解码容错: {e}")
            return target_str

    def set_html_content(self, html_content: str) -> None:
        """设置新的HTML内容并重新初始化字体映射。"""
        self.html_content = html_content
        self.__init_font_map(html_content)
