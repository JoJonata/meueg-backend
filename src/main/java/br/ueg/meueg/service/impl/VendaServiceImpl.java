package br.ueg.meueg.service.impl;

import br.ueg.meueg.entity.Estoque;
import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
import br.ueg.meueg.entity.Venda;
import br.ueg.meueg.entity.VendaItem;
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

    @Override
    @Transactional // Garante atomicidade da venda e atualização de estoque
    public Venda save(Venda venda) {
        // 1. Validar Usuário
        User usuario = userRepository.findById(venda.getUsuario().getId())
                .orElseThrow(() -> new NotFoundException("Usuário com ID " + venda.getUsuario().getId() + " não encontrado."));
        venda.setUsuario(usuario);

        // 2. Processar Itens da Venda e Atualizar Estoque
        BigDecimal valorTotal = BigDecimal.ZERO;
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new BusinessException("A venda deve conter pelo menos um item.");
        }

        for (VendaItem item : venda.getItens()) {
            ProdutoServico produtoServico = produtoServicoRepository.findById(item.getProdutoServico().getId())
                    .orElseThrow(() -> new NotFoundException("Produto/Serviço com ID " + item.getProdutoServico().getId() + " não encontrado."));

            Estoque estoque = estoqueRepository.findByProdutoServico_Id(produtoServico.getId())
                    .orElseThrow(() -> new NotFoundException("Estoque para o Produto/Serviço " + produtoServico.getNome() + " não encontrado."));

            // Verifica se há quantidade suficiente em estoque
            if (estoque.getQuantidade() < item.getQuantidade()) {
                throw new BusinessException("Quantidade insuficiente em estoque para o produto " + produtoServico.getNome() + ". Disponível: " + estoque.getQuantidade());
            }

            // Atualiza o estoque
            estoque.setQuantidade(estoque.getQuantidade() - item.getQuantidade());
            estoqueRepository.save(estoque);

            // Configura o item da venda
            item.setVenda(venda); // Liga o item à venda pai
            item.setProdutoServico(produtoServico); // Garante que o objeto gerenciado pelo JPA é usado
            item.setPrecoUnitario(produtoServico.getPreco()); // Pega o preço atual do produto
            valorTotal = valorTotal.add(produtoServico.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }

        venda.setData(LocalDateTime.now()); // Define a data da venda
        venda.setValor_total(valorTotal); // Calcula o valor total da venda

        return vendaRepository.save(venda);
    }

    @Override
    @Transactional
    public Venda update(Long id, Venda venda) {
        // Atualizar uma venda é complexo, pois envolve itens de estoque e valores.
        // Para simplificar, esta implementação permite apenas alterar o ID do usuário e forma de pagamento.
        // Se precisar alterar itens ou quantidades, considere deletar e criar uma nova venda,
        // ou implementar uma lógica de compensação de estoque bem complexa.
        Venda existingVenda = vendaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venda com ID " + id + " não encontrada."));

        User usuario = userRepository.findById(venda.getUsuario().getId())
                .orElseThrow(() -> new NotFoundException("Usuário com ID " + venda.getUsuario().getId() + " não encontrado."));
        existingVenda.setUsuario(usuario);
        existingVenda.setForma_pagamento(venda.getForma_pagamento());

        // Itens de venda e valor_total não são atualizados diretamente aqui para evitar inconsistências complexas.
        // Uma atualização de venda geralmente envolve lógica de "estorno" ou "re-cálculo de estoque".

        return vendaRepository.save(existingVenda);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venda com ID " + id + " não encontrada."));

        // Ao deletar uma venda, você deve "devolver" os itens para o estoque
        for (VendaItem item : venda.getItens()) {
            Estoque estoque = estoqueRepository.findByProdutoServico_Id(item.getProdutoServico().getId())
                    .orElseThrow(() -> new NotFoundException("Estoque para o produto " + item.getProdutoServico().getNome() + " não encontrado ao estornar venda."));
            estoque.setQuantidade(estoque.getQuantidade() + item.getQuantidade());
            estoqueRepository.save(estoque);
        }

        vendaRepository.deleteById(id);
    }
}