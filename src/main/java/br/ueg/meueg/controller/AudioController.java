package br.ueg.meueg.controller;

import br.ueg.meueg.dto.VendaDTO;
import br.ueg.meueg.entity.User;
import br.ueg.meueg.exception.BusinessException;
import br.ueg.meueg.repository.UserRepository;
import br.ueg.meueg.service.AIService;
import br.ueg.meueg.service.SpeechToTextService;
import br.ueg.meueg.service.VendaService; // Importante
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/voz")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class AudioController {

    private final SpeechToTextService speechToTextService;
    private final VendaService vendaService; // <--- Certifique-se que o Service com a correção está injetado aqui
    private final AIService aiService;
    private final UserRepository userRepository;

    @PostMapping(value = "/processar-venda", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Transcreve áudio, processa a IA e retorna a PROPOSTA de venda (Não Salva).")
    public ResponseEntity<VendaDTO> processarVenda(@RequestParam("audioFile") MultipartFile audioFile, Principal principal) {
        if (audioFile.isEmpty()) {
            throw new BusinessException("Nenhum arquivo de áudio enviado.");
        }

        String username = principal.getName();
        User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado."));
        Long userId = userEntity.getId();

        File tempAudioFile = null;
        try {
            tempAudioFile = File.createTempFile("audio", ".wav");
            try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                fos.write(audioFile.getBytes());
            }

            // 1. Transcrição (Áudio -> Texto)
            String transcricao = speechToTextService.transcrever(tempAudioFile);
            if (transcricao == null || transcricao.isEmpty()) {
                throw new BusinessException("Não foi possível transcrever o áudio.");
            }

            // 2. Inteligência Artificial (Texto -> Itens brutos)
            // A IA identifica "pipoca" e "2", mas não sabe preço nem ID.
            Optional<VendaDTO> vendaDTOOptional = aiService.processVoiceCommand(transcricao, userId);

            if (vendaDTOOptional.isEmpty()) {
                throw new BusinessException("A IA não conseguiu entender o comando.");
            }

            VendaDTO vendaDTO = vendaDTOOptional.get();

            // Define o ID do usuário para a busca no banco funcionar
            vendaDTO.setIdUsuario(userId);

            // --- A MÁGICA ACONTECE AQUI (A CORREÇÃO) ---
            // Chamamos o VendaService para pegar os preços, IDs e corrigir maiúsculas/minúsculas
            VendaDTO propostaCalculada = vendaService.processarPropostaVoz(vendaDTO);

            // 3. Retorna a proposta JÁ CALCULADA para o Flutter
            return ResponseEntity.ok(propostaCalculada);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (tempAudioFile != null) {
                tempAudioFile.delete();
            }
        }
    }
}