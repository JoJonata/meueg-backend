package br.ueg.meueg.controller;

import br.ueg.meueg.dto.EstoqueDTO;
import br.ueg.meueg.entity.Estoque;
import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
import br.ueg.meueg.exception.BusinessException;
import br.ueg.meueg.exception.NotFoundException;
import br.ueg.meueg.service.EstoqueService;
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
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class EstoqueController {

    private final EstoqueService estoqueService;
    private final UserService userService;
    private final ProdutoServicoService produtoServicoService;

    // **NOVO ENDPOINT: Buscar estoque por usuário logado**
    @GetMapping("/meu-estoque") // Ou simplesmente @GetMapping se você remover o getAllEstoque() genérico
    public ResponseEntity<List<EstoqueDTO>> getEstoqueByLoggedInUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        User usuario = userService.findByUsername(userDetails.getUsername());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.");
        }

        List<EstoqueDTO> dtos = estoqueService.findByUsuario(usuario).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Mantenha seu getEstoqueById se ainda for usado, mas ele deve considerar o usuário
    @GetMapping("/{id}")
    public ResponseEntity<EstoqueDTO> getEstoqueById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userService.findByUsername(userDetails.getUsername());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.");
        }
        return estoqueService.findByIdAndUsuario(id, usuario) // Use o novo método findByIdAndUsuario
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Ajustar o createEstoque para garantir o usuário e usar idProduto para nome
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EstoqueDTO> createEstoque(
            @RequestBody EstoqueDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userService.findByUsername(userDetails.getUsername());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.");
        }

        // Converte o DTO para Entidade
        Estoque entity = toEntity(dto);
        // O serviço agora irá lidar com a associação do ProdutoServico e Usuário
        // e se deve criar ou atualizar a entrada de estoque.
        Estoque savedEntity = estoqueService.createOrUpdateEstoqueForUser(entity, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(savedEntity));
    }


    // Ajuste nos métodos de aumentar/diminuir para filtrar por usuário
    // O DTO deve ter o idProduto ou a Entidade ProdutoServico, não o id.
    // Mudei o path para ser idProduto e o request body para aceitar o DTO completo.
    @PutMapping("/aumentar") // Sem id no path, recebe DTO completo
    public ResponseEntity<EstoqueDTO> addQuantidadeEstoque(
            @RequestBody EstoqueDTO dto, // Recebe o DTO com idProduto e quantidade
            @AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userService.findByUsername(userDetails.getUsername());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.");
        }
        try {
            Estoque updatedEstoque = estoqueService.addQuantidade(dto.getIdProduto(), dto.getQuantidade(), usuario);
            return ResponseEntity.ok(toDTO(updatedEstoque));
        } catch (NotFoundException e) { // Capture exceções específicas do serviço
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BusinessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/remover") // Sem id no path, recebe DTO completo
    public ResponseEntity<EstoqueDTO> removeQuantidadeEstoque(
            @RequestBody EstoqueDTO dto, // Recebe o DTO com idProduto e quantidade
            @AuthenticationPrincipal UserDetails userDetails) {
        User usuario = userService.findByUsername(userDetails.getUsername());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.");
        }
        try {
            Estoque updatedEstoque = estoqueService.removeQuantidade(dto.getIdProduto(), dto.getQuantidade(), usuario);
            return ResponseEntity.ok(toDTO(updatedEstoque));
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BusinessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteEstoque(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User usuarioLogado = userService.findByUsername(userDetails.getUsername());
        if (usuarioLogado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.");
        }
        try {
            estoqueService.delete(id, usuarioLogado);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BusinessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Métodos de conversão DTO <-> Entity
    private EstoqueDTO toDTO(Estoque entity) {
        EstoqueDTO dto = new EstoqueDTO();
        dto.setId(entity.getId());
        dto.setIdProduto(entity.getProdutoServico().getId()); // Pega o ID do ProdutoServico
        dto.setNomeProduto(entity.getProdutoServico().getNome()); // Pega o NOME do ProdutoServico
        dto.setQuantidade(entity.getQuantidade());
        return dto;
    }

    private Estoque toEntity(EstoqueDTO dto) {
        // Cuidado: Ao converter para entidade, o ProdutoServico precisa ser setado corretamente
        // aqui estamos apenas criando um ProdutoServico "fake" com o ID.
        // O serviço fará a busca real do ProdutoServico.
        ProdutoServico produtoServico = new ProdutoServico();
        produtoServico.setId(dto.getIdProduto());

        return Estoque.builder()
                .id(dto.getId()) // ID pode ser nulo para criação
                .produtoServico(produtoServico) // Setar a entidade ProdutoServico
                .quantidade(dto.getQuantidade())
                .build();
    }
}