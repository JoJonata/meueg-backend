package br.ueg.meueg.service.impl;

import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
import br.ueg.meueg.repository.ProdutoServicoRepository;
import br.ueg.meueg.service.ProdutoServicoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoServicoServiceImpl implements ProdutoServicoService {

    private final ProdutoServicoRepository produtoServicoRepository;

    @Override
    public List<ProdutoServico> findAll() {
        return produtoServicoRepository.findAll();
    }

    @Override
    public Optional<ProdutoServico> findById(Long id) {
        return produtoServicoRepository.findById(id);
    }

    @Override
    public ProdutoServico save(ProdutoServico produtoServico) {
        return produtoServicoRepository.save(produtoServico);
    }

    @Override
    public ProdutoServico update(Long id, ProdutoServico produtoServico) {
        ProdutoServico existingProdutoServico = produtoServicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto/Serviço não encontrado com ID: " + id));

        existingProdutoServico.setNome(produtoServico.getNome());
        existingProdutoServico.setTipo(produtoServico.getTipo());
        existingProdutoServico.setPreco(produtoServico.getPreco());
        existingProdutoServico.setCusto(produtoServico.getCusto());
        existingProdutoServico.setCategoria(produtoServico.getCategoria());

        return produtoServicoRepository.save(existingProdutoServico);
    }

    @Override
    public void delete(Long id) {
        if (!produtoServicoRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto/Serviço não encontrado com ID: " + id);
        }
        produtoServicoRepository.deleteById(id);
    }

    @Override
    public List<ProdutoServico> findByUsuario(User usuario) {
        return produtoServicoRepository.findByUsuario(usuario);
    }
}