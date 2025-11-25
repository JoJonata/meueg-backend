package br.ueg.meueg.controller;

import br.ueg.meueg.dto.VendaDTO;
import br.ueg.meueg.dto.VendaItemDTO;
import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
import br.ueg.meueg.entity.Venda;
import br.ueg.meueg.entity.VendaItem;
import br.ueg.meueg.enums.FormaPagamento;
import br.ueg.meueg.service.VendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/vendas")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class VendaController {

    private final VendaService vendaService;

    // --- ENDPOINTS PADRÃO ---

    @GetMapping
    @Operation(summary = "Lista todas as vendas")
    public ResponseEntity<List<VendaDTO>> getAllVendas() {
        List<VendaDTO> dtos = vendaService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma venda por ID")
    public ResponseEntity<VendaDTO> getVendaById(@PathVariable Long id) {
        return vendaService.findById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova venda (Manual ou Confirmada)")
    public ResponseEntity<VendaDTO> createVenda(@RequestBody VendaDTO dto) {
        Venda venda = toEntity(dto);
        Venda savedVenda = vendaService.save(venda);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(savedVenda));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza a forma de pagamento e o usuário de uma venda existente")
    public ResponseEntity<VendaDTO> updateVenda(@PathVariable Long id, @RequestBody VendaDTO dto) {
        User user = User.builder().id(dto.getIdUsuario()).build();
        FormaPagamento formaPagamento = FormaPagamento.valueOf(dto.getForma_pagamento());
        Venda venda = Venda.builder()
                .usuario(user)
                .forma_pagamento(formaPagamento)
                .build();
        Venda updatedVenda = vendaService.update(id, venda);
        return ResponseEntity.ok(toDTO(updatedVenda));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta uma venda por ID e reverte os itens para o estoque")
    public ResponseEntity<Void> deleteVenda(@PathVariable Long id) {
        vendaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINTS DE VOZ (FLUXO NOVO) ---

    /**
     * PASSO 1: SIMULAÇÃO
     * Recebe o texto da IA (ex: "pipoca"), busca preços e retorna a proposta.
     * NÃO SALVA NO BANCO AINDA.
     */
    @PostMapping("/simular-venda-voz") // No Flutter, aponte para este endpoint primeiro
    @Operation(summary = "Recebe itens da IA, busca preços e retorna prévia (sem salvar)")
    public ResponseEntity<VendaDTO> simularVendaVoz(@RequestBody VendaDTO dto) {
        // Chama o método que apenas calcula e preenche os dados (Service alterado no passo anterior)
        VendaDTO proposta = vendaService.processarPropostaVoz(dto);
        return ResponseEntity.ok(proposta);
    }

    /**
     * PASSO 2: CONFIRMAÇÃO
     * Recebe a proposta confirmada pelo usuário e SALVA de verdade.
     */
    @PostMapping("/confirmar-venda-voz")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Finaliza e salva uma venda previamente processada por voz.")
    public ResponseEntity<VendaDTO> confirmarVendaVoz(@RequestBody VendaDTO dto) {
        // 1. Converte o DTO (que agora tem preços e IDs certos) para Entidade
        Venda venda = toEntity(dto);

        // 2. Chama o SAVE real (que baixa estoque e salva no banco)
        // OBS: Não use 'processarPropostaVoz' aqui, use 'save'!
        Venda savedVenda = vendaService.save(venda);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(savedVenda));
    }

    // --- MÉTODOS AUXILIARES (CONVERSÃO) ---

    private VendaDTO toDTO(Venda entity) {
        VendaDTO dto = new VendaDTO();
        dto.setId(entity.getId_venda());
        dto.setData(entity.getData());
        dto.setValor_total(entity.getValor_total());
        dto.setForma_pagamento(entity.getForma_pagamento().toString());
        dto.setIdUsuario(entity.getUsuario() != null ? entity.getUsuario().getId() : null);
        dto.setUsernameUsuario(entity.getUsuario() != null ? entity.getUsuario().getUsername() : null);

        if (entity.getItens() != null) {
            dto.setItens(entity.getItens().stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private VendaItemDTO toItemDTO(VendaItem itemEntity) {
        VendaItemDTO itemDto = new VendaItemDTO();
        itemDto.setId(itemEntity.getId());
        itemDto.setIdProduto(itemEntity.getProdutoServico() != null ? itemEntity.getProdutoServico().getId() : null);
        itemDto.setNomeProduto(itemEntity.getProdutoServico() != null ? itemEntity.getProdutoServico().getNome() : null);
        itemDto.setQuantidade(itemEntity.getQuantidade());
        itemDto.setPrecoUnitario(itemEntity.getPrecoUnitario());
        return itemDto;
    }

    private Venda toEntity(VendaDTO dto) {
        User usuario = null;
        if (dto.getIdUsuario() != null) {
            usuario = User.builder().id(dto.getIdUsuario()).build();
        }

        FormaPagamento formaPagamento = null;
        if(dto.getForma_pagamento() != null) {
            try {
                formaPagamento = FormaPagamento.valueOf(dto.getForma_pagamento());
            } catch (Exception e) {
                formaPagamento = FormaPagamento.DINHEIRO; // Fallback
            }
        }

        Venda venda = Venda.builder()
                .id_venda(dto.getId())
                .forma_pagamento(formaPagamento)
                .usuario(usuario)
                .build();

        if (dto.getItens() != null) {
            List<VendaItem> itens = dto.getItens().stream()
                    .map(itemDto -> toItemEntity(itemDto, venda))
                    .collect(Collectors.toList());
            venda.setItens(itens);
        }
        return venda;
    }

    private VendaItem toItemEntity(VendaItemDTO itemDto, Venda venda) {
        ProdutoServico produtoServico = null;
        if (itemDto.getIdProduto() != null) {
            produtoServico = ProdutoServico.builder().id(itemDto.getIdProduto()).build();
        }
        return VendaItem.builder()
                .id(itemDto.getId())
                .venda(venda)
                .produtoServico(produtoServico)
                .quantidade(itemDto.getQuantidade())
                .precoUnitario(itemDto.getPrecoUnitario())
                .build();
    }
}