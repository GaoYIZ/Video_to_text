import argparse
import json
import os
import wave
from pathlib import Path

from vosk import Model, KaldiRecognizer


def parse_args():
    parser = argparse.ArgumentParser(description="Local ASR transcription for VideoAI using Vosk")
    parser.add_argument("--input", required=True, help="Path to input audio file")
    parser.add_argument("--output", required=True, help="Path to output json file")
    parser.add_argument("--model", required=True, help="Path to Vosk model directory")
    parser.add_argument("--language", default="zh-cn", help="Language hint (zh-cn for Chinese)")
    return parser.parse_args()


def main():
    args = parse_args()
    input_path = Path(args.input)
    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    if not input_path.exists():
        raise FileNotFoundError(f"Input audio file not found: {input_path}")

    # Load Vosk model
    model_path = args.model
    if not os.path.exists(model_path):
        raise FileNotFoundError(f"Vosk model directory not found: {model_path}")
    
    print(f"Loading Vosk model from: {model_path}")
    model = Model(model_path)
    
    # Open audio file
    wf = wave.open(str(input_path), "rb")
    sample_rate = wf.getframerate()
    channels = wf.getnchannels()
    
    # Vosk requires mono 16-bit PCM
    if channels != 1 or wf.getsampwidth() != 2:
        raise ValueError(f"Audio must be mono 16-bit PCM, got {channels} channels, {wf.getsampwidth()*8}-bit")
    
    print(f"Processing audio: {sample_rate}Hz, {channels} channel(s)")
    
    # Create recognizer
    rec = KaldiRecognizer(model, sample_rate)
    rec.SetWords(True)
    
    # Process audio in chunks
    transcript_segments = []
    transcript_lines = []
    current_time_ms = 0
    chunk_duration_ms = 5000  # 5 seconds per chunk for timing estimation
    
    while True:
        data = wf.readframes(4000)  # Read ~250ms at 16kHz
        if len(data) == 0:
            break
        
        if rec.AcceptWaveform(data):
            result = json.loads(rec.Result())
            if "result" in result and result["result"]:
                for word in result["result"]:
                    text = word.get("word", "").strip()
                    if text:
                        start_ms = int(word.get("start", 0) * 1000)
                        end_ms = int(word.get("end", 0) * 1000)
                        transcript_segments.append({
                            "index": len(transcript_segments),
                            "startTimeMs": start_ms,
                            "endTimeMs": end_ms,
                            "content": text
                        })
                        transcript_lines.append(text)
    
    # Get final result
    final_result = json.loads(rec.FinalResult())
    if "text" in final_result and final_result["text"].strip():
        # If we have partial text without word-level timing
        text = final_result["text"].strip()
        if transcript_segments:
            last_end = transcript_segments[-1]["endTimeMs"]
        else:
            last_end = 0
        transcript_lines.append(text)
    
    wf.close()
    
    # Calculate duration
    duration_ms = transcript_segments[-1]["endTimeMs"] if transcript_segments else 0
    
    payload = {
        "language": args.language,
        "durationMs": duration_ms,
        "transcriptText": " ".join(transcript_lines) if transcript_lines else "",
        "segments": transcript_segments,
    }
    
    output_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Transcription complete. Output written to: {output_path}")
    print(f"Segments: {len(transcript_segments)}, Duration: {duration_ms}ms")


if __name__ == "__main__":
    main()
