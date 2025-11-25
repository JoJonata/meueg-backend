package br.ueg.meueg.service;

import java.io.File;
public interface SpeechToTextService {
    String transcrever(File audioFile) throws Exception;
}
