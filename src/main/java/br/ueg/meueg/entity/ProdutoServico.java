package br.ueg.meueg.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "produto_servico")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_produto;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal custo;

    @Column(nullable = false)
    private String categoria;
}