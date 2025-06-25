package br.ueg.meueg.service.impl;

import br.ueg.meueg.entity.Estoque;
import br.ueg.meueg.entity.ProdutoServico;
import br.ueg.meueg.entity.User;
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
    private final ProdutoServicoRepository produtoServicoRepository; // Necessário para buscar ProdutoServico

    @Override
    public List<Estoque> findAll() {
        // Este método buscará TUDO. Geralmente, usado apenas para admins.
        return estoqueRepository.findAll();
    }

    // NOVO: Buscar estoque por usuário
    @Override
    public List<Estoque> findByUsuario(User usuario) {
        return estoqueRepository.findByUsuario(usuario);
    }

    // NOVO: Buscar um estoque pelo ID e USUÁRIO
    @Override
    public Optional<Estoque> findByIdAndUsuario(Long id, User usuario) {
        return estoqueRepository.findByIdAndUsuario(id, usuario);
    }

    @Override
    public Optional<Estoque> findByProdutoServico_IdAndUsuario(Long produtoServicoId, User usuario) {
        return estoqueRepository.findByProdutoServico_IdAndUsuario(produtoServicoId, usuario);
    }

    @Override
    public Optional<Estoque> findById(Long id) {
        // Este método pode ser removido se findByIdAndUsuario for o padrão
        // ou usado com cautela, pois não filtra por usuário.
        return estoqueRepository.findById(id);
    }

    @Override
    @Transactional
    public Estoque save(Estoque estoque, User usuario) { // Recebe User
        ProdutoServico produtoServico = produtoServicoRepository.findById(estoque.getProdutoServico().getId())
                .orElseThrow(() -> new NotFoundException("Produto/Serviço com ID " + estoque.getProdutoServico().getId() + " não encontrado."));

        // Verifica se já existe um registro de estoque para este PRODUTO E USUÁRIO
        if (estoqueRepository.findByProdutoServico_IdAndUsuario(produtoServico.getId(), usuario).isPresent()) {
            throw new BusinessException("Já existe um registro de estoque para o Produto/Serviço com ID " + produtoServico.getId() + " para este usuário.");
        }

        estoque.setProdutoServico(produtoServico);
        estoque.setUsuario(usuario); // Define o usuário
        return estoqueRepository.save(estoque);
    }

    @Override
    @Transactional
    public Estoque update(Long id, Estoque estoqueAtualizado, User usuario) { // Recebe User
        Estoque existingEstoque = estoqueRepository.findByIdAndUsuario(id, usuario) // Busca pelo ID e USUÁRIO
                .orElseThrow(() -> new NotFoundException("Estoque com ID " + id + " não encontrado ou não pertence ao usuário."));

        existingEstoque.setQuantidade(estoqueAtualizado.getQuantidade());

        // Não atualize produtoServico ou usuario aqui, a menos que seja um caso muito específico.
        // existingEstoque.setProdutoServico(estoqueAtualizado.getProdutoServico());

        return estoqueRepository.save(existingEstoque);
    }

    @Override
    @Transactional
    public void delete(Long id, User usuario) { // Recebe User
        Estoque existingEstoque = estoqueRepository.findByIdAndUsuario(id, usuario) // Busca pelo ID e USUÁRIO
                .orElseThrow(() -> new NotFoundException("Estoque com ID " + id + " não encontrado ou não pertence ao usuário."));
        estoqueRepository.delete(existingEstoque);
    }

    @Override
    @Transactional
    public Estoque addQuantidade(Long produtoServicoId, Integer quantidade, User usuario) { // Recebe User
        Estoque estoque = estoqueRepository.findByProdutoServico_IdAndUsuario(produtoServicoId, usuario) // Busca pelo ID do Produto/Serviço e USUÁRIO
                .orElseThrow(() -> new NotFoundException("Estoque para Produto/Serviço com ID " + produtoServicoId + " não encontrado para este usuário."));

        if (quantidade <= 0) {
            throw new BusinessException("A quantidade a ser adicionada deve ser positiva.");
        }

        estoque.setQuantidade(estoque.getQuantidade() + quantidade);
        return estoqueRepository.save(estoque);
    }

    @Override
    @Transactional
    public Estoque removeQuantidade(Long produtoServicoId, Integer quantidade, User usuario) { // Recebe User
        Estoque estoque = estoqueRepository.findByProdutoServico_IdAndUsuario(produtoServicoId, usuario) // Busca pelo ID do Produto/Serviço e USUÁRIO
                .orElseThrow(() -> new NotFoundException("Estoque para Produto/Serviço com ID " + produtoServicoId + " não encontrado para este usuário."));

        if (quantidade <= 0) {
            throw new BusinessException("A quantidade a ser removida deve ser positiva.");
        }

        if (estoque.getQuantidade() < quantidade) {
            throw new BusinessException("Quantidade insuficiente em estoque. Disponível: " + estoque.getQuantidade() + ", Solicitado: " + quantidade);
        }

        estoque.setQuantidade(estoque.getQuantidade() - quantidade);
        return estoqueRepository.save(estoque);
    }

    // NOVO: Método para criar ou atualizar um item de estoque de forma inteligente (para o POST)
    @Override
    @Transactional
    public Estoque createOrUpdateEstoqueForUser(Estoque estoqueDtoParaSalvar, User usuario) {
        // Encontra o ProdutoServico pelo ID
        ProdutoServico produtoServico = produtoServicoRepository.findById(estoqueDtoParaSalvar.getProdutoServico().getId())
                .orElseThrow(() -> new NotFoundException("Produto/Serviço com ID " + estoqueDtoParaSalvar.getProdutoServico().getId() + " não encontrado."));

        // Tenta encontrar um item de estoque existente para este produto E este usuário
        Optional<Estoque> existingEstoque = estoqueRepository.findByProdutoServico_IdAndUsuario(produtoServico.getId(), usuario);

        if (existingEstoque.isPresent()) {
            // Se já existe, atualiza a quantidade
            Estoque item = existingEstoque.get();
            item.setQuantidade(item.getQuantidade() + estoqueDtoParaSalvar.getQuantidade()); // Adiciona a quantidade
            return estoqueRepository.save(item);
        } else {
            // Se não existe, cria um novo item de estoque
            Estoque newEstoque = new Estoque();
            newEstoque.setProdutoServico(produtoServico);
            newEstoque.setUsuario(usuario); // Associa ao usuário
            newEstoque.setQuantidade(estoqueDtoParaSalvar.getQuantidade()); // Define a quantidade inicial
            return estoqueRepository.save(newEstoque);
        }
    }
}