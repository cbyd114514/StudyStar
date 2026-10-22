##
# @Author: SocialSisterYi
# @Edit: Samueli924
# @Reference: https://github.com/SocialSisterYi/xuexiaoyi-to-xuexitong-tampermonkey-proxy
#
# Android 适配版：增强 resource_path，多重兼容 Chaquopy 与物理文件系统
#

import base64
import hashlib
import json
import os
import sys
import zipfile
from io import BytesIO
from pathlib import Path
from typing import Dict, IO, Optional, Union

from fontTools.ttLib.tables._g_l_y_f import Glyph, table__g_l_y_f
from fontTools.ttLib.ttFont import TTFont

from exceptions import FontDecodeError

# 康熙部首替换表
KX_RADICALS_TAB = str.maketrans(
    # 康熙部首
    "⼀⼁⼂⼃⼄⼅⼆⼇⼈⼉⼊⼋⼌⼍⼎⼏⼐⼑⼒⼓⼔⼕⼖⼗⼘⼙⼚⼛⼜⼝⼞⼟⼠⼡⼢⼣⼤⼥⼦⼧⼨⼩⼪⼫⼬⼭⼮⼯⼰⼱⼲⼳⼴⼵⼶⼷⼸⼹⼺⼻⼼⼽⼾⼿⽀⽁⽂⽃⽄⽅⽆⽇⽈⽉⽊⽋⽌⽍⽎⽏⽐⽑⽒⽓⽔⽕⽖⽗⽘⽙⽚⽛⽜⽝⽞⽟⽠⽡⽢⽣⽤⽥⽦⽧⽨⽩⽪⽫⽬⽭⽮⽯⽰⽱⽲⽳⽴⽵⽶⽷⽸⽹⽺⽻⽼⽽⽾⽿⾀⾁⾂⾃⾄⾅⾆⾇⾈⾉⾊⾋⾌⾍⾎⾏⾐⾑⾒⾓⾔⾕⾖⾗⾘⾙⾚⾛⾜⾝⾞⾟⾠⾡⾢⾣⾤⾥⾦⾧⾨⾩⾪⾫⾬⾭⾮⾯⾰⾱⾲⾳⾴⾵⾶⾷⾸⾹⾺⾻⾼⾽⾾⾿⿀⿁⿂⿃⿄⿅⿆⿇⿈⿉⿊⿋⿌⿍⿎⿏⿐⿑⿒⿓⿔⿕",
    # 对应汉字
    "一丨丶丿乙亅二亠人儿入八冂冖冫几凵刀力勹匕匚匸十卜卩厂厶又口囗土士夂夊夕大女子宀寸小尢尸屮山巛工己巾干幺广廴廾弋弓彐彡彳心戈戶手支攴文斗斤方无日曰月木欠止歹殳毋比毛氏气水火爪父爻爿片牙牛犬玄玉瓜瓦甘生用田疋疒癶白皮皿目矛矢石示禸禾穴立竹米糸缶网羊羽老而耒耳聿肉臣自至臼舌舛舟艮色艸虍虫血行衣襾見角言谷豆豕豸貝赤走足身車辛辰辵邑酉采里金長門阜隶隹雨青非面革韋韭音頁風飛食首香馬骨高髟鬥鬯鬲鬼魚鳥鹵鹿麥麻黃黍黑黹黽鼎鼓鼠鼻齊齒龍龟龠民齐黄马飞见母长",
)


def load_font_map_json(file_rel_path: str = "resource/font_map_table.json") -> Dict[str, str]:
    """
    全方位鲁棒加载字体哈希映射表 JSON
    优先级：
    1. 本地物理路径（包括 __file__ 所在目录、cwd、android_study 相对路径）
    2. Chaquopy / sys.path 中的虚拟包和 zip 归档（如 app.imy、requirements.imy）
    3. pkgutil 资源接口
    """
    base_dir = os.path.dirname(os.path.abspath(__file__))

    # 1. 直接物理文件尝试
    candidates = [
        os.path.join(base_dir, file_rel_path),
        os.path.join(base_dir, os.path.basename(file_rel_path)),
        os.path.join(os.getcwd(), file_rel_path),
        file_rel_path,
        os.path.join(os.getcwd(), "resource", "font_map_table.json"),
        os.path.join(base_dir, "..", "resource", "font_map_table.json"),
    ]

    for p in candidates:
        if os.path.isfile(p):
            try:
                with open(p, "r", encoding="utf-8") as fp:
                    return json.load(fp)
            except Exception:
                pass

    # 2. 从 sys.path 中的压缩包 (如 Chaquopy 的 app.imy / requirements-common.imy) 中查找读取
    for entry in sys.path:
        if os.path.isfile(entry) and (entry.endswith(".zip") or entry.endswith(".imy") or entry.endswith(".apk")):
            try:
                with zipfile.ZipFile(entry, "r") as zf:
                    namelist = zf.namelist()
                    # 匹配 "resource/font_map_table.json" 或 "font_map_table.json"
                    target_name = None
                    for name in namelist:
                        if name.endswith("font_map_table.json"):
                            target_name = name
                            break
                    if target_name:
                        raw_bytes = zf.read(target_name)
                        return json.loads(raw_bytes.decode("utf-8"))
            except Exception:
                pass
        elif os.path.isdir(entry):
            p = os.path.join(entry, file_rel_path)
            if os.path.isfile(p):
                try:
                    with open(p, "r", encoding="utf-8") as fp:
                        return json.load(fp)
                except Exception:
                    pass

    # 3. 尝试通过 pkgutil 读取
    try:
        import pkgutil
        data = pkgutil.get_data("cxsecret_font", file_rel_path)
        if not data:
            data = pkgutil.get_data("", file_rel_path)
        if data:
            return json.loads(data.decode("utf-8"))
    except Exception:
        pass

    raise FontDecodeError(f"无法在当前运行环境中定位或读取字体映射表: {file_rel_path}")


class FontHashDAO:
    """
    字体哈希数据访问对象，负责管理字体哈希映射表
    """

    def __init__(self, file_path: str = "resource/font_map_table.json"):
        self.char_map: Dict[str, str] = {}  # unicode -> hash
        self.hash_map: Dict[str, str] = {}  # hash -> unicode

        try:
            self.char_map = load_font_map_json(file_path)
            self.hash_map = {hash_val: char for char, hash_val in self.char_map.items()}
        except Exception as e:
            raise FontDecodeError(f"加载字体映射表失败: {file_path} - {e}") from e

    def find_char(self, font_hash: str) -> Optional[str]:
        """通过字体哈希值查找对应的Unicode字符编码"""
        return self.hash_map.get(font_hash)

    def find_hash(self, char: str) -> Optional[str]:
        """通过Unicode字符编码查找对应的字体哈希值"""
        return self.char_map.get(char)


# 初始化字体哈希DAO单例
try:
    fonthash_dao = FontHashDAO()
except Exception as e:
    print(f"[cxsecret_font] 初始化字体哈希数据警告 - {e}")
    fonthash_dao = FontHashDAO.__new__(FontHashDAO)
    fonthash_dao.char_map = {}
    fonthash_dao.hash_map = {}


def hash_glyph(glyph: Glyph) -> str:
    """计算TTF字体字形的哈希值"""
    if glyph.numberOfContours <= 0:
        return ""

    pos_data = []
    last_index = 0

    for i in range(glyph.numberOfContours):
        end_point = glyph.endPtsOfContours[i]
        for j in range(last_index, end_point + 1):
            x, y = glyph.coordinates[j]
            flag = glyph.flags[j] & 0x01
            pos_data.append(f"{x}{y}{flag}")
        last_index = end_point + 1

    pos_bin = "".join(pos_data)
    return hashlib.md5(pos_bin.encode()).hexdigest()


def font2map(font_data: Union[IO, Path, str]) -> Dict[str, str]:
    """
    从字体文件或Base64编码的字体数据中提取字形哈希映射表
    兼容处理所有前缀格式与无前缀的纯 Base64 数据
    """
    font_hashmap = {}

    # 处理各种 Base64 编码的字体数据（无论前缀是 font-ttf、font-woff 还是纯 base64）
    if isinstance(font_data, str):
        b64_content = font_data
        if "base64," in b64_content:
            b64_content = b64_content.split("base64,")[-1]
        b64_clean = b64_content.strip().strip("'\"(); \r\n\t")
        try:
            font_data = BytesIO(base64.b64decode(b64_clean))
        except Exception as e:
            raise FontDecodeError(f"无法解码Base64字体数据: {e}") from e

    try:
        with TTFont(font_data, lazy=False) as font_file:
            if "glyf" in font_file:
                table: table__g_l_y_f = font_file["glyf"]
                for name in table.glyphOrder:
                    if name.startswith("uni"):
                        glyph_hash = hash_glyph(table.glyphs[name])
                        if glyph_hash:
                            font_hashmap[name] = glyph_hash
    except Exception as e:
        raise FontDecodeError(f"无法解析字体文件: {e}") from e

    return font_hashmap


def decrypt(dst_fontmap: Dict[str, str], encrypted_text: str) -> str:
    """
    解密超星学习通加密字体的文本
    """
    if not dst_fontmap or not fonthash_dao.hash_map:
        return encrypted_text

    result = []

    for char in encrypted_text:
        char_code = f"uni{ord(char):X}"

        if char_code in dst_fontmap:
            dst_hash = dst_fontmap[char_code]
            original_char_code = fonthash_dao.find_char(dst_hash)
            if original_char_code:
                try:
                    original_char = chr(int(original_char_code[3:], 16))
                    result.append(original_char)
                    continue
                except (ValueError, IndexError):
                    pass

        result.append(char)

    decrypted_text = "".join(result).translate(KX_RADICALS_TAB)
    return decrypted_text
