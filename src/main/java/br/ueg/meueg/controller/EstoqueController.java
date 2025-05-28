package br.ueg.meueg.controller;

import br.ueg.meueg.dto.EstoqueDTO;
import br.ueg.meueg.entity.Estoque;
import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping
    @Operation(summary = "Lista todos os registros de estoque")
    public ResponseEntity<List<EstoqueDTO>> getAllEstoque() {
        List<EstoqueDTO> dtos = estoqueService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um registro de estoque por ID")
    public ResponseEntity<EstoqueDTO> getEstoqueById(@PathVariable Long id) {
        return estoqueService.findById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo registro de estoque para um Produto/Serviço")
    public ResponseEntity<EstoqueDTO> createEstoque(@RequestBody EstoqueDTO dto) {
        Estoque estoque = toEntity(dto);
        Estoque savedEstoque = estoqueService.save(estoque);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(savedEstoque));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza a quantidade de um registro de estoque existente")
    public ResponseEntity<EstoqueDTO> updateEstoque(@PathVariable Long id, @RequestBody EstoqueDTO dto) {
        Estoque estoque = toEntity(dto); // O ID do Produto pode ser ignorado aqui, estamos atualizando pelo ID do Estoque
        Estoque updatedEstoque = estoqueService.update(id, estoque);
        return ResponseEntity.ok(toDTO(updatedEstoque));
    }

    @PatchMapping("/add/{idProduto}")
    @Operation(summary = "Adiciona quantidade ao estoque de um Produto/Serviço")
    public ResponseEntity<EstoqueDTO> addQuantidadeEstoque(@PathVariable Long idProduto, @RequestParam Integer quantidade) {
        Estoque updatedEstoque = estoqueService.addQuantidade(idProduto, quantidade);
        return ResponseEntity.ok(toDTO(updatedEstoque));
    }

    @PatchMapping("/remove/{idProduto}")
    @Operation(summary = "Remove quantidade do estoque de um Produto/Serviço")
    public ResponseEntity<EstoqueDTO> removeQuantidadeEstoque(@PathVariable Long idProduto, @RequestParam Integer quantidade) {
        Estoque updatedEstoque = estoqueService.removeQuantidade(idProduto, quantidade);
        return ResponseEntity.ok(toDTO(updatedEstoque));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta um registro de estoque por ID")
    public ResponseEntity<Void> deleteEstoque(@PathVariable Long id) {
        estoqueService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Métodos de mapeamento (Entidade <-> DTO)
    private EstoqueDTO toDTO(Estoque estoque) {
        EstoqueDTO dto = new EstoqueDTO();
        dto.setId(estoque.getId());
        dto.setIdProduto(estoque.getProdutoServico() != null ? estoque.getProdutoServico().getId() : null);
        dto.setNomeProduto(estoque.getProdutoServico() != null ? estoque.getProdutoServico().getNome() : null);
        dto.setQuantidade(estoque.getQuantidade());
        return dto;
    }

    private Estoque toEntity(EstoqueDTO dto) {
        ProdutoServico produtoServico = null;
        if (dto.getIdProduto() != null) {
            produtoServico = ProdutoServico.builder().id(dto.getIdProduto()).build();
        }

        return Estoque.builder()
                .id(dto.getId()) // Pode ser nulo para criação
                .produtoServico(produtoServico)
                .quantidade(dto.getQuantidade())
                .build();
    }
}