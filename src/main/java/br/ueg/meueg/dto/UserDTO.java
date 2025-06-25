package br.ueg.meueg.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String nome;
    private String password;
}
