package br.ueg.meueg.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository<User> extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}