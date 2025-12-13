# save_training.py - Save training data
import numpy as np
from PIL import Image
import sys
import os
import hashlib
from image_utils import preprocess_image, get_digit_regions

# 1. Read command-line arguments
if len(sys.argv) < 3:
    print("ERR_INVALID_ARGS")
    sys.exit(1)

img_path = sys.argv[1]
correct_labels_str = sys.argv[2]  # User input correct digit(s)

if not os.path.exists(img_path):
    print("ERR_FILE_NOT_FOUND")
    sys.exit(1)

# 2. Preprocess image
binary_img, labeled_array, num_features = preprocess_image(img_path)

if num_features == 0:
    print("ERR_NO_DIGITS")
    sys.exit(1)

# 3. Validate digit count matches
correct_labels = list(correct_labels_str)  # e.g., ['5', '7']

if len(correct_labels) != num_features:
    print(
        f"ERR_DIGIT_COUNT_MISMATCH: detected {num_features} digits, "
        f"got {len(correct_labels)} labels"
    )
    sys.exit(1)

# 4. Get digit regions
digits = get_digit_regions(binary_img, labeled_array)

if len(digits) == 0:
    print("ERR_NO_VALID_DIGITS")
    sys.exit(1)

# 5. Process and save each digit
saved_count = 0

for order, (_, digit_arr) in enumerate(digits):
    # Get corresponding label
    correct_label = correct_labels[order]

    # Validate label is a single digit
    if not correct_label.isdigit():
        print(f"ERR_INVALID_LABEL: {correct_label}")
        sys.exit(1)

    # Save to corresponding folder
    output_dir = f"labeled_data/{correct_label}"
    os.makedirs(output_dir, exist_ok=True)

    # Generate unique filename (hash based on image path + position + label)
    file_hash = hashlib.md5(
        f"{img_path}_{order}_{correct_label}".encode()
    ).hexdigest()[:8]
    output_path = f"{output_dir}/{file_hash}.png"

    # Save
    Image.fromarray(digit_arr).save(output_path)
    print(f"Saved digit {order} (label={correct_label}): {output_path}")
    saved_count += 1

# 6. Output result
if saved_count > 0:
    print(f"Successfully saved {saved_count} digit(s)")
else:
    print("ERR_NO_VALID_DIGITS")
