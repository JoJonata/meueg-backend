package br.ueg.meueg.repository;

import br.ueg.meueg.entity.ProdutoServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoServicoRepository extends JpaRepository<ProdutoServico, Long> {
    // Métodos de busca personalizados, se necessário. Ex:
    // Optional<ProdutoServico> findByNome(String nome);
    // List<ProdutoServico> findByCategoria(String categoria);
}