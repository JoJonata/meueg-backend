import os
from flask import Flask, request, jsonify
from vosk import Model, KaldiRecognizer
import wave
import json

app = Flask(__name__)

# --- ATENÇÃO: AJUSTE O NOME DA PASTA DO SEU MODELO AQUI ---
#MODEL_PATH = "vosk-model-small-pt-0.3"
MODEL_PATH = r"C:\vosk-model-pt-fb-v0.1.1-20220516_2113"
# Verifique se o modelo existe
if not os.path.exists(MODEL_PATH):
    print(f"Erro: O modelo Vosk não foi encontrado em {MODEL_PATH}")
    exit(1)

model = Model(MODEL_PATH)
print("Modelo Vosk carregado com sucesso.")

@app.route('/transcribe', methods=['POST'])
def transcribe_audio():
    if 'audioFile' not in request.files:
        return jsonify({'error': 'Nenhum arquivo de áudio enviado.'}), 400

    audio_file = request.files['audioFile']
    temp_audio_path = 'temp_audio.wav'
    audio_file.save(temp_audio_path)

    try:
        # A melhor prática: use 'with' para garantir que o arquivo seja fechado automaticamente.
        with wave.open(temp_audio_path, "rb") as wf:
            if wf.getnchannels() != 1 or wf.getsampwidth() != 2 or wf.getcomptype() != "NONE":
                return jsonify({'error': "O arquivo de áudio deve ser mono, 16 bits PCM, e sem compressão (WAV)."}), 400

            recognizer = KaldiRecognizer(model, wf.getframerate())
            audio_data = wf.readframes(wf.getnframes())
            recognizer.AcceptWaveform(audio_data)

            result = recognizer.FinalResult()
            transcription_text = json.loads(result)["text"]

        # O arquivo 'temp_audio.wav' já foi fechado pelo bloco 'with'. Agora, é seguro removê-lo.
        os.remove(temp_audio_path)

        return jsonify({'transcription': transcription_text})

    except Exception as e:
        # Este bloco lida com erros de processamento e garante a limpeza.
        if os.path.exists(temp_audio_path):
            os.remove(temp_audio_path)
        return jsonify({'error': f"Ocorreu um erro no processamento: {str(e)}"}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5567)