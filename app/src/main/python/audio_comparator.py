
import wave
import numpy as np


def load_wave_file(path):
    with wave.open(path, 'rb') as wf:
        n_frames = wf.getnframes()
        raw = wf.readframes(n_frames)
        audio_data = np.frombuffer(raw, dtype=np.int16)
        if wf.getnchannels() > 1:
            audio_data = audio_data[::wf.getnchannels()]  # mono
        return audio_data, wf.getframerate()


def compare_audio(file1_path, file2_path, threshold=500):
    y1, sr1 = load_wave_file(file1_path)
    y2, sr2 = load_wave_file(file2_path)

    if sr1 != sr2:
        return "Sample rates do not match"

    min_len = min(len(y1), len(y2))
    y1 = y1[:min_len]
    y2 = y2[:min_len]

    mse = np.mean((y1 - y2) ** 2)
    return mse < threshold
