package br.ueg.meueg.service.impl;

import br.ueg.meueg.dto.VendaDTO;
import br.ueg.meueg.dto.VendaItemDTO;
import br.ueg.meueg.repository.ProdutoServicoRepository;
import br.ueg.meueg.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ProdutoServicoRepository produtoServicoRepository;
    private final String OLLAMA_URL = "http://localhost:11434/api/generate";

    @Override
    public Optional<VendaDTO> processVoiceCommand(String transcribedText, Long userId) {
        // 1. Busca produtos do banco para dar contexto à IA
        List<String> productNames = produtoServicoRepository.findAllNomesByUserId(userId);
        String productList = productNames.stream().collect(Collectors.joining(", "));

        String prompt = String.format("""
            Atue como um interpretador de pedidos. Analise a frase e extraia: Produto, Quantidade e Forma de Pagamento.
            
            REGRAS DE PRODUTO:
            - O nome deve ser similar a um destes: [%s].
            
            REGRAS DE PAGAMENTO (Mapeie para o código exato):
            - Se falar "Pix" -> retorne "PIX"
            - Se falar "Crédito" ou "Cartão de Crédito" -> retorne "CARTAO_CREDITO"
            - Se falar "Débito" ou "Cartão de Débito" -> retorne "CARTAO_DEBITO"
            - Se falar "Dinheiro" ou não falar nada -> retorne null (será padrão dinheiro).
            
            FORMATO DE RESPOSTA (JSON):
            {
                "product": "nome do produto",
                "quantity": 1,
                "payment": "CODIGO_DO_PAGAMENTO"
            }
            
            Frase do usuário: "%s"
            """, productList, transcribedText);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "mistral"); // Ou o modelo que estiver usando
        requestBody.put("prompt", prompt);
        requestBody.put("format", "json");
        requestBody.put("stream", false); // Dica: false facilita o parse, vem tudo de uma vez

        try {
            // Tenta pegar a resposta direta (se stream=false)
            String iaRawResponse = restTemplate.postForObject(OLLAMA_URL, requestBody, String.class);

            // Lógica para limpar a resposta do Ollama e pegar apenas o texto gerado
            JsonNode rootResponse = objectMapper.readTree(iaRawResponse);
            String actualResponseText = rootResponse.get("response").asText();

            // 3. Parsear o JSON interno que a IA gerou
            Optional<VendaDTO> venda = parseVendaFromAIResponse(actualResponseText);
            venda.ifPresent(v -> v.setIdUsuario(userId));
            return venda;

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<VendaDTO> parseVendaFromAIResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            // Leitura segura dos campos
            JsonNode productNode = root.get("product");
            JsonNode quantityNode = root.get("quantity");
            JsonNode paymentNode = root.get("payment"); // Novo campo

            if (productNode != null && quantityNode != null) {
                String productName = productNode.asText();
                int quantity = quantityNode.asInt(1); // Default 1 se falhar

                VendaItemDTO itemDTO = new VendaItemDTO();
                itemDTO.setNomeProduto(productName);
                itemDTO.setQuantidade(quantity);

                VendaDTO vendaDTO = new VendaDTO();
                vendaDTO.setItens(Collections.singletonList(itemDTO));

                // AQUI PREENCHEMOS O PAGAMENTO NO DTO
                if (paymentNode != null && !paymentNode.isNull() && !paymentNode.asText().isEmpty()) {
                    String pgto = paymentNode.asText().toUpperCase();
                    // Pequena sanitização caso a IA mande algo fora do padrão
                    if (pgto.contains("CREDITO")) pgto = "CARTAO_CREDITO";
                    if (pgto.contains("DEBITO")) pgto = "CARTAO_DEBITO";

                    vendaDTO.setForma_pagamento(pgto);
                }

                return Optional.of(vendaDTO);
            }
        } catch (Exception e) {
            System.err.println("Erro ao converter JSON da IA: " + jsonResponse);
            e.printStackTrace();
        }
        return Optional.empty();
    }
}