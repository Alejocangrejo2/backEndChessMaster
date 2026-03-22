package com.chess.service;

import com.chess.dto.AuthResponseDTO;
import com.chess.dto.LoginRequestDTO;
import com.chess.dto.RegisterRequestDTO;
import com.chess.model.User;
import com.chess.repository.UserRepository;
import com.chess.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de usuarios — lógica de negocio para autenticación.
 * 
 * PATRÓN MVC: Service layer que coordina entre
 * Controller (presentación) y Repository (datos).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registra un nuevo usuario.
     * - Verifica que el username y email no existan
     * - Hashea la contraseña con BCrypt
     * - Genera y retorna un JWT token
     */
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Validar que no exista el username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // Validar que no exista el email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear usuario con contraseña hasheada
        User user = new User(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);

        // Generar JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponseDTO(token, user.getUsername(), user.getRating());
    }

    /**
     * Autenticar usuario (login).
     * - Busca el usuario por username
     * - Verifica la contraseña
     * - Genera y retorna un JWT token
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        // Buscar usuario
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        // Generar JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponseDTO(token, user.getUsername(), user.getRating());
    }

    /**
     * Busca un usuario por username.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }
}
