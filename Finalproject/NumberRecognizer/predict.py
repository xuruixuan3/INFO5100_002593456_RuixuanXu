# predict.py 
import tensorflow as tf
import numpy as np
from PIL import Image
import sys
import os
from image_utils import preprocess_image, get_digit_regions, normalize_digit

# 1. 读取命令参数
if len(sys.argv) < 2:
    print("ERR_NO_INPUT")
    sys.exit(1)

img_path = sys.argv[1]

if not os.path.exists(img_path):
    print("ERR_FILE_NOT_FOUND")
    sys.exit(1)

# 2. 加载模型
model = tf.saved_model.load("mnist_model")

# 3. 预处理图片
binary_img, labeled_array, num_features = preprocess_image(img_path)

if num_features == 0:
    print("ERR_NO_DIGITS")
    sys.exit(1)

# 4. 根据连通域个数判断流程
if num_features == 1:
    # 单数字流程 + 中心化 
    print("[DEBUG] Single digit detected, applying centering...", file=__import__('sys').stderr)
    
    # 获取数字区域
    digits = get_digit_regions(binary_img, labeled_array)

    if len(digits) != 1:
        print("ERR_NO_VALID_DIGITS")
        sys.exit(1)

    _, digit_arr = digits[0]

    # 中心化处理
    coords = np.argwhere(digit_arr > 127)
    if len(coords) > 0:
        cy, cx = coords.mean(axis=0)
        dy = int(14 - cy)
        dx = int(14 - cx)
        
        digit_centered = np.zeros((28, 28), dtype=np.uint8)
        for y in range(28):
            for x in range(28):
                ny, nx = y + dy, x + dx
                if 0 <= ny < 28 and 0 <= nx < 28:
                    digit_centered[ny, nx] = digit_arr[y, x]
        digit_arr = digit_centered
    
    # 归一化 + 预测
    digit_norm = digit_arr / 255.0
    digit_batch = digit_norm.reshape(1, 28, 28, 1).astype("float32")
    
    infer = model.signatures["serving_default"]
    output_key = list(infer.structured_outputs.keys())[0]
    prediction = infer(tf.constant(digit_batch))[output_key]
    predicted_number = int(np.argmax(prediction))
    confidence = float(np.max(prediction)) * 100  

    print(predicted_number)
    print(f"CONFIDENCES:{confidence:.1f}") 

else:
    # 多位数流程（不做中心化）
    print(f"{num_features} digits detected, processing separately...", file=__import__('sys').stderr)
    
    # 获取每个连通域的边界框
    digits = get_digit_regions(binary_img, labeled_array)

    if len(digits) == 0:
        print("ERR_NO_VALID_DIGITS")
        sys.exit(1)
    
    # 批量预测
    digit_arrays = []
    for _, digit_arr in digits:
        digit_norm = normalize_digit(digit_arr)  # 这里已经reshape成(28,28,1)了
        digit_arrays.append(digit_norm)

    # 堆成一个大数组
    digit_batch = np.stack(digit_arrays).astype("float32")  # 直接是(N,28,28,1)

    # 一次性预测全部
    infer = model.signatures["serving_default"]
    output_key = list(infer.structured_outputs.keys())[0]

    predictions = infer(tf.constant(digit_batch))[output_key]

    # 提取结果
    results = []
    confidences = [] 
    for prediction in predictions:
        predicted_digit = int(np.argmax(prediction))
        confidence = float(np.max(prediction)) * 100 
        results.append(str(predicted_digit))
        confidences.append(f"{confidence:.1f}")  
    # 输出结果
    if results:
        print("".join(results))
        print(f"CONFIDENCES:{','.join(confidences)}")  
    else:
        print("ERR_NO_VALID_DIGITS")