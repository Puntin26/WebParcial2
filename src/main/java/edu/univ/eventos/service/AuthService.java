package edu.univ.eventos.service;

import edu.univ.eventos.model.Role;
import edu.univ.eventos.model.User;
import edu.univ.eventos.repository.UserRepository;
import edu.univ.eventos.util.PasswordUtil;

public class AuthService {
    private final UserRepository userRepository = new UserRepository();

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException("Credenciales inválidas"));
        if (user.isBlocked()) throw new AppException("Usuario bloqueado");
        if (!PasswordUtil.verify(password, user.getPasswordHash())) throw new AppException("Credenciales inválidas");
        return user;
    }

    public User registerParticipant(String name, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) throw new AppException("Email ya registrado");
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(PasswordUtil.hash(password));
        u.setRole(Role.PARTICIPANTE);
        return userRepository.save(u);
    }
}
