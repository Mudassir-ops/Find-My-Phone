# File: src/main/python/clap_detector.py

import numpy as np
import time

last_clap_time = 0
double_clap_window = 0.2  # seconds


def detect_clap(
        samples,
        threshold=0.32,
        noise_floor=0.002,
        min_rms=0.014,
        max_rms=0.22,
        min_peak_to_rms_ratio=4.0
):
    global last_clap_time

    samples = np.array(samples, dtype=np.float32)
    rms = np.sqrt(np.mean(np.square(samples)))
    peak = np.max(np.abs(samples))
    dynamic_peak_threshold = max(threshold, rms * 2) if rms >= noise_floor else threshold
    peak_to_rms_ratio = peak / (rms + 1e-8)
    print(
        f"[PYTHON] peak={peak:.2f}, rms={rms:.2f}, ratio={peak_to_rms_ratio:.2f}, thr={dynamic_peak_threshold:.2f}")

    current_time = time.time()

    if (
            peak > dynamic_peak_threshold and
            min_rms <= rms <= max_rms and
            peak_to_rms_ratio >= min_peak_to_rms_ratio
    ):
        if current_time - last_clap_time < double_clap_window:
            last_clap_time = 0  # reset to prevent triple detection
            return f"Double Clap Detected! (Δt={current_time - last_clap_time:.2f}s)"
        else:
            last_clap_time = current_time
            print("[PYTHON] Single clap detected, waiting for second...")
    return None

# def detect_clap(samples, threshold=0.4, noise_floor=0.02, min_rms=0.02, max_rms=0.25):
#     samples = np.array(samples, dtype=np.float32)
#
#     rms = np.sqrt(np.mean(np.square(samples)))
#     peak = np.max(np.abs(samples))
#
#     # Dynamic threshold for peak (relative to noise)
#     dynamic_peak_threshold = max(threshold, rms * 2) if rms >= noise_floor else threshold
#
#     print(f"[PYTHON] peak={peak}, rms={rms}, dynamic_peak_threshold={dynamic_peak_threshold}")
#
#     # New refined logic
#     if (
#             peak > dynamic_peak_threshold and  # high peak
#             min_rms <= rms <= max_rms  # moderate rms (filtering out background noise & speech)
#     ):
#         return f"Clap detected! (Peak: {peak:.2f}, RMS: {rms:.2f}, Thr: {dynamic_peak_threshold:.2f})"
#     else:
#         return None
