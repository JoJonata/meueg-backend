package br.ueg.meueg.service;

import br.ueg.meueg.entity.Estoque;
import java.util.List;
import java.util.Optional;

public interface EstoqueService {
    List<Estoque> findAll();
    Optional<Estoque> findById(Long id);
    Estoque save(Estoque estoque);
    Estoque update(Long id, Estoque estoque);
    void delete(Long id);
    Estoque addQuantidade(Long idProduto, Integer quantidade);
    Estoque removeQuantidade(Long idProduto, Integer quantidade);
}