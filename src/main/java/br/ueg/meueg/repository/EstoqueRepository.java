package br.ueg.meueg.repository;

import br.ueg.meueg.entity.Estoque;
import br.ueg.meueg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    Optional<Estoque> findByProdutoServico_Id(Long idProduto);

    // Adicionar um método para buscar estoque por usuário
    List<Estoque> findByUsuario(User usuario);

    // Opcional: Se você quiser buscar um item de estoque específico de um usuário
    Optional<Estoque> findByIdAndUsuario(Long id, User usuario);

    // Para buscar um item de estoque por idProduto E usuári
    Optional<Estoque> findByProdutoServico_IdAndUsuario(Long produtoServicoId, User usuario);
}