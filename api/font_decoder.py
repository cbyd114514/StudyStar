import re
from typing import Dict, Optional

from bs4 import BeautifulSoup

import api.cxsecret_font as cxfont
from api.exceptions import FontDecodeError
from api.logger import logger


class FontDecoder:
    """超星加密字体解码器。
    
    用于解码超星平台使用特殊字体加密的内容。
    支持各类单双引号、无引号或多行换行的 Base64 字体数据，
    支持多位置自动查找 @font-face 声明，并在未生成字体映射时安全降级回退。
    """
    
    FONT_BASE64_PATTERN = r"base64,([A-Za-z0-9+/=\r\n\t ]+)"
    FONT_DATA_URL_PREFIX = "data:application/font-ttf;charset=utf-8;base64,"
    
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
        """从HTML内容中提取字体信息并初始化字体映射。
        
        Args:
            html_content: 包含加密字体信息的HTML内容
        """
        try:
            soup = BeautifulSoup(html_content, "lxml")
            style_text = ""
            
            style_tag = soup.find("style", id="cxSecretStyle")
            if style_tag and style_tag.text:
                style_text = style_tag.text
            else:
                for st in soup.find_all("style"):
                    txt = st.text or ""
                    if "cxsecret" in txt or "base64," in txt:
                        style_text = txt
                        break

            if not style_text:
                match = re.search(r"@font-face\s*\{[^}]*base64,[A-Za-z0-9+/=\r\n\t ]+[^}]*\}", html_content)
                if match:
                    style_text = match.group(0)

            if not style_text:
                self.__font_map = {}
                return

            match = re.search(self.FONT_BASE64_PATTERN, style_text)
            if not match:
                self.__font_map = {}
                return

            font_base64 = match.group(1).replace("\r", "").replace("\n", "").replace("\t", "").replace(" ", "").strip().strip("'\"();")
            if not font_base64:
                self.__font_map = {}
                return

            font_data_url = self.FONT_DATA_URL_PREFIX + font_base64
            self.__font_map = cxfont.font2map(font_data_url)
        except Exception as e:
            logger.warning(f"初始化字体映射失败: {e}")
            self.__font_map = {}
    
    def decode(self, target_str: str) -> str:
        """解码加密字符串。
        
        Args:
            target_str: 需要解码的加密字符串
            
        Returns:
            解码后的字符串，若映射未初始化或解码异常则安全返回原字符串
        """
        if not target_str or not self.__font_map:
            return target_str

        try:
            return cxfont.decrypt(self.__font_map, target_str)
        except Exception as e:
            logger.warning(f"单项字体解码异常: {e}")
            return target_str
    
    def set_html_content(self, html_content: str) -> None:
        """设置新的HTML内容并重新初始化字体映射。
        
        Args:
            html_content: 包含加密字体信息的HTML内容
        """
        self.html_content = html_content
        self.__init_font_map(html_content)
