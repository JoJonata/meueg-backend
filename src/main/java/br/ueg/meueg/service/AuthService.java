package br.ueg.meueg.service;

import br.ueg.meueg.dto.LoginRequest;
import br.ueg.meueg.dto.AuthResponse;
import br.ueg.meueg.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
