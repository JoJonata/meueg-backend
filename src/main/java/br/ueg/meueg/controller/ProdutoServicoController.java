package br.ueg.meueg.controller;

import br.ueg.meueg.dto.ProdutoServicoDTO;
import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
import br.ueg.meueg.service.ProdutoServicoService;
import br.ueg.meueg.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/produtos-servicos")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class ProdutoServicoController {

    private final ProdutoServicoService produtoServicoService;
    private final UserService userService;
    @GetMapping
    public ResponseEntity<List<ProdutoServicoDTO>> getAllProdutosServicos(
            @AuthenticationPrincipal UserDetails userDetails) {

        User usuario = userService.findByUsername(userDetails.getUsername());

        List<ProdutoServicoDTO> dtos = produtoServicoService.findByUsuario(usuario).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProdutoServicoDTO> getProdutoServicoById(@PathVariable Long id) {
        return produtoServicoService.findById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ProdutoServicoDTO> createProdutoServico(
            @RequestBody ProdutoServicoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        User usuario = userService.findByUsername(userDetails.getUsername());

        ProdutoServico entity = toEntity(dto);
        entity.setUsuario(usuario);

        ProdutoServico savedEntity = produtoServicoService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(savedEntity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoServicoDTO> updateProdutoServico(
            @PathVariable Long id,
            @RequestBody ProdutoServicoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProdutoServico entity = toEntity(dto);
        ProdutoServico existente = produtoServicoService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User usuarioLogado = userService.findByUsername(userDetails.getUsername());

        if (!existente.getUsuario().equals(usuarioLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para atualizar este produto.");
        }

        entity.setUsuario(usuarioLogado); // Garante o vínculo
        ProdutoServico updatedEntity = produtoServicoService.update(id, entity);

        return ResponseEntity.ok(toDTO(updatedEntity));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProdutoServico(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProdutoServico existente = produtoServicoService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User usuarioLogado = userService.findByUsername(userDetails.getUsername());

        if (!existente.getUsuario().equals(usuarioLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este produto.");
        }

        produtoServicoService.delete(id);

        return ResponseEntity.noContent().build();
    }

    private ProdutoServicoDTO toDTO(ProdutoServico entity) {
        ProdutoServicoDTO dto = new ProdutoServicoDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setTipo(entity.getTipo());
        dto.setPreco(entity.getPreco());
        dto.setCusto(entity.getCusto());
        dto.setCategoria(entity.getCategoria());
        return dto;
    }

    private ProdutoServico toEntity(ProdutoServicoDTO dto) {
        return ProdutoServico.builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .tipo(dto.getTipo())
                .preco(dto.getPreco())
                .custo(dto.getCusto())
                .categoria(dto.getCategoria())
                .build();
    }
}