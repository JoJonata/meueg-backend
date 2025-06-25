package br.ueg.meueg.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "estoque")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_produto_servico", referencedColumnName = "id_produto", nullable = false)
    private ProdutoServico produtoServico;

    @Column(nullable = false)
    private Integer quantidade;

    @ManyToOne(fetch = FetchType.LAZY) // Relacionamento com User
    @JoinColumn(name = "id_usuario", nullable = false) // Coluna da chave estrangeira para o usuário
    private User usuario;
}