#!/usr/bin/env python3
"""
Generate high-quality Material Design Android app icons from source image:
- Clean watermark
- Generate density-specific adaptive foreground in mipmap (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- Generate legacy square icons in mipmap
- Generate legacy circular icons in mipmap
- Generate app_logo.png in drawable for in-app display
"""
import os
import shutil
from PIL import Image, ImageDraw
import numpy as np

SOURCE_PATH = r"C:/Users/18062/.gemini/antigravity/brain/4d19c274-fc3c-497b-8fbb-b7151dae0c33/.user_uploaded/media_1792584739006_47998563.jpg"
RES_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "app", "src", "main", "res")

# 1. Load and clean image
img = Image.open(SOURCE_PATH).convert("RGBA")
arr = np.array(img)

# Clear watermark in bottom right (y > 910, x > 720)
watermark_region = (arr[910:, 720:, :3] > 220).all(axis=2)
arr[910:, 720:][watermark_region] = [255, 255, 255, 255]

cleaned_img = Image.fromarray(arr)

# 2. Dimensions for legacy icons & adaptive foregrounds
# Android standard density scales:
# mdpi = 1.0x (legacy 48x48, adaptive 108x108)
# hdpi = 1.5x (legacy 72x72, adaptive 162x162)
# xhdpi = 2.0x (legacy 96x96, adaptive 216x216)
# xxhdpi = 3.0x (legacy 144x144, adaptive 324x324)
# xxxhdpi = 4.0x (legacy 192x192, adaptive 432x432)
densities = {
    "mipmap-mdpi": (48, 108),
    "mipmap-hdpi": (72, 162),
    "mipmap-xhdpi": (96, 216),
    "mipmap-xxhdpi": (144, 324),
    "mipmap-xxxhdpi": (192, 432),
}

def create_circular_icon(image, size):
    """Create a circular clipped icon with smooth antialiasing."""
    scale = 4
    high_size = size * scale
    high_img = image.resize((high_size, high_size), Image.Resampling.LANCZOS)
    
    mask = Image.new("L", (high_size, high_size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, high_size - 1, high_size - 1), fill=255)
    
    result = Image.new("RGBA", (high_size, high_size), (0, 0, 0, 0))
    result.paste(high_img, (0, 0), mask=mask)
    
    return result.resize((size, size), Image.Resampling.LANCZOS)

def create_rounded_icon(image, size, corner_radius_ratio=0.20):
    """Create a modern Material squircle/rounded icon."""
    scale = 4
    high_size = size * scale
    radius = int(high_size * corner_radius_ratio)
    high_img = image.resize((high_size, high_size), Image.Resampling.LANCZOS)
    
    mask = Image.new("L", (high_size, high_size), 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle((0, 0, high_size - 1, high_size - 1), radius=radius, fill=255)
    
    result = Image.new("RGBA", (high_size, high_size), (0, 0, 0, 0))
    result.paste(high_img, (0, 0), mask=mask)
    
    return result.resize((size, size), Image.Resampling.LANCZOS)

def create_adaptive_foreground(image, canvas_size):
    """
    Android adaptive icon foreground:
    Canvas is 108dp. Safe area is central 72dp (66.6%).
    The character is centered inside the safe area.
    """
    safe_size = int(canvas_size * 0.72)
    offset = (canvas_size - safe_size) // 2
    char_resized = image.resize((safe_size, safe_size), Image.Resampling.LANCZOS)
    
    fg_canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    fg_canvas.paste(char_resized, (offset, offset))
    return fg_canvas

# 3. Generate legacy icons and adaptive foregrounds for each density
for folder, (legacy_size, adaptive_size) in densities.items():
    folder_path = os.path.join(RES_DIR, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # Legacy Square / rounded icon
    sq_icon = create_rounded_icon(cleaned_img, legacy_size)
    sq_path = os.path.join(folder_path, "ic_launcher.png")
    sq_icon.save(sq_path, "PNG", optimize=True)
    
    # Legacy Round icon
    round_icon = create_circular_icon(cleaned_img, legacy_size)
    round_path = os.path.join(folder_path, "ic_launcher_round.png")
    round_icon.save(round_path, "PNG", optimize=True)
    
    # Adaptive Foreground icon in mipmap
    fg_icon = create_adaptive_foreground(cleaned_img, adaptive_size)
    fg_path = os.path.join(folder_path, "ic_launcher_foreground.png")
    fg_icon.save(fg_path, "PNG", optimize=True)
    
    print(f"Generated {folder}: legacy={legacy_size}x{legacy_size}, adaptive_fg={adaptive_size}x{adaptive_size}")

# 4. Save in-app high-res logo in drawable
drawable_dir = os.path.join(RES_DIR, "drawable")
os.makedirs(drawable_dir, exist_ok=True)

# Remove old drawable/ic_launcher_foreground.png if it exists to avoid ambiguity with mipmap
old_fg = os.path.join(drawable_dir, "ic_launcher_foreground.png")
if os.path.exists(old_fg):
    os.remove(old_fg)
    print("Removed obsolete drawable/ic_launcher_foreground.png")

logo_path = os.path.join(drawable_dir, "app_logo.png")
app_logo = cleaned_img.resize((512, 512), Image.Resampling.LANCZOS)
app_logo.save(logo_path, "PNG", optimize=True)
print(f"Generated in-app logo: {logo_path} (512x512)")

print("All Android icons generated successfully!")
