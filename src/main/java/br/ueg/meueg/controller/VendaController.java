package br.ueg.meueg.controller;

import br.ueg.meueg.dto.VendaDTO;
import br.ueg.meueg.dto.VendaItemDTO;
import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
import br.ueg.meueg.entity.Venda;
import br.ueg.meueg.entity.VendaItem;
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
    @Operation(summary = "Cria uma nova venda")
    public ResponseEntity<VendaDTO> createVenda(@RequestBody VendaDTO dto) {
        Venda venda = toEntity(dto);
        Venda savedVenda = vendaService.save(venda);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(savedVenda));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza a forma de pagamento e o usuário de uma venda existente")
    public ResponseEntity<VendaDTO> updateVenda(@PathVariable Long id, @RequestBody VendaDTO dto) {
        // A conversão para entidade aqui só precisa dos campos que podem ser atualizados
        User user = User.builder().id(dto.getIdUsuario()).build();
        Venda venda = Venda.builder()
                .usuario(user)
                .forma_pagamento(dto.getForma_pagamento())
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

    private VendaDTO toDTO(Venda entity) {
        VendaDTO dto = new VendaDTO();
        dto.setId(entity.getId_venda());
        dto.setData(entity.getData());
        dto.setValor_total(entity.getValor_total());
        dto.setForma_pagamento(entity.getForma_pagamento());
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

        Venda venda = Venda.builder()
                .id_venda(dto.getId())
                .forma_pagamento(dto.getForma_pagamento())
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