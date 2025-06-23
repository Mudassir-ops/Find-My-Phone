import librosa
import numpy as np
from dtw import dtw

def load_and_preprocess(path):
    y, sr = librosa.load(path, sr=16000)
    y = y / np.max(np.abs(y))  # Normalize
    y, _ = librosa.effects.trim(y)  # Remove silence
    return y, sr

def compare_mfcc(file1, file2, threshold=200):
    y1, sr1 = load_and_preprocess(file1)
    y2, sr2 = load_and_preprocess(file2)

    mfcc1 = librosa.feature.mfcc(y=y1, sr=sr1, n_mfcc=13)
    mfcc2 = librosa.feature.mfcc(y=y2, sr=sr2, n_mfcc=13)

    distance, _, _, _ = dtw(mfcc1.T, mfcc2.T)
    print("DTW distance:", distance)
    return distance < threshold


# import wave
# import numpy as np
# from python_speech_features import mfcc
# from scipy.spatial.distance import euclidean
# from fastdtw import fastdtw
#
#
# def read_wave(path):
#     with wave.open(path, 'rb') as wf:
#         framerate = wf.getframerate()
#         frames = wf.readframes(wf.getnframes())
#         data = np.frombuffer(frames, dtype=np.int16)
#         if wf.getnchannels() > 1:
#             data = data[::wf.getnchannels()]
#         return data, framerate
#
#
# def compare_mfcc(file1, file2, threshold=200):
#     sig1, rate1 = read_wave(file1)
#     sig2, rate2 = read_wave(file2)
#
#     mfcc1 = mfcc(sig1, samplerate=rate1)
#     mfcc2 = mfcc(sig2, samplerate=rate2)
#
#     distance, _ = fastdtw(mfcc1, mfcc2, dist=euclidean)
#
#     return distance < threshold
