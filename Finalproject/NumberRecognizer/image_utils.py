# image_utils.py - 统一的图像处理函数
import numpy as np
from PIL import Image
from scipy import ndimage

# 预处理参数
THRESHOLD = 127
PADDING = 5
TARGET_SIZE = 24
MIN_AREA = 20
MAX_AREA_RATIO = 0.6

def preprocess_image(img_path):
    """
    预处理图片：二值化 + 反色 + 连通域标记
    
    Args:
        img_path: 图片路径
    
    Returns:
        binary_img: 二值化反色后的图 (255=数字, 0=背景)
        labeled_array: 标号后的图
        num_features: 连通域个数
    """
    img = Image.open(img_path).convert("L")
    img = np.array(img)
    
    # 二值化
    binary_img = np.where(img > THRESHOLD, 255, 0).astype(np.uint8)
    
    # 反色：让数字变成白色(255)，背景变黑色(0)
    binary_img = 255 - binary_img
    
    # 连通域标记
    labeled_array, num_features = ndimage.label(binary_img)
    
    return binary_img, labeled_array, num_features


def get_digit_regions(binary_img, labeled_array):
    """
    获取所有连通域的信息
    
    Args:
        binary_img: 二值化的图
        labeled_array: 标号后的图
    
    Returns:
        digits: [(x_pos, digit_arr), ...] 已排序
    """
    bboxes = ndimage.find_objects(labeled_array)
    
    digits = []
    
    for bbox in bboxes:
        if bbox is None:
            continue
        
        y_slice, x_slice = bbox
        digit_region = binary_img[y_slice, x_slice]
        
        # 过滤太小或太大的区域
        area = np.sum(digit_region > 0)
        max_area = binary_img.size * MAX_AREA_RATIO
        
        if area < MIN_AREA or area > max_area:
            continue
        
        # 处理成28×28
        digit_arr = process_digit_region(digit_region)
        
        x_pos = x_slice.start
        digits.append((x_pos, digit_arr))
    
    # 按 x 坐标排序
    digits.sort(key=lambda x: x[0])
    
    return digits


def process_digit_region(digit_region):
    """
    处理单个数字区域：等比缩放 + 填充 → 28×28
    
    Args:
        digit_region: 单个数字的二值化区域
    
    Returns:
        digit_arr: 28×28 的数组
    """
    height, width = digit_region.shape
    aspect_ratio = width / height
    
    # 计算目标尺寸（保持宽高比）
    if aspect_ratio > 1:
        target_width = TARGET_SIZE
        target_height = int(TARGET_SIZE / aspect_ratio)
    else:
        target_height = TARGET_SIZE
        target_width = int(TARGET_SIZE * aspect_ratio)
    
    # 缩放
    digit_img = Image.fromarray(digit_region).resize(
        (target_width, target_height),
        Image.BILINEAR
    )
    
    # 加padding
    digit_arr = np.zeros((28 + PADDING * 2, 28 + PADDING * 2), dtype=np.uint8)
    y_offset = (28 + PADDING * 2 - target_height) // 2
    x_offset = (28 + PADDING * 2 - target_width) // 2
    digit_arr[y_offset:y_offset+target_height, x_offset:x_offset+target_width] = np.array(digit_img)
    
    # 裁剪回28×28
    digit_arr = digit_arr[PADDING:PADDING+28, PADDING:PADDING+28]
    
    return digit_arr


def normalize_digit(digit_arr):
    """
    归一化数字（0-1范围）
    
    Args:
        digit_arr: 28×28 的uint8数组
    
    Returns:
        归一化后的float32数组，形状 (28, 28, 1)
    """
    digit_norm = digit_arr / 255.0
    digit_batch = digit_norm.reshape(28, 28, 1).astype("float32")
    return digit_batch