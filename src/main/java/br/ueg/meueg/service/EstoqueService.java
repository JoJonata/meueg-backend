package br.ueg.meueg.service;

import br.ueg.meueg.entity.Estoque;
import br.ueg.meueg.entity.User;

import java.util.List;
import java.util.Optional;

public interface EstoqueService {
    // Métodos existentes (ajustados para serem menos genéricos se não forem usados)
    List<Estoque> findAll(); // Este método ainda pode ser usado por admins, por exemplo
    Optional<Estoque> findById(Long id); // Pode ser ajustado para findByIdAndUser

    // Métodos novos/modificados que recebem o User
    List<Estoque> findByUsuario(User usuario);
    Optional<Estoque> findByIdAndUsuario(Long id, User usuario); // Novo ou substitui findById para usuários comuns
    // ADICIONE ESTE MÉTODO
    Optional<Estoque> findByProdutoServico_IdAndUsuario(Long produtoServicoId, User usuario); // Para verificar estoque existente para um produto do user
    Estoque save(Estoque estoque, User usuario); // Salvar um novo estoque, associando ao usuário
    Estoque update(Long id, Estoque estoque, User usuario); // Atualizar um estoque, verificando o usuário
    void delete(Long id, User usuario); // Deletar um estoque, verificando o usuário

    // Métodos de quantidade, ajustados para receber o User
    Estoque addQuantidade(Long produtoServicoId, Integer quantidade, User usuario);
    Estoque removeQuantidade(Long produtoServicoId, Integer quantidade, User usuario);

    // Método para criar ou atualizar um estoque de forma inteligente
    Estoque createOrUpdateEstoqueForUser(Estoque estoque, User usuario);
}