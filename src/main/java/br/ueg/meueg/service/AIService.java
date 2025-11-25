package br.ueg.meueg.service;

import br.ueg.meueg.dto.VendaDTO;

import java.util.Optional;

public interface AIService {
    Optional<VendaDTO> processVoiceCommand(String transcribedText, Long userId);
}