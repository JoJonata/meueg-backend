package br.ueg.meueg.service;

import br.ueg.meueg.entity.Venda;
import java.util.List;
import java.util.Optional;

public interface VendaService {
    List<Venda> findAll();
    Optional<Venda> findById(Long id);
    Venda save(Venda venda);
    Venda update(Long id, Venda venda);
    void delete(Long id);
}