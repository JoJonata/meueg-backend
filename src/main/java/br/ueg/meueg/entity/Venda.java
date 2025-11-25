package br.ueg.meueg.entity;

import br.ueg.meueg.enums.FormaPagamento;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "venda")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_venda;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor_total;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FormaPagamento forma_pagamento;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", nullable = false)
    private User usuario;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendaItem> itens;
}