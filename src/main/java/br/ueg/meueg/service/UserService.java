package br.ueg.meueg.service;

import br.ueg.meueg.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Long id);
    User save(User user);
    User update(Long id, User user);
    void delete(Long id);
    User findByUsername(String username);
}
