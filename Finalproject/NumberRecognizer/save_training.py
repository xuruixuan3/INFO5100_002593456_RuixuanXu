# save_training.py - 保存训练数据
import numpy as np
from PIL import Image
import sys
import os
import hashlib
from image_utils import preprocess_image, get_digit_regions

# 1. 读取命令参数
if len(sys.argv) < 3:
    print("ERR_INVALID_ARGS")
    sys.exit(1)

img_path = sys.argv[1]
correct_labels_str = sys.argv[2]  # 用户输入的正确数字

if not os.path.exists(img_path):
    print("ERR_FILE_NOT_FOUND")
    sys.exit(1)

# 2. 预处理图片
binary_img, labeled_array, num_features = preprocess_image(img_path)

if num_features == 0:
    print("ERR_NO_DIGITS")
    sys.exit(1)

# 3. 验证位数是否匹配
correct_labels = list(correct_labels_str)  # ['5', '7']

if len(correct_labels) != num_features:
    print(f"ERR_DIGIT_COUNT_MISMATCH: detected {num_features} digits, got {len(correct_labels)} labels")
    sys.exit(1)

# 4. 获取数字区域
digits = get_digit_regions(binary_img, labeled_array)

if len(digits) == 0:
    print("ERR_NO_VALID_DIGITS")
    sys.exit(1)

# 5. 处理每一位数字并保存
saved_count = 0

for order, (_, digit_arr) in enumerate(digits):
    # 获取对应的标签
    correct_label = correct_labels[order]
    
    # 验证标签是否是单个数字
    if not correct_label.isdigit():
        print(f"ERR_INVALID_LABEL: {correct_label}")
        sys.exit(1)
    
    # 保存到对应文件夹
    output_dir = f"labeled_data/{correct_label}"
    os.makedirs(output_dir, exist_ok=True)
    
    # 生成唯一文件名（用原图路径 + 位置 + 标签生成hash）
    file_hash = hashlib.md5(f"{img_path}_{order}_{correct_label}".encode()).hexdigest()[:8]
    output_path = f"{output_dir}/{file_hash}.png"
    
    # 保存
    Image.fromarray(digit_arr).save(output_path)
    print(f"Saved digit {order} (label={correct_label}): {output_path}")
    saved_count += 1

# 6. 输出结果
if saved_count > 0:
    print(f"Successfully saved {saved_count} digit(s)")
else:
    print("ERR_NO_VALID_DIGITS")