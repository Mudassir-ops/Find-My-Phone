# File: src/main/python/clap_detector.py


import librosa
import numpy as np
from librosa.sequence import dtw


def compare_audio_mfcc(file1_path, file2_path, threshold=500):
    # Load audio files
    y1, sr1 = librosa.load(file1_path, sr=None)
    y2, sr2 = librosa.load(file2_path, sr=None)

    # Resample if sample rates differ
    if sr1 != sr2:
        sr = min(sr1, sr2)
        y1 = librosa.resample(y1, orig_sr=sr1, target_sr=sr)
        y2 = librosa.resample(y2, orig_sr=sr2, target_sr=sr)
    else:
        sr = sr1

    # Extract MFCC features
    mfcc1 = librosa.feature.mfcc(y=y1, sr=sr, n_mfcc=13)
    mfcc2 = librosa.feature.mfcc(y=y2, sr=sr, n_mfcc=13)

    # Compute Dynamic Time Warping (DTW) distance between MFCCs
    D, wp = dtw(mfcc1, mfcc2, subseq=True)
    distance = D[-1, -1]

    print(f"DTW distance: {distance}")
    return distance < threshold

# import wave
# import io
# import numpy as np
# from scipy.signal import butter, lfilter
#
#
# class ClapDetector:
#     def __init__(self, threshold_bias=6000, sample_rate=44100, lowcut=800, highcut=3000):
#         self.threshold_bias = threshold_bias
#         self.sample_rate = sample_rate
#         self.lowcut = lowcut
#         self.highcut = highcut
#
#     def bandpass_filter(self, data):
#         nyquist = 0.5 * self.sample_rate
#         low = self.lowcut / nyquist
#         high = self.highcut / nyquist
#         b, a = butter(3, [low, high], btype='band')
#         return lfilter(b, a, data)
#
#     def detect_claps(self, data):
#         data = self.bandpass_filter(data)
#
#         # Calculate short-time energy
#         frame_size = int(0.02 * self.sample_rate)  # 20ms window
#         step_size = int(0.01 * self.sample_rate)  # 10ms step
#         energies = [
#             np.sum(np.abs(data[i:i + frame_size]) ** 2)
#             for i in range(0, len(data) - frame_size, step_size)
#         ]
#
#         energies = np.array(energies)
#         peak_indices = np.where(energies > self.threshold_bias)[0]
#
#         if len(peak_indices) == 0:
#             return []
#
#         times = peak_indices * step_size / self.sample_rate
#         result = []
#         last_time = -1
#         for t in times:
#             if t - last_time > 0.3:  # 300ms debounce
#                 result.append(t)
#                 last_time = t
#         return result
#
#     def run(self, audio_bytes):
#         wf = wave.open(io.BytesIO(audio_bytes), 'rb')
#         num_frames = wf.getnframes()
#         self.sample_rate = wf.getframerate()
#         raw_audio = wf.readframes(num_frames)
#
#         audio_data = np.frombuffer(raw_audio, dtype=np.int16).astype(np.float32)
#
#         if wf.getnchannels() > 1:
#             audio_data = audio_data[::wf.getnchannels()]  # mono
#
#         return self.detect_claps(audio_data)

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
