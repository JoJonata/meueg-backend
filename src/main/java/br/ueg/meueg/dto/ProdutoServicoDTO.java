package br.ueg.meueg.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoServicoDTO {
    private Long id_produto;
    private String nome;
    private String tipo;
    private BigDecimal preco;
    private BigDecimal custo;
    private String categoria;
}