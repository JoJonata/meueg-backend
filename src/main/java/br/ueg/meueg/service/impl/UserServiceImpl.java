package br.ueg.meueg.service.impl;

import br.ueg.meueg.entity.User;
import br.ueg.meueg.repository.UserRepository;
import br.ueg.meueg.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        existing.setUsername(user.getUsername());

        if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2a$")) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        } else if (user.getPassword() == null || user.getPassword().isEmpty()) {}

        existing.setEmail(user.getEmail());

        return userRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public User findByUsername(String username) {
        // Assume que 'username' no seu User Entity é o campo que você usa para login
        // (geralmente o email)
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o username: " + username));
    }
}
