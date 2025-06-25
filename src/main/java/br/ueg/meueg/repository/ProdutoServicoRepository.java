package br.ueg.meueg.repository;

import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoServicoRepository extends JpaRepository<ProdutoServico, Long> {
    List<ProdutoServico> findByUsuario(User usuario);
    // Métodos de busca personalizados, se necessário. Ex:
    // Optional<ProdutoServico> findByNome(String nome);
    // List<ProdutoServico> findByCategoria(String categoria);
}