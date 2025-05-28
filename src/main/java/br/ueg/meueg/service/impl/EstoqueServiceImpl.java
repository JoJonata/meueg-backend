package br.ueg.meueg.service.impl;

import br.ueg.meueg.entity.Estoque;
import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.exception.BusinessException;
import br.ueg.meueg.exception.NotFoundException;
import br.ueg.meueg.repository.EstoqueRepository;
import br.ueg.meueg.repository.ProdutoServicoRepository;
import br.ueg.meueg.service.EstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EstoqueServiceImpl implements EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoServicoRepository produtoServicoRepository;

    @Override
    public List<Estoque> findAll() {
        return estoqueRepository.findAll();
    }

    @Override
    public Optional<Estoque> findById(Long id) {
        return estoqueRepository.findById(id);
    }

    @Override
    @Transactional
    public Estoque save(Estoque estoque) {
        ProdutoServico produtoServico = produtoServicoRepository.findById(estoque.getProdutoServico().getId())
                .orElseThrow(() -> new NotFoundException("Produto/Serviço com ID " + estoque.getProdutoServico().getId() + " não encontrado."));

        if (estoqueRepository.findByProdutoServico_Id(produtoServico.getId()).isPresent()) {
            throw new BusinessException("Já existe um registro de estoque para o Produto/Serviço com ID " + produtoServico.getId() + ".");
        }

        estoque.setProdutoServico(produtoServico);
        return estoqueRepository.save(estoque);
    }

    @Override
    @Transactional
    public Estoque update(Long id, Estoque estoque) {
        Estoque existingEstoque = estoqueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estoque com ID " + id + " não encontrado."));

        existingEstoque.setQuantidade(estoque.getQuantidade());

        return estoqueRepository.save(existingEstoque);
    }

    @Override
    public void delete(Long id) {
        if (!estoqueRepository.existsById(id)) {
            throw new NotFoundException("Estoque com ID " + id + " não encontrado.");
        }
        estoqueRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Estoque addQuantidade(Long idProduto, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoServico_Id(idProduto)
                .orElseThrow(() -> new NotFoundException("Estoque para Produto/Serviço com ID " + idProduto + " não encontrado."));

        if (quantidade <= 0) {
            throw new BusinessException("A quantidade a ser adicionada deve ser positiva.");
        }

        estoque.setQuantidade(estoque.getQuantidade() + quantidade);
        return estoqueRepository.save(estoque);
    }

    @Override
    @Transactional
    public Estoque removeQuantidade(Long idProduto, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoServico_Id(idProduto)
                .orElseThrow(() -> new NotFoundException("Estoque para Produto/Serviço com ID " + idProduto + " não encontrado."));

        if (quantidade <= 0) {
            throw new BusinessException("A quantidade a ser removida deve ser positiva.");
        }

        if (estoque.getQuantidade() < quantidade) {
            throw new BusinessException("Quantidade insuficiente em estoque. Disponível: " + estoque.getQuantidade() + ", Solicitado: " + quantidade);
        }

        estoque.setQuantidade(estoque.getQuantidade() - quantidade);
        return estoqueRepository.save(estoque);
    }
}