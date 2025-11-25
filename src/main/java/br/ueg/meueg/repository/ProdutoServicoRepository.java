package br.ueg.meueg.repository;

import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoServicoRepository extends JpaRepository<ProdutoServico, Long> {

    List<ProdutoServico> findByUsuario(User usuario);

    boolean existsByNomeAndUsuario(String nome, User usuario);

    @Query("SELECT ps.nome FROM ProdutoServico ps WHERE ps.usuario.id = :idUsuario")
    List<String> findAllNomesByUserId(@Param("idUsuario") Long idUsuario);

    Optional<ProdutoServico> findByNomeAndUsuario(String nome, User usuario);

    // --- NOVO MÉTODO OBRIGATÓRIO ---
    // Encontra o produto mesmo se a IA mandar minúsculo e no banco estiver maiúsculo
    Optional<ProdutoServico> findByNomeIgnoreCaseAndUsuario(String nome, User usuario);
}