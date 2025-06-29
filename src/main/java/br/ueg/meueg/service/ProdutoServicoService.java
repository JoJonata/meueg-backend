package br.ueg.meueg.service;

import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProdutoServicoService {
    List<ProdutoServico> findAll();
    Optional<ProdutoServico> findById(Long id);
    ProdutoServico save(ProdutoServico produtoServico);
    ProdutoServico update(Long id, ProdutoServico produtoServico);
    void delete(Long id);

    List<ProdutoServico> findByUsuario(User usuario);
}