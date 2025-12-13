# predict.py
import tensorflow as tf
import numpy as np
from PIL import Image
import sys
import os
from image_utils import preprocess_image, get_digit_regions, normalize_digit

# 1. Read command-line arguments
if len(sys.argv) < 2:
    print("ERR_NO_INPUT")
    sys.exit(1)

img_path = sys.argv[1]

if not os.path.exists(img_path):
    print("ERR_FILE_NOT_FOUND")
    sys.exit(1)

# 2. Load model
model = tf.saved_model.load("mnist_model")

# 3. Preprocess image
binary_img, labeled_array, num_features = preprocess_image(img_path)

if num_features == 0:
    print("ERR_NO_DIGITS")
    sys.exit(1)

# 4. Determine workflow based on number of connected components
if num_features == 1:
    # Single digit workflow + centering
    print("[DEBUG] Single digit detected, applying centering...", file=__import__('sys').stderr)

    # Get digit region
    digits = get_digit_regions(binary_img, labeled_array)

    if len(digits) != 1:
        print("ERR_NO_VALID_DIGITS")
        sys.exit(1)

    _, digit_arr = digits[0]

    # Centering
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

    # Normalize + predict
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
    # Multi-digit workflow (no centering)
    print(f"{num_features} digits detected, processing separately...", file=__import__('sys').stderr)

    # Get bounding boxes for each connected component
    digits = get_digit_regions(binary_img, labeled_array)

    if len(digits) == 0:
        print("ERR_NO_VALID_DIGITS")
        sys.exit(1)

    # Batch prediction
    digit_arrays = []
    for _, digit_arr in digits:
        digit_norm = normalize_digit(digit_arr)  # already reshaped to (28, 28, 1)
        digit_arrays.append(digit_norm)

    # Stack into a single array
    digit_batch = np.stack(digit_arrays).astype("float32")  # shape (N, 28, 28, 1)

    # Predict all at once
    infer = model.signatures["serving_default"]
    output_key = list(infer.structured_outputs.keys())[0]

    predictions = infer(tf.constant(digit_batch))[output_key]

    # Extract results
    results = []
    confidences = []
    for prediction in predictions:
        predicted_digit = int(np.argmax(prediction))
        confidence = float(np.max(prediction)) * 100
        results.append(str(predicted_digit))
        confidences.append(f"{confidence:.1f}")

    # Output results
    if results:
        print("".join(results))
        print(f"CONFIDENCES:{','.join(confidences)}")
    else:
        print("ERR_NO_VALID_DIGITS")