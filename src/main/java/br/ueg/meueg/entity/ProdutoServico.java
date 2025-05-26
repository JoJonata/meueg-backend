package br.ueg.meueg.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "produto_servico") // Nome da tabela no banco
@Data // Lombok para getters, setters, toString, equals e hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // Para usar o User.builder()...
public class ProdutoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_produto; // Ou apenas 'id'

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private String tipo; // Ex: "Produto", "Servico"

    @Column(nullable = false, precision = 10, scale = 2) // Exemplo para valores monetários
    private BigDecimal preco;

    @Column(nullable = false, precision = 10, scale = 2) // Exemplo para valores monetários
    private BigDecimal custo;

    @Column(nullable = false)
    private String categoria;
}