package br.ueg.meueg.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaDTO {
    private Long id;
    private LocalDateTime data;
    private BigDecimal valor_total;
    private String forma_pagamento;
    private Long idUsuario;
    private String usernameUsuario;
    private List<VendaItemDTO> itens;
}