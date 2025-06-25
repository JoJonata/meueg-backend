package br.ueg.meueg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private Long id; // Opcional: ID do usuário
    private String username; // Opcional: Username do usuário
    private String email; // Opcional: Email do usuário
    private String nome; // Opcional: Nome do usuário
}
