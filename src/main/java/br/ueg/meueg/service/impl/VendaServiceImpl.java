package br.ueg.meueg.service.impl;

import br.ueg.meueg.dto.VendaDTO;
import br.ueg.meueg.dto.VendaItemDTO;
import br.ueg.meueg.entity.Estoque;
import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
import br.ueg.meueg.entity.Venda;
import br.ueg.meueg.entity.VendaItem;
import br.ueg.meueg.enums.FormaPagamento;
import br.ueg.meueg.exception.BusinessException;
import br.ueg.meueg.exception.NotFoundException;
import br.ueg.meueg.repository.EstoqueRepository;
import br.ueg.meueg.repository.ProdutoServicoRepository;
import br.ueg.meueg.repository.UserRepository;
import br.ueg.meueg.repository.VendaRepository;
import br.ueg.meueg.service.VendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendaServiceImpl implements VendaService {

    private final VendaRepository vendaRepository;
    private final UserRepository userRepository;
    private final ProdutoServicoRepository produtoServicoRepository;
    private final EstoqueRepository estoqueRepository;

    @Override
    public List<Venda> findAll() {
        return vendaRepository.findAll();
    }

    @Override
    public Optional<Venda> findById(Long id) {
        return vendaRepository.findById(id);
    }

    // --- SALVAMENTO REAL (Chamado quando clica em CONFIRMAR no App) ---
    @Override
    @Transactional
    public Venda save(Venda venda) {
        User usuario = userRepository.findById(venda.getUsuario().getId())
                .orElseThrow(() -> new NotFoundException("Usuário com ID " + venda.getUsuario().getId() + " não encontrado."));
        venda.setUsuario(usuario);

        BigDecimal valorTotal = BigDecimal.ZERO;
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new BusinessException("A venda deve conter pelo menos um item.");
        }

        for (VendaItem item : venda.getItens()) {
            ProdutoServico produtoServico = produtoServicoRepository.findById(item.getProdutoServico().getId())
                    .orElseThrow(() -> new NotFoundException("Produto/Serviço com ID " + item.getProdutoServico().getId() + " não encontrado."));

            Estoque estoque = estoqueRepository.findByProdutoServico_Id(produtoServico.getId())
                    .orElseThrow(() -> new NotFoundException("Estoque para o Produto/Serviço " + produtoServico.getNome() + " não encontrado."));

            if (estoque.getQuantidade() < item.getQuantidade()) {
                throw new BusinessException("Quantidade insuficiente em estoque para o produto " + produtoServico.getNome() + ". Disponível: " + estoque.getQuantidade());
            }

            // Baixa no estoque
            estoque.setQuantidade(estoque.getQuantidade() - item.getQuantidade());
            estoqueRepository.save(estoque);

            // Vincula dados
            item.setVenda(venda);
            item.setProdutoServico(produtoServico);
            item.setPrecoUnitario(produtoServico.getPreco());

            // Soma ao total
            valorTotal = valorTotal.add(produtoServico.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }

        venda.setData(LocalDateTime.now());
        venda.setValor_total(valorTotal);

        return vendaRepository.save(venda);
    }

    // --- SIMULAÇÃO/PRÉVIA (Chamado assim que a voz é processada) ---
    // ATENÇÃO: Renomeie na Interface VendaService também de 'saveFromAudio' para 'processarPropostaVoz'
    @Override
    public VendaDTO processarPropostaVoz(VendaDTO vendaDTO) {
        // 1. Validar usuário
        User usuarioLogado = userRepository.findById(vendaDTO.getIdUsuario())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        BigDecimal totalEstimado = BigDecimal.ZERO;

        if (vendaDTO.getItens() != null) {
            for (VendaItemDTO itemDTO : vendaDTO.getItens()) {

                // A. Correção de Quantidade (IA às vezes manda 0 ou null)
                if (itemDTO.getQuantidade() == null || itemDTO.getQuantidade() <= 0) {
                    itemDTO.setQuantidade(1);
                }

                // B. Busca Inteligente (IgnoreCase)
                // Remove espaços e garante que ache "Pipoca" mesmo se vier "pipoca"
                String nomeBusca = itemDTO.getNomeProduto().trim();

                ProdutoServico produtoServico = produtoServicoRepository.findByNomeIgnoreCaseAndUsuario(nomeBusca, usuarioLogado)
                        .orElseThrow(() -> new BusinessException("Produto '" + nomeBusca + "' não encontrado. Fale o nome exato cadastrado."));

                // C. Preencher dados para retorno ao App
                itemDTO.setIdProduto(produtoServico.getId());
                itemDTO.setNomeProduto(produtoServico.getNome()); // Pega o nome correto (Ex: Pipoca Salgada)
                itemDTO.setPrecoUnitario(produtoServico.getPreco()); // Pega o preço do banco

                // D. Calcular subtotal apenas para exibição (NÃO SALVA AINDA)
                BigDecimal subtotal = produtoServico.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade()));
                totalEstimado = totalEstimado.add(subtotal);
            }
        }

        // 2. Prepara o DTO de volta
        vendaDTO.setValor_total(totalEstimado);

        // Define forma de pagamento padrão se a IA não pegou
        if (vendaDTO.getForma_pagamento() == null) {
            vendaDTO.setForma_pagamento("DINHEIRO");
        }

        // RETORNA O DTO PREENCHIDO (SEM SALVAR NO BANCO)
        return vendaDTO;
    }

    @Override
    @Transactional
    public Venda update(Long id, Venda venda) {
        Venda existingVenda = vendaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venda com ID " + id + " não encontrada."));

        User usuario = userRepository.findById(venda.getUsuario().getId())
                .orElseThrow(() -> new NotFoundException("Usuário com ID " + venda.getUsuario().getId() + " não encontrado."));
        existingVenda.setUsuario(usuario);
        existingVenda.setForma_pagamento(venda.getForma_pagamento());
        return vendaRepository.save(existingVenda);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venda com ID " + id + " não encontrada."));

        for (VendaItem item : venda.getItens()) {
            Estoque estoque = estoqueRepository.findByProdutoServico_Id(item.getProdutoServico().getId())
                    .orElseThrow(() -> new NotFoundException("Estoque para o produto " + item.getProdutoServico().getNome() + " não encontrado ao estornar venda."));
            estoque.setQuantidade(estoque.getQuantidade() + item.getQuantidade());
            estoqueRepository.save(estoque);
        }

        vendaRepository.deleteById(id);
    }

    // --- Métodos Auxiliares (ToEntity / ToDTO) ---

    // Este método converte o DTO para Entidade na hora de SALVAR DE FATO
    public Venda toEntityFromAudio(VendaDTO dto) {
        User usuario = null;
        if (dto.getIdUsuario() != null) {
            usuario = User.builder().id(dto.getIdUsuario()).build();
        }

        FormaPagamento formaPagamento = null;
        if(dto.getForma_pagamento() != null) {
            try {
                formaPagamento = FormaPagamento.valueOf(dto.getForma_pagamento());
            } catch (IllegalArgumentException e) {
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

    public VendaDTO toDTO(Venda entity) {
        VendaDTO dto = new VendaDTO();
        dto.setId(entity.getId_venda());
        dto.setData(entity.getData());
        dto.setValor_total(entity.getValor_total());
        dto.setForma_pagamento(entity.getForma_pagamento() != null ? entity.getForma_pagamento().toString() : "DINHEIRO");
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
}