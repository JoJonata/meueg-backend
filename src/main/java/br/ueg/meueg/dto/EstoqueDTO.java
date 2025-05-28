package br.ueg.meueg.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueDTO {
    private Long id;
    private Long idProduto;
    private String nomeProduto;
    private Integer quantidade;
}