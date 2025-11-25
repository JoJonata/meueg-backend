package br.ueg.meueg.service.impl;

import br.ueg.meueg.service.SpeechToTextService;
import com.google.cloud.speech.v1.*;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechSettings;

@Service
public class SpeechToTextServiceImpl implements SpeechToTextService {

    @Value("${google.api.credential.path}")
    private String googleCredentialsPath;

    @Override
    public String transcrever(File audioFile) throws IOException {
        // Obtenha os bytes do arquivo de áudio.
        byte[] audioBytes = Files.readAllBytes(audioFile.toPath());

        try (FileInputStream credentialsStream = new FileInputStream(googleCredentialsPath.replace("classpath:", "src/main/resources/"));
             SpeechClient speechClient = SpeechClient.create(
                     SpeechSettings.newBuilder().setCredentialsProvider(
                             FixedCredentialsProvider.create(GoogleCredentials.fromStream(credentialsStream))).build())) {

            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(audioBytes)).build();
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16) // Ajuste para o formato do seu áudio
                    .setLanguageCode("pt-BR")
                    .build();

            RecognizeResponse response = speechClient.recognize(config, audio);

            if (response.getResultsCount() > 0) {
                SpeechRecognitionResult result = response.getResults(0);
                if (result.getAlternativesCount() > 0) {
                    return result.getAlternatives(0).getTranscript();
                }
            }
        }
        return null; // Retorne null se a transcrição falhar
    }
}