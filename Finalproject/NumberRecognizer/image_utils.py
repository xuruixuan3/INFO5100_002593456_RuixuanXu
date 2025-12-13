# image_utils.py - unified image processing functions
import numpy as np
from PIL import Image
from scipy import ndimage

# Preprocessing parameters
THRESHOLD = 127
PADDING = 5
TARGET_SIZE = 24
MIN_AREA = 20
MAX_AREA_RATIO = 0.6

def preprocess_image(img_path):
    """
    Preprocess image: binarization + invert colors + connected-component labeling

    Args:
        img_path: image path

    Returns:
        binary_img: binarized and inverted image (255=digit, 0=background)
        labeled_array: labeled image
        num_features: number of connected components
    """
    img = Image.open(img_path).convert("L")
    img = np.array(img)

    # Binarization
    binary_img = np.where(img > THRESHOLD, 255, 0).astype(np.uint8)

    # Invert colors: make digits white (255) and background black (0)
    binary_img = 255 - binary_img

    # Connected-component labeling
    labeled_array, num_features = ndimage.label(binary_img)

    return binary_img, labeled_array, num_features


def get_digit_regions(binary_img, labeled_array):
    """
    Get information of all connected components

    Args:
        binary_img: binarized image
        labeled_array: labeled image

    Returns:
        digits: [(x_pos, digit_arr), ...] sorted
    """
    bboxes = ndimage.find_objects(labeled_array)

    digits = []

    for bbox in bboxes:
        if bbox is None:
            continue

        y_slice, x_slice = bbox
        digit_region = binary_img[y_slice, x_slice]

        # Filter regions that are too small or too large
        area = np.sum(digit_region > 0)
        max_area = binary_img.size * MAX_AREA_RATIO

        if area < MIN_AREA or area > max_area:
            continue

        # Process to 28×28
        digit_arr = process_digit_region(digit_region)

        x_pos = x_slice.start
        digits.append((x_pos, digit_arr))

    # Sort by x coordinate
    digits.sort(key=lambda x: x[0])

    return digits


def process_digit_region(digit_region):
    """
    Process a single digit region: scale with aspect ratio + padding -> 28×28

    Args:
        digit_region: binarized region of a single digit

    Returns:
        digit_arr: 28×28 array
    """
    height, width = digit_region.shape
    aspect_ratio = width / height

    # Compute target size (keep aspect ratio)
    if aspect_ratio > 1:
        target_width = TARGET_SIZE
        target_height = int(TARGET_SIZE / aspect_ratio)
    else:
        target_height = TARGET_SIZE
        target_width = int(TARGET_SIZE * aspect_ratio)

    # Resize
    digit_img = Image.fromarray(digit_region).resize(
        (target_width, target_height),
        Image.BILINEAR
    )

    # Add padding
    digit_arr = np.zeros((28 + PADDING * 2, 28 + PADDING * 2), dtype=np.uint8)
    y_offset = (28 + PADDING * 2 - target_height) // 2
    x_offset = (28 + PADDING * 2 - target_width) // 2
    digit_arr[y_offset:y_offset+target_height, x_offset:x_offset+target_width] = np.array(digit_img)

    # Crop back to 28×28
    digit_arr = digit_arr[PADDING:PADDING+28, PADDING:PADDING+28]

    return digit_arr


def normalize_digit(digit_arr):
    """
    Normalize digit (range 0-1)

    Args:
        digit_arr: 28×28 uint8 array

    Returns:
        normalized float32 array with shape (28, 28, 1)
    """
    digit_norm = digit_arr / 255.0
    digit_batch = digit_norm.reshape(28, 28, 1).astype("float32")
    return digit_batch