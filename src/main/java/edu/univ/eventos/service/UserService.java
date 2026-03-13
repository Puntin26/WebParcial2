package edu.univ.eventos.service;

import edu.univ.eventos.model.Role;
import edu.univ.eventos.model.User;
import edu.univ.eventos.repository.UserRepository;

import java.util.List;

public class UserService {
    private final UserRepository userRepository = new UserRepository();

    public List<User> allUsers() { return userRepository.findAll(); }

    public User changeRole(Long userId, Role role) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("Usuario no existe"));
        if ("admin@universidad.edu".equalsIgnoreCase(user.getEmail()) && role != Role.ADMIN) {
            throw new AppException("No se puede remover rol ADMIN del usuario inicial");
        }
        user.setRole(role);
        return userRepository.save(user);
    }

    public User toggleBlock(Long userId, boolean blocked) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("Usuario no existe"));
        if ("admin@universidad.edu".equalsIgnoreCase(user.getEmail())) {
            throw new AppException("No se puede bloquear admin inicial");
        }
        user.setBlocked(blocked);
        return userRepository.save(user);
    }
}
